package com.akon.kuripaka.reflection;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

@RequiredArgsConstructor
public class FieldAccessor {

	private static final FieldAccessor MODIFIERS = new FieldAccessor(Field.class, "modifiers");

	private final Class<?> clazz;
	private final String fieldName;
	private volatile Field field;

	public static FieldAccessor fromField(Field field) {
		FieldAccessor fieldAccessor = new FieldAccessor(field.getDeclaringClass(), field.getName());
		fieldAccessor.initField(field);
		return fieldAccessor;
	}

	public ReflectionResult<?> set(Object obj, Object value) {
		return ReflectionResult.of(() -> this.getField0().set(obj, value));
	}

	public ReflectionResult<?> setStatic(Object value) {
		return this.set(null, value);
	}

	public ReflectionResult<Object> get(Object obj) {
		return ReflectionResult.of(() -> this.getField0().get(obj));
	}

	@SuppressWarnings("unchecked")
	public <T> ReflectionResult<T> get(Object obj, Class<T> clazz) {
		return ReflectionResult.of(() -> (T)ClassUtils.primitiveToWrapper(clazz).cast(this.getField0().get(obj)));
	}

	public ReflectionResult<Object> getStatic() {
		return this.get(null);
	}

	public <T> ReflectionResult<T> getStatic(Class<T> clazz) {
		return this.get(null, clazz);
	}

	public ReflectionResult<Field> getField() {
		return ReflectionResult.of(this::getField0);
	}

	private Field getField0() throws NoSuchFieldException {
		if (this.field == null) {
			this.initField(this.clazz.getDeclaredField(this.fieldName));
		}
		return this.field;
	}

	private void initField(Field field) {
		if (Modifier.isFinal(field.getModifiers())) {
			MODIFIERS.set(field, field.getModifiers() & ~Modifier.FINAL);
		}
		AccessController.doPrivileged((PrivilegedAction<?>)() -> {
			field.setAccessible(true);
			this.field = field;
			return null;
		});
	}

}
