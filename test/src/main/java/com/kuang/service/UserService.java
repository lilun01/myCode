package com.kuang.service;

import com.kuang.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ${author}
 * @since 2020-04-14
 */
public interface UserService extends IService<User> {
	
	public int insert(User user);

}
