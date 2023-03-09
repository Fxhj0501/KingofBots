package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class BotPool extends Thread{
    private final static ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();
    public void addBot(Integer userId,String botCode,String input){
        lock.lock();
        try{
            bots.add(new Bot(userId,botCode,input));
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
    private void consume(Bot bot){
        Comsumer comsumer  = new Comsumer();
        comsumer.startTimeout(2000,bot);

    }
    @Override
    public void run(){
        while(true){
            lock.lock();
            if(bots.isEmpty()){
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else{
                Bot bot = bots.remove();
                lock.unlock();
                // 考虑到这步需要编译代码并执行，比较耗时，因此先释放锁后执行
                //并且这一步不涉及任何读写冲突
                consume(bot);
            }
        }
    }
}
