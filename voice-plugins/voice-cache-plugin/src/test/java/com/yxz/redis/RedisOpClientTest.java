package com.yxz.redis;

import com.yzx.core.config.ConfigUtils;
import com.yzx.redis.RedisOpClient;

public class RedisOpClientTest {
	public static void main(String[] args) {
		ConfigUtils.load();
//		RedisOpClient._init();
		RedisOpClient.init();
		
//		System.out.println(RedisOpClient.get("xupiao"));
//		
//		System.out.println("set begin=======" + Thread.currentThread().getName());
//		RedisOpClient.set("xupiao", "matrix", x -> {
//			if(x.succeeded()) {
//				System.out.println("set begin=======" + Thread.currentThread().getName());
//				try {
//					Thread.sleep(2 * 1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				System.out.println("set method=======" + Thread.currentThread().getName() + x.result());
//			} else {
//				System.out.println(x.cause());
//			}
//		});
//		System.out.println("set end=======" + Thread.currentThread().getName());
//		
//		System.out.println("get begin=======" + Thread.currentThread().getName());
//		RedisOpClient.get("xupiao", x -> {
//			System.out.println("get begin=======" + Thread.currentThread().getName());
//			if(x.succeeded()) {
//				System.out.println("get method=======" + Thread.currentThread().getName() + x.result());
//			} else {
//				System.out.println(x.cause());
//			}
//		});
//		System.out.println("get end=======" + Thread.currentThread().getName());
	}
}
