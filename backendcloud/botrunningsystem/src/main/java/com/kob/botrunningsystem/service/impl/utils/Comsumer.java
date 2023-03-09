package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
@Component
public class Comsumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private final static String reveiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        Comsumer.restTemplate = restTemplate;
    }
    public void startTimeout(long timeout,Bot bot){
        this.bot = bot;
        this.start();
        try {
            //最多等待timeout秒
            this.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            this.interrupt();
        }

    }
    //在code中的Bot类名后添加uid
    private String addUid(String code,String uid){
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        return code.substring(0,k)+uid+code.substring(k);
    }
    //同类名的代码只编译一次，因此需要加上随机的uid
    @Override
    public void run(){
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0,8);
        BotInterface botInterface = Reflect.compile(
            "com.kob.botrunningsystem.utils.Bot"+uid,
                addUid(bot.getBotCode(),uid)
        ).create().get();
        Integer direction = botInterface.nextMove(bot.getInput());
        System.out.println(botInterface.nextMove(bot.getInput()));
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("user_id",bot.getUserId().toString());
        data.add("direction",direction.toString());
        restTemplate.postForObject(reveiveBotMoveUrl,data,String.class);
    }
}
