package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    private Session session = null;
    private User user;
    //线程安全
    private static ConcurrentHashMap<Integer,WebSocketServer> users = new ConcurrentHashMap<>();
    //线程安全的选手匹配池
    private static CopyOnWriteArraySet<User> matchpool = new CopyOnWriteArraySet<>();
    private static UserMapper userMapper;
    //private Game game = null;
    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper = userMapper;
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if(this.user != null){
            System.out.println("Connected");
            users.put(userId,this);
        }else{
            this.session.close();
        }

    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected");
        if(this.user != null){
            users.remove(this.user.getId());
            matchpool.remove(this.user);
        }
    }

    private void startMatching(){
        System.out.println("start-matching");
        matchpool.add(this.user);
        while (matchpool.size()>=2){
            Iterator<User> it = matchpool.iterator();
            User a = it.next(),b=it.next();
            matchpool.remove(a);
            matchpool.remove(b);
            Game game = new Game(13,14,20);
            game.createMap();
            JSONObject respA = new JSONObject();
            respA.put("event","match_found");
            respA.put("opponent_username",b.getUsername());
            respA.put("opponent_photo",b.getPhoto());
            respA.put("gamemap",game.getMap());
            users.get(a.getId()).sendMessage(respA.toJSONString());

            JSONObject respB = new JSONObject();
            respB.put("event","match_found");
            respB.put("opponent_username",a.getUsername());
            respB.put("opponent_photo",a.getPhoto());
            respB.put("gamemap",game.getMap());
            users.get(b.getId()).sendMessage(respB.toJSONString());
        }
    }

    private void stopMatching(){
        System.out.println("stop-matching");
        matchpool.remove(this.user);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("received message");
        JSONObject data = JSONObject.parseObject(message);
        //解析从前端传来的event
        String event = data.getString("event");
        if("start-matching".equals(event)){
            startMatching();
        }else if("stop-matching".equals(event)){
            stopMatching();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message){
        synchronized (this.session){
            try{
                this.session.getBasicRemote().sendText(message);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

