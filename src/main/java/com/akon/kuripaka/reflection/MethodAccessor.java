package com.akon.kuripaka.reflection;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MethodAccessor {

	private final Class<?> clazz;
	private final String methodName;
	private final Class<?>[] params;
	private volatile Method method;

	public MethodAccessor(Class<?> clazz, String methodName, Class<?>... params) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.params = params;
	}

	public static MethodAccessor fromMethod(Method method) {
		MethodAccessor methodAccessor = new MethodAccessor(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
		methodAccessor.initMethod(method);
		return methodAccessor;
	}

	public ReflectionResult<?> invoke(Object obj, Object... args) {
		return ReflectionResult.of(() -> this.getMethod0().invoke(obj, args));
	}

	@SuppressWarnings("unchecked")
	public <T> ReflectionResult<T> invoke(Object obj, Class<T> clazz, Object... args) {
		return ReflectionResult.of(() -> (T)ClassUtils.primitiveToWrapper(clazz).cast(this.getMethod0().invoke(obj, args)));
	}

	public ReflectionResult<?> invokeStatic(Object... args) {
		return this.invoke(null, args);
	}

	public <T> ReflectionResult<T> invokeStatic(Class<T> clazz, Object... args) {
		return this.invoke(null, clazz, args);
	}

	public ReflectionResult<Method> getMethod() {
		return ReflectionResult.of(this::getMethod0);
	}

	private Method getMethod0() throws NoSuchMethodException {
		if (this.method == null) {
			this.initMethod(this.clazz.getDeclaredMethod(this.methodName, this.params));
		}
		return this.method;
	}

	private void initMethod(Method method) {
		AccessController.doPrivileged((PrivilegedAction<?>)() -> {
			method.setAccessible(true);
			this.method = method;
			return null;
		});
	}

}
