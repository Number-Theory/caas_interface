package com.yzx.core.config;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.Modifier;

import com.yzx.core.util.OperateOfCache;
import com.yzx.core.util.OperateOfCache.CacheFunction;
import com.yzx.core.util.StringUtil;

/**
 * 
 * @author xupiao 2017年6月12日
 *
 */
public class PropertyUtil {
	public static class MyPropertyDescriptor {
		private Method getter, setter;
		private String property;
		private Class<?> propertyType;

		public MyPropertyDescriptor(String property, Class<?> propertyType, Method getter, Method setter) {
			this.getter = getter;
			this.propertyType = propertyType;
			this.property = property;
			this.setter = setter;
		}

		public Method getReadMethod() {
			return getter;
		}

		public Method getWriteMethod() {
			return setter;
		}

		public Class<?> getPropertyType() {
			return propertyType;
		}

		public String getProperty() {
			return property;
		}
	}

	/**
	 * 查找类中的所有属性描述
	 * 
	 * @param clazz
	 * @return
	 */
	public static MyPropertyDescriptor[] getMyPropertyDescriptors(Class<?> clazz) {
		List<MyPropertyDescriptor> pd = null;
		try {
			PropertyDescriptor[] ps = java.beans.Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			pd = new ArrayList<MyPropertyDescriptor>();
			for (int i = 0; i < ps.length; i++) {
				// TODO: ps[i].getPropertyType()可能为null
				if (ps[i].getPropertyType() == null || ps[i].getName() == null)
					continue;
				if (ps[i].getPropertyType() != null && ps[i].getPropertyType().isArray()
						&& ps[i].getPropertyType().getComponentType() == net.sf.cglib.proxy.Callback.class)
					continue;
				Method setter = ps[i].getWriteMethod();
				if (setter == null && ps[i].getReadMethod() != null) {
					setter = findWriteMethodForAnyType(clazz, ps[i].getName(), ps[i].getReadMethod().getReturnType());
				}
				pd.add(new MyPropertyDescriptor(ps[i].getName(), ps[i].getPropertyType(), ps[i].getReadMethod(), setter));
			}
		} catch (IntrospectionException e) {
			throw new IllegalArgumentException(e);
		}
		return pd.toArray(new MyPropertyDescriptor[0]);
	}

	public static MyPropertyDescriptor getMyPropertyDescriptor(final Class<?> clazz, final String property) {
		CacheFunction<MyPropertyDescriptor> cf = new OperateOfCache.CacheFunction<MyPropertyDescriptor>() {
			@Override
			public String getKey() {
				return "getMyPropertyDescriptor-" + clazz.getName() + property;
			}

			@Override
			public MyPropertyDescriptor call() {
				return _getMyPropertyDescriptor(clazz, property);
			}
		};
		return OperateOfCache.execute(cf);
	}

	/**
	 * 使用getPropertyDescriptor方法取不到setter方法时，可用本方法
	 */
	private static MyPropertyDescriptor _getMyPropertyDescriptor(Class<?> clazz, String property) {
		try {
			PropertyDescriptor[] ps = java.beans.Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for (PropertyDescriptor p : ps) {
				if (property.equals(p.getName())) {
					Method setter = p.getWriteMethod();
					if (setter == null && p.getReadMethod() != null) {
						setter = findWriteMethodForAnyType(clazz, property, p.getReadMethod().getReturnType());
					}
					return new MyPropertyDescriptor(property, p.getPropertyType(), p.getReadMethod(), setter);
				}
			}
		} catch (IntrospectionException e) {
			throw new IllegalArgumentException(e);
		}
		return null;
	}

	/**
	 * 得到属性描述 PropertyDescriptor(PropertyDescriptor主要用于得到属性的get/set方法)。
	 * 当属性的setter方法的参数类型与getter方法的返回类型不一致时，本方法将无法获取对应的setter方法。当getter
	 * 方法实现的是接口中的方法时，即便本类的getter方法与setter方法类型一致，若setter方法类型与接口中声明的
	 * getter方法的类型不致时，也同样取不到setter方法。
	 * 
	 * @see {@link #getMyPropertyDescriptor(Class, String)}
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		try {
			PropertyDescriptor[] ps = java.beans.Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for (PropertyDescriptor p : ps) {
				if (property.equals(p.getName()))
					return p;
			}
		} catch (IntrospectionException e) {
			throw new IllegalArgumentException(e);
		}
		return null;
	}

	/**
	 * 根据给定的属性名称和类型查找给定类中的setter方法，只会返回public方法
	 */
	// TODO:
	private static Method findWriteMethodForAnyType(Class<?> clazz, String property, Class<?> propertyType) {
		String name = "set" + StringUtil.capitalFirst(property);
		for (Method m : clazz.getMethods()) {
			if (name.equals(m.getName()) && m.getParameterTypes().length == 1
					&& propertyType.isAssignableFrom(m.getParameterTypes()[0]))
				return m;
		}
		return null;
	}

	/**
	 * 获取方法对应的属性，属性通常与字段名对应。
	 * 
	 * @param method
	 *            方法对象
	 * @return 如果方法不是getter或setter方法则返回null。
	 */
	public static String getProperty(Method method) {
		if (!Modifier.isFinal(method.getModifiers())) {
			String property = method.getName();
			if (property.startsWith("is"))
				return StringUtil.uncapitalFirst(property.substring(2));
			else if (property.startsWith("get") || property.startsWith("set"))
				return StringUtil.uncapitalFirst(property.substring(3));
		}
		return null;
	}
}
