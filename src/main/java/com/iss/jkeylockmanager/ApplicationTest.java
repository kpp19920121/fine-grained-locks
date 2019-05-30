package com.iss.jkeylockmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;
import de.jkeylockmanager.manager.LockCallback;

public class ApplicationTest {

	private static KeyLockManager firstkeyLockManager = KeyLockManagers
			.newLock();

	private static KeyLockManager secondkeyLockManager = KeyLockManagers
			.newLock();

	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationTest.class);

	public static void main(String[] args) {
		final String key = Thread.currentThread().getName();

		final String key2 = "BBBBB";

		for (int i = 0, len = 100; i < len; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					firstkeyLockManager.executeLocked(key2, new LockCallback() {
						@Override
						public void doInLock() {
							try {
								logger.debug("哈哈，第一层锁开始执行了!");
								Thread.sleep(1000);

//								secondkeyLockManager.executeLocked(key2,
//										new LockCallback() {
//											@Override
//											public void doInLock() {
//												try {
//													logger.debug("哈哈，第二层锁开始执行了!");
//													Thread.sleep(1000);
//													logger.debug("哈哈，第二层锁执行结束了!");
//												} catch (InterruptedException e) {
//													e.printStackTrace();
//												}
//											}
//										});

								logger.debug("哈哈，第一层锁执行结束了");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});

				}
			}).start();
			;
		}

	}

}
