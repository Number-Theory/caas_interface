package com.yzx.auth.start;

/**
 * 通过启动引导程序加载的接口
 * 
 * @author xupiao 2017年6月1日
 *
 */
public interface Bootable {

	/**
	 * 设置上级ClassLoader
	 * 
	 * @param parentClassLoader
	 */
	public void setParentClassLoader(ClassLoader parentClassLoader);

	/**
	 * 启动系统
	 */
	public void start();

	/**
	 * 关闭系统
	 */
	public void stop();

	/**
	 * 发起停止服务. 向另一JVM中运行的系统发出关闭系统指令
	 */
	public void stopServer();

	/**
	 * 发起停止服务. 向另一JVM中运行的系统发出关闭系统指令
	 * 
	 * @param arguments
	 */
	public void stopServer(String[] arguments);

	/**
	 * 设置(启动完成后)是否要等待直到接收到关闭指令并关闭系统.
	 * 
	 * @param await
	 *            true (启动完成后)等待直到接收到关闭指令并关闭系统(在独立JVM中启动的场景). false
	 *            (启动完成后)不等待关闭指令且不关闭系统(在容器中启动的场景).
	 */
	public void setAwait(boolean await);

	/**
	 * (启动完成后)是否要等待直到接收到关闭指令并关闭系统.
	 * 
	 * @return true (启动完成后)等待直到接收到关闭指令并关闭系统(在独立JVM中启动的场景). false
	 *         (启动完成后)不等待关闭指令且不关闭系统(在容器中启动的场景).
	 */
	public boolean getAwait();

}