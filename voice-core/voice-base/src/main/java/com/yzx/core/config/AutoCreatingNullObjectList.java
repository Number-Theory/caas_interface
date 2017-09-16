package com.yzx.core.config;

import java.util.ArrayList;

/**
 * 
 * @author xupiao 2017年6月12日
 *
 * @param <E>
 */
public class AutoCreatingNullObjectList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 6417310975816829850L;
	private final Class<E> clazz;

	public AutoCreatingNullObjectList(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public E get(int index) {
		if (index >= this.size()) {
			if (index - this.size() > 100)
				throw new IllegalArgumentException("index too large!");
			for (int i = this.size(); i <= index; i++)
				this.add(createObject());
		}
		return super.get(index);
	}

	public E createObject() {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
