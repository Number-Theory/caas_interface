package com.yzx.access.client;

public interface AbstractFutureCallback {
	public void execute(String context);
	
	public void failed(final Exception ex);
}
