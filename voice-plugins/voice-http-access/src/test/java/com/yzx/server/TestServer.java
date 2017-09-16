package com.yzx.server;

import com.yzx.auth.start.Bootstrap;

public class TestServer {
	public static void main(String[] args) throws InterruptedException {
		Bootstrap.main(new String[] { "startd" });
		
		Thread.sleep(Integer.MAX_VALUE);
	}
}
