/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yzx.auth.start;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 服务启动入口
 * 
 * <p>
 * 支持通过app.classpath或环境变量APP_CLASSPATH使用通配符的方式设置类路径。通配符规则如下：
 * <ul>
 * <li>通配符只支持*，foo\*匹配foo目录下所有子目录；foo\*\classes匹配foo目录下所有子目录内的classes子目录</li>
 * <li>不支持部分匹配，即foo/ln-*</li>
 * <li>不支持直接以*开头的类路径，如*\classes</li>
 * <li>通配符只支持匹配目录，不支持匹配jar文件</li>
 * <li>通配符不支持子目录递归匹配；</li>
 * <li>使用通配符匹配的类路径，其顺序是不确定的。应用程序不应该依赖于该顺序；</li>
 * </ul>
 * 通配符解析成功后会将实际的类路径设置到系统属性app.class.path中。
 * 
 * @author xupiao 2017年6月1日
 *
 */

public final class Bootstrap {

	// -------------------------------------------------------------- Constants

	protected static final String APP_HOME_TOKEN = "${app.home}";
	protected static final String APP_BASE_TOKEN = "${app.base}";
	protected static final String APP_VMID_TOKEN = "${app.vmid}";
	private static final String START_CLAZZ = "com.yzx.auth.start.Startup";
	// ------------------------------------------------------- Static Variables

	/**
	 * Daemon object used by main.
	 */
	private static Bootstrap daemon = null;

	/**
	 * Daemon reference.
	 */
	private Object appDaemon = null;

	protected ClassLoader commonLoader = null;
	protected ClassLoader appLoader = null;

	public static final String CommonLoaderName = "common";
	public static final String AppLoaderName = "app";

	/**
	 * Print usage information for this application.
	 */
	protected void usage() {

		System.out.println("usage: java com.yzx.auth.start.Bootstrap" + " [ -config {pathname} ] "
				+ " [ -nonaming ] { start | stop | startd | stopd}");

	}

	protected static ClassLoader initAppClassLoader() {
		String value = System.getProperty("app.classpath");
		if (value == null || "".equals(value))
			value = System.getenv("APP_CLASSPATH");
		if (value != null && !"".equals(value)) {
			value = convertClassPathElement(value);
			List<URL> urls = new ArrayList<URL>();
			List<String> app = new ArrayList<String>();
			for (String v : value.split(File.pathSeparator)) {
				if (v == null || v.trim().length() == 0)
					continue;
				List<String> cps = expendPatternClassPath(v, true);
				for (String cp : cps) {
					try {
						urls.add(new File(cp).toURI().toURL());
						app.add(cp);
					} catch (MalformedURLException e) {
						log("Classpath error[" + cp + "]: " + e.getMessage());
					}
				}
			}
			if (urls.size() > 0) {
				System.setProperty("app.class.path", app.toString());
				ClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), Bootstrap.class.getClassLoader());
				return loader;
			}
		}
		return Bootstrap.class.getClassLoader();
	}

	protected static String convertClassPathElement(String e) {
		String repository = e;

		// Local repository
		if (repository.startsWith(APP_HOME_TOKEN)) {
			repository = getAppHome() + repository.substring(APP_HOME_TOKEN.length());
		} else if (repository.startsWith(APP_BASE_TOKEN)) {
			repository = getAppBase() + repository.substring(APP_BASE_TOKEN.length());
		}

		// ${app.vmid}替换
		String vmid = System.getProperty("app.vmid", "");
		if (vmid.length() > 0) {
			String str = repository;
			int idx = str.indexOf(APP_VMID_TOKEN);
			if (idx >= 0) {
				repository = str.substring(0, idx);
				repository = repository + vmid;
				repository = repository + str.substring(idx + APP_VMID_TOKEN.length());
			}
		}

		return repository;
	}

	protected static List<String> _expendPatternClassPath(String patternPath, boolean checkExist) {
		List<String> ret = new ArrayList<String>();
		patternPath = patternPath.replace("\\", "/");

		String wildcardPart = "/**";
		int patternIndex = patternPath.indexOf(wildcardPart);
		if (patternIndex != -1) {
			String rootDir = patternPath.substring(0, patternIndex);
			File rootDirFile = new File(rootDir);
			if (!rootDirFile.exists() || !rootDirFile.isDirectory() || !rootDirFile.canRead())
				return ret;

			patternPath = patternPath.replace("**", ".*");
			// 递归处理
			collect(rootDirFile, patternPath, ret);
		}
		return ret;
	}

	private static void collect(File folder, String regex, List<String> cols) {
		for (File subFile : folder.listFiles()) {
			String p = subFile.getPath().replace("\\", "/");
			if (Pattern.matches(regex, p)) {
				cols.add(p);
				continue;
			}
			if (subFile.isDirectory())
				collect(subFile, regex, cols);
		}
	}

	/**
	 * 将带有通配符的类路径展开为实际的类路径。
	 * 
	 * @param element
	 * @return
	 */
	protected static List<String> expendPatternClassPath(String patternPath, boolean checkExist) {
		if (patternPath.contains("**"))
			return _expendPatternClassPath(patternPath, checkExist);
		List<String> ret = new ArrayList<String>();
		if ('/' != File.separatorChar)
			patternPath = patternPath.replace('/', File.separatorChar);
		if ('\\' != File.separatorChar)
			patternPath = patternPath.replace('\\', File.separatorChar);

		String wildcardPart = File.separator + "*";
		int patternIndex = patternPath.indexOf(wildcardPart);
		if (patternIndex == -1) {
			File file = new File(patternPath);
			if (!checkExist || file.exists())
				ret.add(file.getPath());
		} else {
			String rootDir = patternPath.substring(0, patternIndex);
			String subPattern = patternPath.substring(patternIndex + wildcardPart.length());
			File rootDirFile = new File(rootDir);
			if (!rootDirFile.exists() || !rootDirFile.isDirectory() || !rootDirFile.canRead())
				return ret;
			for (File file : rootDirFile.listFiles()) {
				File cp = new File(file.getPath() + subPattern);
				if (!checkExist || cp.exists())
					ret.add(cp.getPath());
			}
		}
		return ret;
	}

	/**
	 * Initialize daemon.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		// Set APP path
		setAppHome();
		setAppBase();

		// 下面方法创建的类加载器是子类优先的代理模式：即优先在本身查找类，找不到才到父加载器中查找
		// initClassLoaders();
		// 如下方法创建的是java中默认的双亲优先的代理模式：即总是先到父加载器中查找，找不到才到本身查找
		// 如下代码同时也取消了架构类和应用类加载分离的模式，是为了尽量减少ClassNotFound异常
		// （说明我们还没有完全搞清楚java的类加载机制）
		commonLoader = appLoader = initAppClassLoader();

		Thread.currentThread().setContextClassLoader(appLoader);

		// 初始化log4j
		initLog4j();

		Class<?> startupClass = appLoader.loadClass(START_CLAZZ);

		Object startupInstance = startupClass.newInstance();

		String methodName = "setParentClassLoader";
		Class<?> paramTypes[] = new Class[1];
		paramTypes[0] = Class.forName("java.lang.ClassLoader");
		Object paramValues[] = new Object[1];
		paramValues[0] = commonLoader;
		Method method = startupInstance.getClass().getMethod(methodName, paramTypes);
		method.invoke(startupInstance, paramValues);

		appDaemon = startupInstance;

	}

	// ----------------------------------------------------------- Main Program

	/**
	 * Load the APP daemon.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public void init(String[] arguments) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		init();
	}

	/**
	 * Start the app daemon.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public void start() throws NoSuchMethodException, SecurityException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (appDaemon == null)
			init();

		Method method = appDaemon.getClass().getMethod("start", (Class<?>[]) null);
		method.invoke(appDaemon, (Object[]) null);
	}

	/**
	 * Stop the app Daemon.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void stop() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Method method = appDaemon.getClass().getMethod("stop", (Class<?>[]) null);
		method.invoke(appDaemon, (Object[]) null);
	}

	/**
	 * Stop the standlone server.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void stopServer() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Method method = appDaemon.getClass().getMethod("stopServer", (Class<?>[]) null);
		method.invoke(appDaemon, (Object[]) null);

	}

	/**
	 * Stop the standlone server.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void stopServer(String[] arguments) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Object param[];
		Class<?> paramTypes[];
		if (arguments == null || arguments.length == 0) {
			paramTypes = null;
			param = null;
		} else {
			paramTypes = new Class[1];
			paramTypes[0] = arguments.getClass();
			param = new Object[1];
			param[0] = arguments;
		}
		Method method = appDaemon.getClass().getMethod("stopServer", paramTypes);
		method.invoke(appDaemon, param);

	}

	/**
	 * 设置(启动完成后)是否要等待直到接收到关闭指令并关闭系统.
	 * 
	 * @param await
	 *            true (启动完成后)等待直到接收到关闭指令并关闭系统(在独立JVM中启动的场景). false
	 *            (启动完成后)不等待关闭指令且不关闭系统(在容器中启动的场景).
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void setAwait(boolean await) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		Class<?> paramTypes[] = new Class[1];
		paramTypes[0] = Boolean.TYPE;
		Object paramValues[] = new Object[1];
		paramValues[0] = new Boolean(await);
		Method method = appDaemon.getClass().getMethod("setAwait", paramTypes);
		method.invoke(appDaemon, paramValues);

	}

	public boolean getAwait() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<?> paramTypes[] = new Class[0];
		Object paramValues[] = new Object[0];
		Method method = appDaemon.getClass().getMethod("getAwait", paramTypes);
		Boolean b = (Boolean) method.invoke(appDaemon, paramValues);
		return b.booleanValue();
	}

	/**
	 * Destroy the Daemon.
	 */
	public void destroy() {

		// FIXME

	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            Command line arguments to be processed
	 */
	public static void main(String[] args) {

		if (daemon == null) {
			daemon = new Bootstrap();
			try {
				daemon.init();
			} catch (Throwable t) {
				t.printStackTrace();
				return;
			}
		}

		try {
			String command = "start";
			if (args.length > 0) {
				command = args[args.length - 1];
			}

			// stop系列命令都没用，直接通过命令行kill -TERM
			// 触发JVM进程调用shutdownHook来关闭服务。
			if (command.equals("startd")) {
				args[0] = "start";
				daemon.start();
			} else if (command.equals("stopd")) {
				args[0] = "stop";
				daemon.stop();
			} else if (command.equals("start")) {
				daemon.setAwait(true);
				daemon.start();
			} else if (command.equals("stop")) {
				// daemon.stopServer(args);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public void setAppHome(String s) {
		System.setProperty("app.home", s);
	}

	public void setAppBase(String s) {
		System.setProperty("app.base", s);
	}

	/**
	 * Set the <code>app.base</code> System property to the current working
	 * directory if it has not been set.
	 */
	private void setAppBase() {

		if (System.getProperty("app.base") != null)
			return;
		if (System.getProperty("app.home") != null)
			System.setProperty("app.base", System.getProperty("app.home"));
		else
			System.setProperty("app.base", System.getProperty("user.dir"));

	}

	/**
	 * Set the <code>app.home</code> System property to the current working
	 * directory if it has not been set.
	 */
	private void setAppHome() {

		if (System.getProperty("app.home") != null)
			return;
		File bootstrapJar = new File(System.getProperty("user.dir"), "bootstrap.jar");
		if (bootstrapJar.exists()) {
			try {
				System.setProperty("app.home", (new File(System.getProperty("user.dir"), "..")).getCanonicalPath());
			} catch (Exception e) {
				// Ignore
				System.setProperty("app.home", System.getProperty("user.dir"));
			}
		} else {
			System.setProperty("app.home", System.getProperty("user.dir"));
		}

	}

	/**
	 * Get the value of the app.home environment variable.
	 */
	public static String getAppHome() {
		return System.getProperty("app.home", System.getProperty("user.dir"));
	}

	/**
	 * Get the value of the app.base environment variable.
	 */
	public static String getAppBase() {
		return System.getProperty("app.base", getAppHome());
	}

	private static final String APP_LOG = "app_log";

	/**
	 * 初始化Log4j。按如下顺序查找log4j配置文件： 1、文件app_log_${app.vmid}.xml； 2、文件app_log.xml;
	 * 3、文件app_log_default.xml;
	 * 
	 * log4j配置文件中可以出现${app.vmid}和${app_home}变量，以动态决定日志文件位置
	 * 
	 */
	private void initLog4j() {

		System.setProperty("app.log.level", "DEBUG");

		// 如果环境变量中传入了日志文件名，则直接使用环境变量中的。
		String logFile = System.getProperty("log4j.configurationFile");
		if (logFile != null && logFile.trim().length() != 0) {
			System.out.println("log config file:" + logFile);
			return;
		}

		List<String> filenames = new ArrayList<String>(Arrays.asList(APP_LOG + ".xml", APP_LOG + "_default.xml"));

		String vmid = System.getProperty("app.vmid");
		if (vmid != null)
			filenames.addAll(0, Arrays.asList(APP_LOG + "_" + vmid + ".xml"));
		for (String filename : filenames) {
			URL url = appLoader.getResource(filename);
			if (url != null) {
				System.setProperty("log4j.configurationFile", filename);
				System.out.println("log config file:" + filename);
				return;
			}
		}
	}

	/**
	 * Log a debugging detail message.
	 * 
	 * @param message
	 *            The message to be logged
	 */
	protected static void log(String message) {

		System.out.print("Bootstrap: ");
		System.out.println(message);

	}

	/**
	 * Log a debugging detail message with an exception.
	 * 
	 * @param message
	 *            The message to be logged
	 * @param exception
	 *            The exception to be logged
	 */
	protected static void log(String message, Throwable exception) {

		log(message);
		exception.printStackTrace(System.out);

	}

}
