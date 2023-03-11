package com.kob.backend.service.impl.user.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.AddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {
    @Autowired
    private BotMapper botMapper;
    @Override
    public Map<String, String> add(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();
        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");
        Map<String,String> map = new HashMap<>();
        if(title == null || title.length()==0){
            map.put("error_message","标题不能为空");
            return map;
        }
        if(title.length()>100){
            map.put("error_message","标题过长");
            return map;
        }
        if(description == null || description.length()==0){
            description = "这个用户太懒了，什么也没有留下QAQ";
        }
        if(description!=null && description.length()>300){
            map.put("error_message","bot描述过长");
            return map;
        }
        if(content == null || content.length()==0){
            map.put("error_message","bot代码不能为空");
            return map;
        }
        if(content.length()>10000){
            map.put("error_message","代码过长");
            return map;
        }
        QueryWrapper<Bot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        if(botMapper.selectCount(queryWrapper)>=10){
            map.put("error_message","最多只能创建5个Bot");
            return map;
        }
        Date now = new Date();
        Bot bot = new Bot(null,user.getId(),title,description,content,now,now);
        botMapper.insert(bot);
        map.put("error_message","success");
        return map;
    }
}
