package com.xxxx.springsecurityoauth2demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/oauth")
@Slf4j
public class AuthController   implements InitializingBean {


    //令牌请求的端点
    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private ClientDetailsService clientDetailsService;


    @Qualifier("jwtTokenServices")
    @Autowired
    private DefaultTokenServices authorizationServerTokenServices;

    /**
     * 重写/oauth/token这个默认接口，返回的数据格式统一
     */
    @PostMapping(value = "/token")
    public  OAuth2AccessToken postAccessToken(Principal principal, @RequestParam
            Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        OAuth2AccessToken accessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        ClientDetails authenticatedClient = tokenEndpoint.getClientDetailsService().loadClientByClientId(clientId);

        // 3、 通过客户端信息生成 TokenRequest 对象
        TokenRequest tokenRequest = getOAuth2RequestFactory().createTokenRequest(parameters, authenticatedClient);


        // 4、 调用 TokenGranter.grant()方法生成 OAuth2AccessToken 对象（即token）
        OAuth2AccessToken token = getTokenGranter().grant(tokenRequest.getGrantType(), tokenRequest);
        if (token == null) {
            throw new UnsupportedGrantTypeException("Unsupported grant type: " + tokenRequest.getGrantType());
        }
        // 5、 返回token
        return getResponse(token);

    }



    @PostMapping(value = "/login")
    @ResponseBody
    public String doLogin(
            HttpServletRequest request,
            String username,
            String password) {

        // 自定义响应对象
        LoginRes res = new LoginRes();

        try {
            // 对请求头进行 base64 解码, 获取 client id 和 client secret
            String[] tokens = CryptUtils.decodeBasicHeader(request.getHeader("Authorization"));
            String clientId = tokens[0];
            String clientSecret = tokens[1];

            // 通过 clientId 获取客户端详情
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);


           // 通过 username 和 password 构建一个 Authentication 对象
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(req.getUsername(),
                    req.getPassword());
            // 验证用户信息
            Authentication auth = authenticationManager.authenticate(authRequest);
            // 放入 Secirty 的上下文
            SecurityContextHolder.getContext().setAuthentication(auth);


            // 通过 Client 信息和 请求参数, 获取一个 TokenRequest 对象
            TokenRequest tokenRequest = new TokenRequest(new HashMap<String, String>(), clientId,
                    clientDetails.getScope(), "client_credentials");

            // 通过 TokenRequest 和 ClientDetails 构建 OAuthRequest
            OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

            // 通过 OAuth2Request 和 Authentication 构建OAuth2Authentication
            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, auth);

            // 通过 OAuth2Authentication 构建 OAuth2AccessToken
            OAuth2AccessToken token = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);

            // 把 token 信息封装到 自定义的响应对象中
            res.setAccessToken(token.getValue());
            res.setTokenType(token.getTokenType());
            res.setRefreshToken(token.getRefreshToken().getValue());
            res.setExpiresIn(token.getExpiresIn());
            res.setScope(token.getScope().toString());

        } catch (Exception e) {
            log.warn("Fail to login of user {} for {}", req.getUsername(), e.getMessage());
        }
        return JsonUtil.toJsonString(res);
    }





    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
