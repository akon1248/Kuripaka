package com.akon.kuripaka.reflection;

import com.akon.kuripaka.ThrowsRunnable;
import com.akon.kuripaka.ThrowsSupplier;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReflectionResult<T> {

	private T result;
	@Getter
	private boolean success;
	@Getter
	private ReflectiveOperationException exception;

	private ReflectionResult(ThrowsSupplier<? extends T, ReflectiveOperationException> supplier) {
		try {
			this.result = supplier.get();
			this.success = true;
		} catch (ReflectiveOperationException ex) {
			this.exception = ex;
		}
	}

	private ReflectionResult(ThrowsRunnable<ReflectiveOperationException> runnable) {
		try {
			runnable.run();
			this.success = true;
		} catch (ReflectiveOperationException ex) {
			this.exception = ex;
		}
	}

	public static <T> ReflectionResult<T> of(ThrowsSupplier<? extends T, ReflectiveOperationException> supplier) {
		return new ReflectionResult<>(supplier);
	}

	public static ReflectionResult<?> of(ThrowsRunnable<ReflectiveOperationException> runnable) {
		return new ReflectionResult<>(runnable);
	}

	public T result() {
		return this.result;
	}

	public ReflectionResult<T> onCatch(Consumer<? super ReflectiveOperationException> consumer) {
		Optional.ofNullable(this.exception).ifPresent(consumer);
		return this;
	}

	public ReflectionResult<T> throwOnCatch() throws ReflectiveOperationException {
		if (this.exception != null) {
			throw this.exception;
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <X extends ReflectiveOperationException> ReflectionResult<T> throwOnCatch(Class<X> clazz) throws X {
		if (clazz.isInstance(this.exception)) {
			throw (X)this.exception;
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <X extends Throwable> ReflectionResult<T> throwInvocationEx(Class<X> clazz) throws X {
		if (this.exception instanceof InvocationTargetException && clazz.isInstance(this.exception.getCause())) {
			throw (X)this.exception.getCause();
		}
		return this;
	}

	public <R> R ifSuccess(Function<? super T, ? extends R> func) {
		if (this.success) {
			return func.apply(this.result);
		}
		return null;
	}

	public <R> R ifSuccess(Supplier<? extends R> func) {
		if (this.success) {
			return func.get();
		}
		return null;
	}

	public <R> R ifSuccessOrElse(Function<? super T, ? extends R> ifSuccess, Supplier<? extends R> orElse) {
		if (this.success) {
			return ifSuccess.apply(this.result);
		}
		return orElse.get();
	}

	public <R> R ifSuccessOrElse(Supplier<? extends R> ifSuccess, Supplier<? extends R> orElse) {
		if (this.success) {
			return ifSuccess.get();
		}
		return orElse.get();
	}

}
