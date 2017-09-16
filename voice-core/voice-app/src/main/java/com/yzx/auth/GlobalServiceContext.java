package com.yzx.auth;

/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class GlobalServiceContext {
	private static GlobalServiceContext instance = new GlobalServiceContext();
	private volatile SystemStatus status = SystemStatus.stopped;
	public static boolean test = false;// 代码当前是在测试环境

	public static GlobalServiceContext get() {
		return instance;
	}

	public SystemStatus getStatus() {
		return status;
	}

	public synchronized void setStatus(SystemStatus status) {
		this.status = status;
	}

	/**
	 * 系统状态
	 *
	 */
	public static enum SystemStatus {
		failed("系统状态(SystemStatus):系统异常"), stopping("系统状态(SystemStatus):正在停止"), stopped("系统状态(SystemStatus):已经停止"), starting(
				"系统状态(SystemStatus):正在启动"), started("系统状态(SystemStatus):已在启动"), ide("IDE运行模式"), ide_debug(
				"IDE-Debug调试模式，事物自动回滚");

		private String name;

		SystemStatus(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		/**
		 * IDE中运行部需要执行的，统一从这里判断
		 * 
		 * @return
		 */
		public boolean isRunWithIDE() {
			if (this == SystemStatus.ide || this == SystemStatus.ide_debug)
				return true;
			else
				return false;
		}
	}

}
