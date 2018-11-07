package com.sankuai.meituan.config.constant;

import java.util.HashMap;

/**
 * 可以为枚举提供根据index或者name获取具体枚举值的方法
 * 使用时需要作为枚举的static final属性,可参考{@link com.sankuai.meituan.pmc.constant.ServiceRateListType}的实现
 */
public class ConstantGenerator<T extends Enum & ConstantGenerator.Constant> {
	private final Class<T> constant;
	private final HashMap<Integer, T> constantByIndex = new HashMap<Integer, T>();
	private final HashMap<String, T> constantByName = new HashMap<String, T>();

	public static <E extends Enum & ConstantGenerator.Constant> ConstantGenerator<E> create(Class<E> constant) {
		return new ConstantGenerator<E>(constant);
	}

	private ConstantGenerator(Class<T> constant) {
		this.constant = constant;
		for (T t : constant.getEnumConstants()) {
			constantByIndex.put(t.getIndex(), t);
			constantByName.put(t.getName(), t);
		}
	}

	public T getByIndex(Integer index) {
		return checkExist(constantByIndex.get(checkNotNull(index)), "index", index);
	}

	public T getByName(String name) {
		return checkExist(constantByName.get(checkNotNull(name)), "name", name);
	}

	public Integer getIndex(String name) {
		return getByName(name).getIndex();
	}

	public String getName(Integer index) {
		return getByIndex(index).getName();
	}

	private T checkExist(T t, String description, Object value) {
		if (t == null) {
			throw new IllegalArgumentException(constant.getSimpleName() + "没有" + description + "为" + value + "的枚举");
		}
		return t;
	}

	private <C> C checkNotNull(C reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	public static interface Constant {
		int getIndex();

		String getName();
	}
}
