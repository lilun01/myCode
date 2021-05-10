package com.kuang.controller;

import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuang.entity.User;
import com.kuang.service.UserService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ${author}
 * @since 2020-04-14
 */
@RestController
@RequestMapping("/user")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@PostMapping("/save")
	public int save(@RequestBody User user) {
		return userService.insert(user);
	}

	
	@GetMapping("/limiter")
	public String limiter() {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		RedissonClient client = Redisson.create(config);
		RRateLimiter rateLimiter = client.getRateLimiter("rate_limiter");
		rateLimiter.trySetRate(RateType.OVERALL, 10, 1, RateIntervalUnit.MINUTES);
		rateLimiter.acquire();
		logger.info("允许执行");
		return "执行成功";

	}
	
	@GetMapping("/limiter2")
	public String limiter2() throws InterruptedException {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		RedissonClient client = Redisson.create(config);
		RPermitExpirableSemaphore semaphore = client.getPermitExpirableSemaphore("mySemaphore");
		semaphore.trySetPermits(10);
		String permitId = semaphore.acquire();
		// 获取一个信号，有效期只有2秒钟。
		String permitId2 = semaphore.acquire(2, TimeUnit.SECONDS);
		// ...
		semaphore.release(permitId2);
		logger.info("允许执行");
		return "执行成功";
	}
	
	@GetMapping("/limiter3")
	public String limiter3() throws InterruptedException {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:6379");
		RedissonClient client = Redisson.create(config);
		RPermitExpirableSemaphore semaphore = client.getPermitExpirableSemaphore("mySemaphore");
		// 获取一个信号，有效期只有2秒钟。
		String permitId2 = semaphore.acquire(2, TimeUnit.SECONDS);
		
		semaphore.release(permitId2);
		logger.info("允许执行");
		return "执行成功";
	}

}
