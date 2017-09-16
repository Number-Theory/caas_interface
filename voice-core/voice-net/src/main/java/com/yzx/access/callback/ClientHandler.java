package com.yzx.access.callback;

import io.netty.handler.codec.http.HttpResponse;

public interface ClientHandler {
	public void execute(HttpResponse response, String context);

	public void failed(final Exception ex);
}
