package com.iss.jdk.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReentrantLockTest {

	
	private static Logger  logger=LoggerFactory.getLogger(ReentrantLockTest.class);
	
	public static void main(String[] args) {
		final ReentrantLock reentrantLock=new ReentrantLock(Boolean.FALSE);
		for(int i=0,len=100;i<len;i++){
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						//logger.debug(reentrantLock+"");
						//reentrantLock.lock();
						logger.debug(reentrantLock.tryLock(3600,TimeUnit.SECONDS)+"");
						logger.info("开始执行代码:");	
						Thread.sleep(2000);
						logger.info("结束执行代码:");	
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						if(reentrantLock.isHeldByCurrentThread()){
							logger.debug("开始释放锁!");
							reentrantLock.unlock();
						}
					}
				}
			}).start();
		}
	}

}
