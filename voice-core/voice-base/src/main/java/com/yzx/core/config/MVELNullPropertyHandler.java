package com.yzx.core.config;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.ASTChain;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.Node;
import ognl.NullHandler;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import com.yzx.core.util.ClassUtils;

/**
 * 
 * @author xupiao 2017年6月12日
 *
 */
public class MVELNullPropertyHandler implements NullHandler {
	private final static String CREATE_NULL_OBJECTS = "CREATE_NULL_OBJECTS";

	public static boolean isCreatingNullObjects(Map<String, Object> context) {
		Boolean myBool = (Boolean) context.get(CREATE_NULL_OBJECTS);
		return (myBool == null) ? false : myBool.booleanValue();
	}

	public static void setCreatingNullObjects(Map<String, Object> context, boolean creatingNullObjects) {
		if (context == null)
			return;
		if (creatingNullObjects)
			context.put(CREATE_NULL_OBJECTS, Boolean.TRUE);
		else
			context.remove(CREATE_NULL_OBJECTS);
	}

	@SuppressWarnings("rawtypes")
	public Object nullMethodResult(Map context, Object target, String methodName, Object[] args) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object nullPropertyValue(Map context, Object target, Object property) {
		return __nullPropertyValue(context, target, property);
	}

	private class MyList extends ArrayList<Object> {
		private static final long serialVersionUID = 1L;

		@Override
		public Object set(int index, Object element) {
			try {
				return super.set(index, element);
			} catch (java.lang.IndexOutOfBoundsException e) {
				add(index, element);
				return null;
			}
		}

		@Override
		public Object get(int index) {
			try {
				return super.get(index);
			} catch (java.lang.IndexOutOfBoundsException e) {
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Object __nullPropertyValue(Map<String, Object> context, Object target, Object property) {
		boolean c = isCreatingNullObjects(context);

		if (!c) {
			return null;
		}

		if ((target == null) || (property == null)) {
			return null;
		}

		OgnlContext ctx = (OgnlContext) context;
		Node nextNode = getNextNodeInChainNode(ctx.getCurrentNode());
		if (nextNode == null) // 最后一个节点不处理
			return null;

		try {
			String propName = property.toString();

			PropertyDescriptor pd = PropertyUtil.getPropertyDescriptor(target.getClass(), propName);
			if (pd == null) {
				// 构造 map 套map|list
				Object currentObj = null;
				if (nextNode instanceof ASTProperty && (((ASTProperty) nextNode).isIndexedAccess())) {
					Node nextNodeValue = nextNode.jjtGetChild(0);
					if (nextNodeValue instanceof ASTConst && (((ASTConst) nextNodeValue).getValue() instanceof Integer))// 直接写的数字
						currentObj = new MyList();
					else {
						try {
							Object value = Ognl.getValue(nextNodeValue.toString(), ctx.getRoot());
							if (value instanceof Integer)
								currentObj = new MyList();
						} catch (OgnlException e) {
							// log.warn("子表达式求值失败，无法决定是否应该创建List:" +
							// nextNodeValue.toString() + ", 完整表达式：" +
							// getRoot(nextNode), e);
						}
					}
				}
				if (currentObj == null)
					currentObj = new HashMap<String, Object>();

				if (currentObj != null) {
					if (target instanceof MyList) {
						int index = Integer.parseInt(propName);
						List<Object> list = (List<Object>) target;
						for (int i = list.size(); i < index - 1; i++) {
							list.add(null);
						}
						list.add(currentObj);
					} else {
						Ognl.setValue(propName, context, target, currentObj);
					}
				}

				return currentObj;
			}

			Class<?> clazz = pd.getPropertyType();

			if (clazz == null) {
				// can't do much here!
				return null;
			}

			Object param = createObject(clazz, target, propName, context);

			if (param != null)
				Ognl.setValue(propName, context, target, param);

			return param;
		} catch (Exception e) {
			// log.error("Could not create and/or set value back on to object: "
			// + getRoot(nextNode), e);
		}

		return null;
	}

	private Object createObject(Class<?> clazz, Object target, String property, Map<String, Object> context)
			throws Exception {
		if (Collection.class.isAssignableFrom(clazz)) {
			Class<Object> c = ClassUtils.getObjectClass(target, property);
			if (c != null)
				return new AutoCreatingNullObjectList<Object>(c);
			return new ArrayList<Object>();
		} else if (clazz == Map.class) {
			return new HashMap<Object, Object>();
		}

		return clazz.newInstance();
	}

	public static Node getRoot(Node node) {
		if (node.jjtGetParent() == null)
			return node;
		return getRoot(node.jjtGetParent());
	}

	/**
	 * 取表达式当前求值结点所在ASTChain结点中的下一个子结点。
	 */
	public static Node getNextNodeInChainNode(Node node) {
		if (node == null)
			return null;

		Node parent = node.jjtGetParent();
		while (parent != null && !(parent instanceof ASTChain)) {
			node = parent;
			parent = parent.jjtGetParent();
		}

		if (parent instanceof ASTChain) {
			while (parent.jjtGetParent() instanceof ASTChain) {
				parent = parent.jjtGetParent();
			}
			for (int i = 0; i < parent.jjtGetNumChildren() - 1; i++) {
				if (parent.jjtGetChild(i) == node)
					return parent.jjtGetChild(i + 1);
			}
			return null;
		} else {
			return null;
		}
	}
}
