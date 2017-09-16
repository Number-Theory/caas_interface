package com.yzx.access;

public class NettyTimeoutException extends RuntimeException {
	private static final long serialVersionUID = 8835912709645454109L;

	public NettyTimeoutException(String message) {
        super(message);
    }
}
