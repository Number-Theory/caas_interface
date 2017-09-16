package com.yzx.core.queue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class NettyWorkQueue {
	public static Queue<BaseQueueModel> queue = new LinkedBlockingQueue<BaseQueueModel>();
}
