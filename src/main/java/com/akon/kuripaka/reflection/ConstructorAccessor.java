package com.akon.kuripaka.reflection;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ConstructorAccessor<T> {

	private final Class<T> clazz;
	private final Class<?>[] params;
	private volatile Constructor<T> constructor;

	public ConstructorAccessor(Class<T> clazz, Class<?>... params) {
		this.clazz = clazz;
		this.params = params;
	}

	public static <T> ConstructorAccessor<T> fromConstructor(Constructor<T> constructor) {
		ConstructorAccessor<T> constructorAccessor = new ConstructorAccessor<>(constructor.getDeclaringClass(), constructor.getParameterTypes());
		constructorAccessor.initConstructor(constructor);
		return constructorAccessor;
	}

	public ReflectionResult<T> construct(Object... args) {
		return ReflectionResult.of(() -> this.getConstructor0().newInstance(args));
	}

	public ReflectionResult<Constructor<T>> getConstructor() {
		return ReflectionResult.of(this::getConstructor0);
	}

	private Constructor<T> getConstructor0() throws NoSuchMethodException {
		if (this.constructor == null) {
			this.initConstructor(this.clazz.getDeclaredConstructor(this.params));
		}
		return this.constructor;
	}

	private void initConstructor(Constructor<T> constructor) {
		AccessController.doPrivileged((PrivilegedAction<?>)() -> {
			constructor.setAccessible(true);
			this.constructor = constructor;
			return null;
		});
	}

}
