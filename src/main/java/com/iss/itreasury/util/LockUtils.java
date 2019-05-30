package com.iss.itreasury.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockUtils {

	private static Logger logger = LoggerFactory.getLogger(LockUtils.class);
	
	
	/**
	 * 已Map的key作为业务主键，如，不同的业务可以采用不同的key，同一个业务下，可能有多个
	 */
	private static Map<String, ReentrantLock> lockedMap = new ConcurrentHashMap<String, ReentrantLock>();


	
	/**
	 * 使用keyLocks来保证不同key获取锁时的同步问题（替代）getLock的synchronize关键字
	 */
	private static ReentrantLock[] keyLocks = new ReentrantLock[16];

	
	/**
	 * 初始化keyLocks
	 */
	static{
		for(int i=0,len=keyLocks.length;i<len;i++){
			keyLocks[i]=new ReentrantLock();
		}
	}
	
	public static String getLock(String prefix, Object object)
			throws InterruptedException {
		// 判断集合中是否有对应的锁

		
		/**
		 * 不同的key获取不同的细粒度锁，保证不同key获取锁的线程安全问题
		 */
		ReentrantLock simpleReentrantLock = keyLocks[prefix.hashCode()
				% keyLocks.length];
		
		simpleReentrantLock.tryLock(5,TimeUnit.SECONDS);

		ReentrantLock reentrantLock = lockedMap.get(prefix);
		if (Thread.currentThread().getName().equals("Thread-0")) {
			logger.debug("我开始休眠了---->");
			// Thread.sleep(20000);
			logger.debug("我休眠结束了---->");
		}
		if (reentrantLock == null) {
			reentrantLock = new ReentrantLock(Boolean.TRUE);
			lockedMap.put(prefix, reentrantLock);
		}

		reentrantLock.lock();
		if(simpleReentrantLock.isHeldByCurrentThread()){
			simpleReentrantLock.unlock();
		}
		
		return "";

	}

	public static void releaseLock(String prefix, Object object) {
		try {

			logger.debug("开始释放锁");
			ReentrantLock reentrantLock = lockedMap.get(prefix);
			if (reentrantLock.isHeldByCurrentThread()) {
				reentrantLock.unlock();
				logger.debug("释放锁结束");
			}

		} finally {
		}
	}


	public static void main(String[] args) {
		
		final String prefix="AAAA";
		for (int i = 0, len = 10000; i < len; i++) {
			
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					String lockObject = Thread.currentThread().getName();
					try {
						getLock(prefix, lockObject);
						logger.debug("开始执行业务代码");
						// Thread.sleep(2000);
						logger.debug("业务代码执行完毕");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						releaseLock(prefix, lockObject);
					}
				}
			}).start();
		}

	}

}
