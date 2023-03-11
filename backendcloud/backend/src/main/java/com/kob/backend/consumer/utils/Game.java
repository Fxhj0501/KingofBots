package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    private int[][] map;
    private final static int[] dx = {-1,0,1,0};
    private final static int[] dy = {0,1,0,-1};
    private final Player playerA;
    private final Player playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    //因为nextStepA/B 这个变量会在client线程中从键盘输入中获取值而被修改，又要在nextstep中被读取
    //因此增加锁来避免读写冲突
    private ReentrantLock lock = new ReentrantLock();
    //状态：playing->finished
    private String status = "playing";
    //三种情况：both代表平局，A代表A输，B代表B输
    private String loser = "";
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";
    public Game(Integer rows,
                Integer cols,
                Integer inner_walls_count,
                Integer idA,
                Bot botA,
                Integer idB,
                Bot botB){
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.map = new int[rows][cols];
        Integer botIdA = -1;
        Integer botIdB = -1;
        String botCodeA = "";
        String botCodeB = "";
        if(botA != null){
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if(botB != null){
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        playerA = new Player(idA,botIdA,botCodeA,rows-2,1,new ArrayList<>());
        playerB = new Player(idB,botIdB,botCodeB,1,cols-2,new ArrayList<>());
    }

    public Player getPlayerA(){
        return playerA;
    }

    public Player getPlayerB(){
        return playerB;
    }

    //设置每一步每名玩家是如何动的
    public void setNextStepA(Integer nextStepA){
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        }finally {
            lock.unlock();
        }

    }

    public  void setNextStepB(Integer nextStepB){
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        }finally {
            lock.unlock();
        }

    }

    public int[][] getMap(){
        return this.map;
    }

    private boolean check_connectivity(int sx,int sy,int tx,int ty){
        if(sx == tx && sy == ty)
            return true;
        map[sx][sy] = 1;
        for (int i=0;i<4;i++){
            int x = sx+dx[i];
            int y = sy+dy[i];
            if(x >=0 && x<this.rows && y>=0 && y<this.cols && map[x][y]==0){
                if(check_connectivity(x,y,tx,ty)){
                    map[sx][sy] = 0;
                    return true;
                }
            }
        }
        map[sx][sy] = 0;
        return false;
    }

    public boolean draw(){
        for(int i=0;i<this.rows;i++){
            for (int j=0;j<this.cols;j++){
                map[i][j] = 0;
            }
        }
        //给地图四周加上墙
        for(int r=0;r<this.rows;r++)
            map[r][0] = map[r][this.cols-1] = 1;
        for(int c=0;c<this.cols;c++)
            map[0][c] = map[this.rows-1][c] = 1;

        Random random = new Random();
        for(int i=0;i<this.inner_walls_count/2;i++){
            for (int j=0;j<1000;j++){
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);
                if(map[r][c] == 1|| map[this.rows-1-r][this.cols-1-c]==1)
                    continue;
                if(r == this.rows-2 && c == 1 && r==1 && c == this.cols-2 )
                    continue;
                map[r][c] = map[this.rows-1-r][this.cols-1-c] = 1;
                break;
            }
        }
        return check_connectivity(this.rows-2,1,1,this.cols-2);

    }

    public void createMap(){
        for(int i=0;i<1000;i++){
            if(draw())
                break;
        }
    }
    //将当前的局势编码为一个局势
    private String getInput(Player player){
        Player me,you;
        if(playerA.getId().equals(player.getId())){
            me = playerA;
            you = playerB;
        }else {
            me = playerB;
            you = playerA;
        }
        return getMapString()+"#"+me.getSx()+"#"+me.getSy()+"#("+me.getStepsString()+")#"+you.getSx()+"#"+you.getSy()+"#("+you.getStepsString()+")#";
    }
    private void sendBotCode(Player player){
        if(player.getBotId().equals(-1))
            return;
        MultiValueMap<String,String> data = new LinkedMultiValueMap<>();
        data.add("user_id",player.getId().toString());
        data.add("bot_code",player.getBotCode());
        data.add("input",getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl,data,String.class);
    }

    //等待两个玩家的下一步操作
    public boolean nextStep(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendBotCode(playerA);
        sendBotCode(playerB);
        for(int i=0;i<50;i++){
            try{
                Thread.sleep(100);
                lock.lock();
                try{
                    //都获取到了
                    if(nextStepA != null && nextStepB != null){
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                }finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public boolean check_valid(List<Cell> cellsA,List<Cell> cellsB){
        int n = cellsA.size();
        Cell cell = cellsA.get(n-1);
        if(this.map[cell.x][cell.y] == 1)
            return false;
        for(int i=0;i<n-1;i++){
            if(cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y)
                return false;
        }
        for(int i=0;i<n-1;i++){
            if(cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y){
                return false;
            }
        }
        return true;
    }
    public void judge(){//判断两名玩家操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();
        boolean validA = check_valid(cellsA,cellsB);
        boolean validB = check_valid(cellsB,cellsA);
        if(!validA || !validB){
            status = "finished";
            if(!validA && !validB)
                loser = "both";
            else if (!validA) {
                loser = "A";
            } else if (!validB) {
                loser = "B";
            }
        }
    }
    //向client传递移动信息
    private void sendMove(){
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event","move");
            System.out.println(nextStepA);
            System.out.println(nextStepB);
            resp.put("a_direction",nextStepA);
            resp.put("b_direction",nextStepB);
            sendAllMessage(resp.toJSONString());
            //进行下一步之前清空之前的步骤
            nextStepA = null;
            nextStepB = null;
        }finally {
            lock.unlock();
        }

    }
    //使用用户池存储的连接往客户端发信息
    private void sendAllMessage(String message){
        if(WebSocketServer.users.get(playerA.getId())!= null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        if(WebSocketServer.users.get(playerB.getId())!= null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }

    private String getMapString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                sb.append(map[i][j]);
            }
        }
        return sb.toString();
    }

    private void updateUserRating(Player player,Integer rating){
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }
    //算分机制是步数越多，说明对局质量越高，因此可以少扣分，多加分
    //为了避免刷分，10分封顶
    private void saveToDatebase(){
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();
        if("A".equals(loser)){
            ratingA -= Math.max((5 - (int) Math.floor(0.1 * playerA.getStepsString().length())), 0);
            ratingB += Math.min(5 + (int)Math.floor(0.1*playerA.getStepsString().length()),10);
        }else if ("B".equals(loser)){
            ratingA += Math.min(5 + (int)Math.floor(0.1*playerA.getStepsString().length()),10);;
            ratingB -= Math.max((5 - (int) Math.floor(0.1 * playerA.getStepsString().length())), 0);
        }
        updateUserRating(playerA,ratingA);
        updateUserRating(playerB,ratingB);
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }
    //向两个client公布结果
    private void sendResult(){
        JSONObject resp = new JSONObject();
        resp.put("event","result");
        resp.put("loser",loser);
        saveToDatebase();
        sendAllMessage(resp.toJSONString());
    }
    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            //是否获取到了两条蛇的下一步操作
            if(nextStep()){
                judge();
                if(status.equals("playing")){
                    sendMove();
                }else {
                    sendResult();
                    break;
                }
            }else {
                this.status = "finished";
                lock.lock();
                try {
                    if(nextStepA == null && nextStepB == null)
                        this.loser = "both";
                    else if(nextStepA == null)
                        this.loser = "A";
                    else
                        this.loser = "B";
                }finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }


}
