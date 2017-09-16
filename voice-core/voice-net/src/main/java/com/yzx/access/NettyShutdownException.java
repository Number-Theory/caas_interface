package com.yzx.access;

/**
 * Net异常父类
 * 
 * @author zhangms
 *
 */
public class NettyShutdownException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public NettyShutdownException(String message){
    	super( message);
    }
    
    public NettyShutdownException(String message, Throwable cause) {
    	super(message, cause);
    }
    
    public NettyShutdownException(Throwable cause) {
    	super(cause);
    }
    
}
