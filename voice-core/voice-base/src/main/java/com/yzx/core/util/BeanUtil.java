package com.yzx.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Bean 通用工具类
 * 
 * 
 */
public class BeanUtil {
    // debug --------------------
    static final int dbg = 0;


    /**
     * 获取一个空的对象
     */
    public static final Object emptyObject = new Object();

    public static interface AttributeHolder {

        public void setAttribute(String key, Object o);

    }

    public static interface PropertySource {

        public String getProperty(String key);

    }

    /**
     * 设置对象中的属性值.
     * <p>
     * 即利用对象中的setter方法设置值.
     * 
     * @param bean 对象
     * @param name 属性名称
     * @param value 属性值
     */
    public static void setProperty(Object bean, String name, Object value) {
        if (bean == null || name == null)
            throw new IllegalArgumentException("bean and properyty name can not be null");
        String setter = "set" + StringUtil.capitalize(name);

        try {
            Class<? extends Object> valueType = (value == null ? null : value.getClass());
            Method methods[] = findMethods(bean.getClass());
            for (int i = 0; i < methods.length; i++) {
                Class<?> paramT[] = methods[i].getParameterTypes();
                if (paramT.length != 1)
                    continue;
                if (!setter.equals(methods[i].getName()))
                    continue;
                if (value == null) {
                    // value==null时，匹配第一个找到的方法
                    methods[i].invoke(bean, new Object[] { null });
                    return;
                }
                // 自动类型转换
                // 第一个匹配的类型，如果有多个方法的形参是继承的类呢？
                if (paramT[0].isAssignableFrom(valueType)) {
                    methods[i].invoke(bean, new Object[] { value });
                    return;
                } else if (paramT[0] == String.class) {
                    methods[i].invoke(bean, new Object[] { value.toString() });
                    return;
                } else if (paramT[0] == Boolean.class) {
                    methods[i].invoke(bean, new Object[] { new Boolean(value.toString()) });
                    return;
                } else if (paramT[0] == Integer.class) {
                    methods[i].invoke(bean, new Object[] { new Integer(value.toString()) });
                    return;
                } else if (paramT[0] == Long.class) {
                    methods[i].invoke(bean, new Object[] { new Long(value.toString()) });
                    return;
                } else if (paramT[0] == BigDecimal.class) {
                    methods[i].invoke(bean, new Object[] { new BigDecimal(value.toString()) });
                    return;
                } else if (paramT[0] == boolean.class) {
                    methods[i].invoke(bean, new Object[] { new Boolean(value.toString()) });
                    return;
                } else if (paramT[0] == int.class) {
                    methods[i].invoke(bean, new Object[] { new Integer(value.toString()) });
                    return;
                }
                LangUtil.println("paramT=" + paramT[0].getName());

            }
            throw new IllegalArgumentException("there is no setter for property:" + valueType.getName() + " " + name);

        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae);
        } catch (InvocationTargetException ie) {
            throw new IllegalArgumentException(ie);
        }
    }

    /**
     * 取对象中的属性值. <br/>
     * 即利用对象中的getter方法(getFoo or isFoo)取值.
     * 
     * @param bean 对象
     * @param name 属性名称
     * @return 值
     */
    public static Object getProperty(Object bean, String name) {
        if (bean == null || name == null)
            return null;
        String getter = "get" + StringUtil.capitalize(name);
        String boolgetter = "is" + StringUtil.capitalize(name);

        try {
            Method methods[] = findMethods(bean.getClass());

            // First, the ideal case - a getFoo() or isFoo method
            for (int i = 0; i < methods.length; i++) {
                Class<?> paramT[] = methods[i].getParameterTypes();
                if ((getter.equals(methods[i].getName()) || boolgetter.equals(methods[i].getName()))
                        && paramT.length == 0)
                    return methods[i].invoke(bean, (Object[]) null);
            }
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae);
        } catch (InvocationTargetException ie) {
            throw new IllegalArgumentException(ie);
        }
        return null;
    }

    /**
     * Return the entire set of properties for which the specified bean provides a getter method
     * 
     * @param bean
     * @return
     */
    public static Map<String, Object> describe(Object bean) {
        Map<String, Object> ret = new HashMap<String, Object>();
        if (bean == null)
            return ret;
        String name;
        try {
            Method methods[] = findMethods(bean.getClass());
            for (int i = 0; i < methods.length; i++) {
                Class<?> paramT[] = methods[i].getParameterTypes();
                if (paramT.length != 0)
                    continue;
                if (methods[i].getName().startsWith("get")
                        && methods[i].getName().length() > 3) {
                    name = StringUtil.unCapitalize(methods[i].getName().substring(3));
                    ret.put(name, methods[i].invoke(bean, (Object[]) null));
                } else if (methods[i].getName().startsWith("is")
                        && methods[i].getName().length() > 2) {
                    name = StringUtil.unCapitalize(methods[i].getName().substring(2));
                    ret.put(name, methods[i].invoke(bean, (Object[]) null));
                }
            }
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae);
        } catch (InvocationTargetException ie) {
            throw new IllegalArgumentException(ie);
        }
        return ret;
    }

    /**
     * 通过对象中的setAttribute方法设置值.
     * Call void setAttribute( String ,Object )
     * 
     * @param bean 对象
     * @param name 属性名称
     * @param value 值
     */
    public static void setAttribute(Object bean, String name, Object value, Class<?> valueType) {
        if (bean == null || name == null)
            return;
        if (bean instanceof AttributeHolder) {
            ((AttributeHolder) bean).setAttribute(name, value);
            return;
        }

        Method executeM = null;
        Class<?> params[] = new Class[2];
        params[0] = String.class;
        params[1] = valueType;
        if (value != null && valueType == null)
            params[1] = value.getClass();
        else if (value == null && valueType == null) {
            throw new IllegalArgumentException("parameter with null value must provide types");
        }
        executeM = findMethod(bean.getClass(), "setAttribute", params, true);
        if (executeM == null) {
            throw new IllegalArgumentException("No setAttribute in " + bean.getClass());
        }
        try {
            executeM.invoke(bean, new Object[] { name, value });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return;
    }

    /**
     * 通过对象中的getAttribute方法取值.
     * Call Object getAttribute( String )
     * 
     * @param bean 对象
     * @param name 属性名称
     * @return 值
     */
    public static Object getAttribute(Object bean, String name) {
        if (bean == null || name == null)
            return null;
        Method executeM = null;
        Class<?> params[] = new Class[1];
        params[0] = String.class;
        executeM = findMethod(bean.getClass(), "getAttribute", params, false);
        if (executeM == null) {
            return null;
        }
        try {
            return executeM.invoke(bean, new Object[] { name });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static final Hashtable<Class<?>, Method[]> objectMethods = new Hashtable<Class<?>, Method[]>();// Cache

    /**
     * 找出类中所有的方法
     * 
     * @param c Class
     * @return
     */
    public static Method[] findMethods(Class<?> c) {
        Method methods[] = (Method[]) objectMethods.get(c);
        if (methods != null)
            return methods;

        methods = c.getMethods();
        objectMethods.put(c, methods);
        return methods;
    }

    /**
     * 在类中按名称参数查找方法.
     * 参数匹配时，如果提供的参数是方法形参的子类，也确定为匹配
     * 
     * @param c Class
     * @param name 方法名称
     * @param params 方法参数类数组
     * @param matchAssignableParam 是否:在参数匹配时，如果提供的参数是方法形参的子类，也确定为匹配
     * @return
     */
    public static Method findMethod(Class<?> c, String name, Class<?> params[], boolean matchAssignableParam) {
        Method methods[] = findMethods(c);
        if (methods == null)
            return null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(name)) {
                Class<?> methodParams[] = methods[i].getParameterTypes();
                if (methodParams == null || methodParams.length == 0)
                    if (params == null || params.length == 0)
                        return methods[i];
                if (methodParams == null || params == null
                        || params.length != methodParams.length)
                    continue;
                boolean found = true;
                for (int j = 0; j < params.length; j++) {
                    if (params[j] != methodParams[j]
                            && !(matchAssignableParam && methodParams[j].isAssignableFrom(params[j]))) {
                        found = false;
                        break;
                    }
                }
                if (found)
                    return methods[i];
            }
        }
        return null;
    }

    /**
     * Test if the object implements a particular method
     * 
     * @param obj
     * @param methodN
     * @return
     */
    public static boolean hasHook(Object obj, String methodN) {
        try {
            Method myMethods[] = findMethods(obj.getClass());
            for (int i = 0; i < myMethods.length; i++) {
                if (methodN.equals(myMethods[i].getName())) {
                    // check if it's overriden
                    Class<?> declaring = myMethods[i].getDeclaringClass();
                    Class<?> parentOfDeclaring = declaring.getSuperclass();
                    // this works only if the base class doesn't extend
                    // another class.

                    // if the method is declared in a top level class
                    // like BaseInterceptor parent is Object, otherwise
                    // parent is BaseInterceptor or an intermediate class
                    if (!"java.lang.Object".
                            equals(parentOfDeclaring.getName())) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 调用方法(参数类型用字符串数组提供)
     * 
     * @param bean 对象
     * @param methodN 方法名称
     * @param params 参数值
     * @param paramsType 参数类型名称
     * @param cl ClassLoader，=null时，缺省使用当前线程上下文的ClassLoader
     * @return
     */
    public static Object callMethod(Object bean,
            String methodN,
            Object[] params,
            String[] paramsType,
            ClassLoader cl) {
        if (bean == null || methodN == null) {
            throw new IllegalArgumentException("Illegal params " + bean + "." + methodN + "(" + params + ")");
        }
        if (cl == null)
            cl = Thread.currentThread().getContextClassLoader();
        Class<?> paramsT[] = null;
        if (paramsType == null)
            return callMethod(bean, methodN, params, paramsT);
        else {
            paramsT = new Class[paramsType.length];
            for (int i = 0; i < paramsT.length; i++) {
                try {
                    paramsT[i] = cl.loadClass(paramsType[i]);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return callMethod(bean, methodN, new Object[] { params }, paramsT);
    }

    private static final Object[] emptyObjArray = new Object[] {};// 空对象数组
    private static final Class<?>[] emptyClassArray = new Class[] {};// 空数数组

    /**
     * 调用方法
     * 
     * @param bean 对象
     * @param methodN 方法名称
     * @param params 参数值数组
     * @param paramsType 参数类型数组
     * @return
     */
    public static Object callMethod(Object bean, String methodN,
            Object params[], Class<?> paramsType[]) {
        if (bean == null || methodN == null)
            throw new IllegalArgumentException("Illegal params " + bean + "." + methodN);
        if (params == null) {
            params = emptyObjArray;
        }
        Method m = null;
        if (paramsType == null && params.length > 0) {
            paramsType = new Class[params.length];
            for (int i = 0; i < paramsType.length; i++) {
                if (params[i] == null)
                    throw new IllegalArgumentException("parameter with null value must provide types");
                else
                    paramsType[i] = params[i].getClass();
            }
        }
        if (paramsType == null)
            paramsType = emptyClassArray;
        m = findMethod(bean.getClass(), methodN, paramsType, true);
        if (m == null)
            throw new IllegalArgumentException("Method Not Found." + bean.getClass().getName() + " " + methodN);

        try {
            return m.invoke(bean, params);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
