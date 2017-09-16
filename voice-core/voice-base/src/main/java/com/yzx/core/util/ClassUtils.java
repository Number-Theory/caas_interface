package com.yzx.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import com.yzx.core.util.OperateOfCache.CacheFunction;

/**
 * 
 * @author jxb
 * 
 */
public class ClassUtils {
	private static Class<?>[] SimpleClass = new Class<?>[] {
		Object.class,
		Class.class,
		Date.class,
		java.sql.Date.class,
		java.sql.Timestamp.class,
		String.class,
		Boolean.class,
		Character.class,
		Byte.class,
		Short.class,
		Integer.class,
		Long.class,
		Float.class,
		Double.class, 
		Void.class};
	public static boolean isSimpleClass(Class<?> c) {
		if (c.isPrimitive()) return true;
		for (Class<?> c1 : SimpleClass)
			if (c1 == c) return true;
		return false;
	}
	
	public static boolean isCollectionOrArray(Class<?> c) {
		if (c.isArray()) return true;
		if (Collection.class.isAssignableFrom(c)) return true;
		return false;
	}
	
	public static <A extends Annotation> List<A> getAnnotationsWithSuper(Class<?> clazz, Class<A> annotationClass) {
		List<A> ret = new ArrayList<A>();
		while (clazz != Object.class) {
			ret.add(clazz.getAnnotation(annotationClass));
			clazz = clazz.getSuperclass();
		}
		return ret;
	}
	
	public static Annotation[] getAnnotationsWithSuper(Class<?> clazz) {
		List<Annotation> ret = new ArrayList<Annotation>();
		while (clazz != Object.class) {
			ret.addAll(Arrays.asList(clazz.getAnnotations()));
			clazz = clazz.getSuperclass();
		}
		return ret.toArray(new Annotation[ret.size()]);
	}
	
	public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] types, Object[] args) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Method m = getDeclaredMethodFromSuper(clazz, methodName, types);
		m.setAccessible(true);
		return m.invoke(clazz, args);
	}
	
	public static Object invokePrivateMethod(Object instance, String methodName, Class<?>[] types, Object[] args) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Method m = getDeclaredMethodFromSuper(instance.getClass(), methodName, types);
		m.setAccessible(true);
		return m.invoke(instance, args);
	}
	/**
	 * 根据给定的属性名称和类型查找给定类中的setter方法，只会返回public方法
	 */
	public static Method findWriteMethodForAnyType(Class<?> clazz, String property, Class<?> propertyType) {
		String name = "set" + StringUtil.capitalFirst(property);
		for (Method m : clazz.getMethods()) {
			if (name.equals(m.getName()) && m.getParameterTypes().length == 1 &&
					propertyType.isAssignableFrom(m.getParameterTypes()[0]))
				return m;
		}
		return null;
	}
	/**
	 * 查找指定类中指定属性对应的getter方法，本方法除返回clazz本身声明的方法外，
	 * 还会返回clazz的父类或接口中声明的同名方法。
	 */
	public static Method[] findReadMethod(Class<?> clazz, String methodName) {
		List<Method> ret = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if (m.getParameterTypes().length == 0 && m.getName().equals(methodName))
				ret.add(m);
		}
		return ret.toArray(new Method[ret.size()]);
	}
	/**
	 * 得到private字段的值,不用通过get方法直接得到值
	 * @param obj
	 * @param name
	 * @return
	 */
	public static Object getPrivateFieldValue(Object obj, String name) {
		return getPrivateFieldValue(obj, name, false);
	}
	/**
	 * 得到private字段值,不用通过get方法直接得到值
	 * @param obj
	 * @param name
	 * @param throwException
	 * @return
	 */
	public static Object getPrivateFieldValue(Object obj, String name, boolean throwException) {
		try {
			Field f = getDeclaredFieldFromSuper(obj.getClass(), name);
			f.setAccessible(true);
			return f.get(obj);
		} catch (Exception e) {
			if (throwException)
				throw new RuntimeException(e);
			return null;
		}
	}
	/**
	 * 设置private字段的值,不用通过set方法直接设置值
	 * @param obj
	 * @param name
	 * @return
	 */
	public static void setPrivateFieldValue(Object obj, String name, Object value) {
		setPrivateFieldValue(obj, name, value, false);
	}
	/**
	 * 设置private字段值,不用通过set方法直接设置值
	 * @param obj
	 * @param name
	 * @param throwException
	 * @return
	 */
	public static void setPrivateFieldValue(Object obj, String name, Object value, boolean throwException) {
		try {
			Field f = getDeclaredFieldFromSuper(obj.getClass(), name);
			f.setAccessible(true);
			f.set(obj, value);
		} catch (Exception e) {
			if (throwException)
				throw new RuntimeException(e);
		}
	}
	/**
	 * 得到指定的class field
	 * @param clazz
	 * @param name
	 * @return Field
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	private static Field _getDeclaredFieldFromSuper(Class<?> clazz, String name) throws NoSuchFieldException, SecurityException {
		if (clazz == null) throw new NoSuchFieldException(name);
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			return _getDeclaredFieldFromSuper(clazz.getSuperclass(), name);
		}
	}
	
	/**
	 * 缓存方法，优化性能(zoujw)
	 */
	public static Field getDeclaredFieldFromSuper(final Class<?> clazz, final String name) throws NoSuchFieldException, SecurityException {
		CacheFunction<Field> cf = new OperateOfCache.CacheFunction<Field>() {
			@Override
			public String getKey() {
				return "DeclaredFieldFromSuper-" + clazz.toString() + name;
			}
			@Override
			public Field call() {
				try {
					return _getDeclaredFieldFromSuper(clazz, name);
				} catch (Exception e) {
					return null;
				} 
			}
			
		};
		Field field = OperateOfCache.execute(cf);
		if (null == field)
			throw new NoSuchFieldException(name);
		
		return field;
	}
	public static Method getDeclaredMethodFromSuper(Class<?> clazz, String name, Class<?>[] parameterTypes) throws NoSuchMethodException, SecurityException {
		if (clazz == null) throw new NoSuchMethodException(name);
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			return getDeclaredMethodFromSuper(clazz.getSuperclass(), name, parameterTypes);
		}
	}
	/**
	 * 得到去掉包后的class名称
	 * @param clazz
	 * @return
	 */
	public static String getShortClassName(Class<?> clazz) {
		return getShortClassName(clazz.getName());
	}
	/**
	 * 得到去掉包后的class名称
	 * @param name
	 * @return
	 */
	public static String getShortClassName(String name) {
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			name = name.substring(index + 1);
		}
		return name;
	}
	public static String getPackageName(String className) {
		int index = className.lastIndexOf(".");
		if (index >= 0) {
			return className.substring(0, index);
		}
		return "";
	}
	public static boolean classExist(String className) {
		try {
			Class.forName(className, true, Thread.currentThread().getContextClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	public static Class<?> classForName(String className) {
		try {
			return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static ClassLoader getDefaultClassLoader () {
		 return Thread.currentThread().getContextClassLoader();
	}
	
	
	public static Class<?> classForName(String className, String defaultClassName) {
		try {
			return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			try {
				return Class.forName(defaultClassName, true, Thread.currentThread().getContextClassLoader());
			} catch (Exception e2) {
				throw new IllegalArgumentException(e2);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 获取类中属性的泛型类型
	 * @param target 目标类
	 * @param property 目标属性
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@SuppressWarnings("unchecked")
	public static Class<Object> getObjectClass(Object target, String property) throws NoSuchFieldException, SecurityException {
		Field field = target.getClass().getDeclaredField(property);
		return (Class<Object>) getCollectionMemberType(field);
	}
	
	public static Class<?> getCollectionMemberType(Member m) {
		if (m instanceof Method) return getCollectionMemberType((Method)m);
		if (m instanceof Field) return getCollectionMemberType((Field)m);
		return null;
	}
	
	public static Class<?> getCollectionMemberType(Method m) {
		Type pt = m.getGenericReturnType();  
		if (pt instanceof ParameterizedType)
			return (Class<?>) getGenericClass((ParameterizedType) pt);
		else {
			// TODO: 处理CollectionMember
			// CollectionMember cm = m.getAnnotation(CollectionMember.class);
			return null;
		}
	}
	
	public static Class<?> getCollectionMemberType(Field m) {
		Type pt = m.getGenericType();  
		if (pt instanceof ParameterizedType)
			return (Class<?>) getGenericClass((ParameterizedType) pt);
		else {
			// TODO: 处理CollectionMember
			// CollectionMember cm = m.getAnnotation(CollectionMember.class);
			return null;
		}
	}
	
	/** 
     * 取得范性类型
     * 
     * @param cls 
     * @param i 
     * @return 
     */ 
    public static Class<?> getGenericClass(ParameterizedType pt) { 
        Object genericClass = pt.getActualTypeArguments()[0]; 
        if (genericClass instanceof ParameterizedType) { // 处理多级泛型 
            return (Class<?>) ((ParameterizedType) genericClass).getRawType(); 
        } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型 
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType(); 
        } else { 
            return (Class<?>) genericClass; 
        } 
    }
    
    /**
     * 获取类的泛型类型
     * @param cls
     * @return
     */
    public static Class<?> getGenericClass(Class<?> cls) {
		return  getGenericClass((ParameterizedType)cls.getClass().getGenericSuperclass());
		
    }
    
    /**
     * 获取指定方法的参数名称
     * @param clazz 目标类
     * @param methodName 目标方法名
     * @return
     */
    public static String[] getParamNames(Class<?> clazz, String methodName) {
		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass cc = pool.get(clazz.getName());
			CtMethod cm = cc.getDeclaredMethod(methodName);
			// 使用javaassist的反射方法获取方法的参数名
			MethodInfo methodInfo = cm.getMethodInfo();
			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
			if (attr == null) {
				// exception
			}
			String[] paramNames = new String[cm.getParameterTypes().length];
			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
			for (int i = 0; i < paramNames.length; i++)
				paramNames[i] = attr.variableName(i + pos);
			return paramNames;
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("获取类[" + clazz.getName() + "]中的方法[" + methodName + "]的参数错误：" + e.getMessage(),e);
		}
	}
    
    /**
     * 取类的名称，这个名称用于代码生成。
     * @param name
     * @return
     */
    public static String getClassName(Class<?> name) {
    	return name.getName().replaceAll("\\$", ".");
    }

	public static boolean isSetterMethod(Method method) {
		return !Modifier.isFinal(method.getModifiers()) &&
				method.getParameterTypes().length == 1 && 
				method.getReturnType() == void.class
				&& method.getName().startsWith("set");
	}

	public static boolean isGetterMethod(Method method) {
		return !Modifier.isFinal(method.getModifiers()) &&
				method.getParameterTypes().length == 0 && 
				(method.getName().startsWith("get") || 
						method.getName().startsWith("is"));
	}
}
