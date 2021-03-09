package com.akon.kuripaka;

@FunctionalInterface
public interface ThrowsSupplier<T, X extends Throwable> {

	T get() throws X;

}
