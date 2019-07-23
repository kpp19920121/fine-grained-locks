package com.iss.lock.local.grained;


import java.util.concurrent.TimeUnit;

public class LockManager {

	public static LockInstance getLockInstance() {
		return new LockInstance();
	}

	public static LockInstance getLockInstance(TimeUnit timeoutUnit,
			long timeout, int stripsNumber) {
		return new LockInstance(timeoutUnit, timeout, stripsNumber);
	}

}
