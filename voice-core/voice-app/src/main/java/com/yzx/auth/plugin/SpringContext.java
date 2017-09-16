package com.yzx.auth.plugin;

import org.springframework.context.ApplicationContext;

public class SpringContext {
	public static ApplicationContext getApplicationContext() {
		return SpringPlugin.context;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(String bean){
		return (T) getApplicationContext().getBean(bean);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<?> bean){
		return (T) getApplicationContext().getBean(bean);
	}
}
