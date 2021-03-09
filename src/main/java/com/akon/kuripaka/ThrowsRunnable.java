package com.akon.kuripaka;

@FunctionalInterface
public interface ThrowsRunnable<X extends Throwable> {

	void run() throws X;

}
