package com.yzx.access.url;

import java.util.Date;

import com.yzx.access.client.AbstractFutureCallback;
import com.yzx.access.client.HttpClientUtil;
import com.yzx.core.util.DateUtil;

public final class Client {

	public static void main(String[] argv) {
		for (int j = 0; j < 10; j++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					int i = 0;
					while (i++ < 500) {
						HttpClientUtil.get().httpGet("http://172.16.1.235:1111/test?i=" + i, new A());
					}
				}
			}).start();
		}
	}

	static class A implements AbstractFutureCallback {

		public A() {
			super();
		}

		@Override
		public void execute(String context) {
			try {
				System.out.println(DateUtil.getNow("yyyy-MM-dd HH:mm:ss:SSS") + " " + Thread.currentThread().getName()
						+ " " + context);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void failed(Exception ex) {
			// TODO Auto-generated method stub

		}

	}

}