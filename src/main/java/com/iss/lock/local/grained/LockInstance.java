package com.iss.lock.local.grained;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取锁实例：<br>
 * 提供两种api的使用： <li>1.使用getLock(Object key)
 * <p>
 * &nbsp;&nbsp;&nbsp;</li>
 * <span>需要注意的是，使用getLock(),需要手动的释放锁，否则，相同的key会发生死锁问题</span> <li>
 * 2.使用executeLocked(String,LockCallBack)
 * <p>
 * &nbsp;&nbsp;&nbsp;</li> <span>会自动的进行加锁和释放锁</span>
 * 
 * 3。加锁和解锁必须成对的出现，否则容易出现死锁
 * @author issuser
 * 
 */
public class LockInstance {

	private static Logger logger = LoggerFactory.getLogger(LockInstance.class);

	/**
	 * 分段锁：根据key获取不同的ReentrantLock锁
	 */
	private Map<Object, ReentrantLock> keysMap = new ConcurrentHashMap<Object, ReentrantLock>();

	/**
	 * 初始化不同的key使用不同的分段锁的容量大小，如果业务并发量较大，建议设计大一些
	 */
	private int stripsNumber = 20;

	/**
	 * 获取锁超时时间的单位
	 */
	private TimeUnit timeoutUnit = TimeUnit.SECONDS;

	/**
	 * 获取锁的超时时间
	 */
	private long timeout = 10;

	/**
	 * 保证相同key的操作的同步行，替换synchronize关键字
	 */
	private ReentrantLock[] stripsLock = new ReentrantLock[stripsNumber];

	public LockInstance() {
		for (int i = 0, len = stripsLock.length; i < len; i++) {
			stripsLock[i] = new ReentrantLock(Boolean.FALSE);
		}
	}

	public LockInstance(TimeUnit timeoutUnit, long timeout, int stripsNumber) {
		this.timeoutUnit = timeoutUnit;
		this.timeout = timeout;
		this.stripsNumber = stripsNumber;
	}

	/**
	 * 获取锁
	 * @see 请使用LockCallBack代替
	 * @param key
	 * @throws InterruptedException
	 */
	@Deprecated
	public void getLock(Object key) throws InterruptedException {
		if (key == null) {
			throw new RuntimeException("key不能为空!");
		}

		ReentrantLock stripsLockReentrantLock = stripsLock[Math.abs(key
				.hashCode()) % stripsNumber];
		stripsLockReentrantLock.tryLock(5000, TimeUnit.SECONDS);

		logger.debug("开始获取锁");
		if (keysMap.get(key) == null) {
			keysMap.put(key, new ReentrantLock(Boolean.TRUE));
		}
		keysMap.get(key).tryLock(timeout, timeoutUnit);
		logger.debug("获取锁成功!");
	}

	/**
	 * 释放锁
	 * @see 请使用LockCallBack代替
	 * @param key
	 */
	@Deprecated
	public void releaseLock(String key) {
		ReentrantLock stripsLockReentrantLock = stripsLock[Math.abs(key
				.hashCode()) % stripsNumber];
		stripsLockReentrantLock.lock();
		ReentrantLock reentrantLock = keysMap.get(key);
		if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
			reentrantLock.unlock();
		}
		
		keysMap.remove(key);

		if (stripsLockReentrantLock.isHeldByCurrentThread()) {
			stripsLockReentrantLock.unlock();
			stripsLockReentrantLock.unlock();
		}

	}

	/**
	 * 模板方法模式，控制加锁和释放锁
	 * 
	 * @param key
	 * @param lockCallBack
	 */
	public void executeLocked(String key, LockCallBack lockCallBack) {
		try {
			getLock(key);
			lockCallBack.doInLock();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			releaseLock(key);
		}

	}

	public static interface LockCallBack {

		public void doInLock() throws Exception;

	}

	public TimeUnit getTimeoutUnit() {
		return timeoutUnit;
	}

	public void setTimeoutUnit(TimeUnit timeoutUnit) {
		this.timeoutUnit = timeoutUnit;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getStripsNumber() {
		return stripsNumber;
	}

	public static void main(String[] args) {

		final LockInstance lockInstance = LockManager.getLockInstance();

		for (int i = 0, len = 1000; i < len; i++) {

			new Thread(new Runnable() {
				String key2;

				public void run() {
					try {

						String key2 = Thread.currentThread().getName();
						lockInstance.getLock("AAA");
						logger.debug("开始执行业务代码");
						Thread.sleep(2000);
						logger.debug("业务代码执行完毕");
						lockInstance.getLock("AAA");
						lockInstance.releaseLock("AAA");
						key2="AAA";
//						lockInstance.executeLocked(key2, new LockCallBack() {
//
//							public void doInLock() throws Exception{
//								logger.debug("开始执行业务代码");
//								Thread.sleep(2000);
//								logger.debug("业务代码执行完毕");
//							}
//						});

					} catch (Exception e) {
						e.printStackTrace();
					} finally {

					}
				}
			}).start();
		}

	}

}
