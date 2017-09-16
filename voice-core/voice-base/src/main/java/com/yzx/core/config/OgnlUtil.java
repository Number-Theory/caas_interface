package com.yzx.core.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.ListPropertyAccessor;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

/**
 * OGNL表达式计算工具 <br/>
 * 
 * @author xupiao 2017年6月12日
 *
 */
public class OgnlUtil {

	private static final Map<String, Object> DefaultContext = new HashMap<String, Object>();

	// 初始化OGNL运行时环境
	static {
		OgnlRuntime.setPropertyAccessor(List.class, new ListPropertyAccessor() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object getProperty(Map context, Object target, Object name) throws OgnlException {
				try {
					Object ret = super.getProperty(context, target, name);
					return ret;
				} catch (java.lang.IndexOutOfBoundsException e) {
					if (target instanceof List && name instanceof Integer) {
						HashMap ret = new HashMap();
						((List) target).add((Integer) name, ret);
						return ret;
					}
					throw e;
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
				List list = (List) target;
				try {
					super.setProperty(context, target, name, value);
				} catch (java.lang.IndexOutOfBoundsException e) {
					list.add(value);
				}
			}
		});
		OgnlRuntime.setNullHandler(Object.class, new MVELNullPropertyHandler());
	}

	public static void setValue(final Object root, final Map<String, Object> context, final String expr,
			final Object value) {
		Map<?, ?> ognlContext = createContext(root, context);
		try {
			Ognl.setValue(expr, ognlContext, root, value);
		} catch (OgnlException e) {
			processException(expr, e);
		}

	}

	public static void setValue(Object root, Map<String, Object> context, String expr, Object value,
			boolean autoCreatingNullNestedProperty) {
		if (context == null)
			context = new HashMap<String, Object>();
		MVELNullPropertyHandler.setCreatingNullObjects(context, autoCreatingNullNestedProperty);
		try {
			setValue(root, context, expr, value);
		} finally {
			MVELNullPropertyHandler.setCreatingNullObjects(context, false);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T findValue(final String expr, final Object root, final Map<String, Object> context) {
		MVELNullPropertyHandler.setCreatingNullObjects(context, true);
		try {
			return (T) Ognl.getValue(expr, createContext(root, context), root);
		} catch (OgnlException e) {
			return (T) processException(expr, e);
		} catch (NullPointerException e) {
			return (T) processException(expr, e);
		} finally {
			MVELNullPropertyHandler.setCreatingNullObjects(context, false);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T findValue(final String expr, final Class<T> clazz, final Object root,
			final Map<String, Object> context) {
		final OgnlContext ctx = (OgnlContext) createContext(root, context);

		Object node = null;
		try {
			node = Ognl.compileExpression(ctx, root, expr);
		} catch (Throwable e) {
			return (T) processException(expr, e);
		}

		MVELNullPropertyHandler.setCreatingNullObjects(ctx, true);
		try {
			if (node != null)
				return (T) Ognl.getValue(node, ctx, root, clazz);
			else
				return (T) Ognl.getValue(expr, ctx, root, clazz);
		} catch (Throwable e) {
			return (T) processException(expr, e);
		} finally {
			MVELNullPropertyHandler.setCreatingNullObjects(ctx, false);
		}

	}

	private static Object processException(String expr, Throwable e) {
		RuntimeException e1 = new RuntimeException("error while evaluate expression[" + expr + "]:" + e.getMessage(), e);
		throw e1;
	}

	private static Map<?, ?> createContext(Object root, Map<String, Object> context) {
		if (context instanceof OgnlContext)
			return context;
		if (context == null)
			context = Collections.emptyMap();
		OgnlContext ret = (OgnlContext) Ognl.addDefaultContext(root, context);
		ret.putAll(DefaultContext);
		return ret;
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		OgnlUtil.setValue(map, map, "a.b", "ab", true);
		OgnlUtil.setValue(map, map, "a.c", new HashMap<String, Object>(), true);
		OgnlUtil.setValue(map, map, "a.c.a", "aca", true);
		OgnlUtil.setValue(map, map, "a.c.b", "acb", true);
		OgnlUtil.setValue(map, map, "a.d.e", "ade", true);
		OgnlUtil.setValue(map, map, "a.d.f", "adf", true);
		System.out.println(map);
		
		OgnlUtil.setValue(map, map, "b[0].a", "0", true);
		OgnlUtil.setValue(map, map, "b[1].a", "1", true);
		OgnlUtil.setValue(map, map, "b[2].a", "2", true);
		OgnlUtil.setValue(map, map, "b[3].a", "3", true);
		System.out.println(map);
		
//		System.out.println(findValue("b[0].a", map, map));
//		
//		System.out.println(findValue("b", map, map));
	}
}
