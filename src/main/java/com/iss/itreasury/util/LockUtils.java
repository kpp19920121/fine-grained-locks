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
	 * ��Map��key��Ϊҵ���������磬��ͬ��ҵ����Բ��ò�ͬ��key��ͬһ��ҵ���£������ж��
	 */
	private static Map<String, ReentrantLock> lockedMap = new ConcurrentHashMap<String, ReentrantLock>();


	
	/**
	 * ʹ��keyLocks����֤��ͬkey��ȡ��ʱ��ͬ�����⣨�����getLock��synchronize�ؼ���
	 */
	private static ReentrantLock[] keyLocks = new ReentrantLock[16];

	
	/**
	 * ��ʼ��keyLocks
	 */
	static{
		for(int i=0,len=keyLocks.length;i<len;i++){
			keyLocks[i]=new ReentrantLock();
		}
	}
	
	public static String getLock(String prefix, Object object)
			throws InterruptedException {
		// �жϼ������Ƿ��ж�Ӧ����

		
		/**
		 * ��ͬ��key��ȡ��ͬ��ϸ����������֤��ͬkey��ȡ�����̰߳�ȫ����
		 */
		ReentrantLock simpleReentrantLock = keyLocks[prefix.hashCode()
				% keyLocks.length];
		
		simpleReentrantLock.tryLock(5,TimeUnit.SECONDS);

		ReentrantLock reentrantLock = lockedMap.get(prefix);
		if (Thread.currentThread().getName().equals("Thread-0")) {
			logger.debug("�ҿ�ʼ������---->");
			// Thread.sleep(20000);
			logger.debug("�����߽�����---->");
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

			logger.debug("��ʼ�ͷ���");
			ReentrantLock reentrantLock = lockedMap.get(prefix);
			if (reentrantLock.isHeldByCurrentThread()) {
				reentrantLock.unlock();
				logger.debug("�ͷ�������");
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
						logger.debug("��ʼִ��ҵ�����");
						// Thread.sleep(2000);
						logger.debug("ҵ�����ִ�����");
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
