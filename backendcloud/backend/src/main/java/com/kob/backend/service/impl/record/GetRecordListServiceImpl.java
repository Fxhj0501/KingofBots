package com.kob.backend.service.impl.record;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import com.kob.backend.service.record.GetRecordListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GetRecordListServiceImpl implements GetRecordListService {
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public JSONObject getList(Integer page) {
        IPage<Record> recordIPage = new Page<>(page,15);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        //降序排序，越新的对战记录越先返回
        queryWrapper.orderByDesc("id");
        List<Record> records = recordMapper.selectPage(recordIPage,queryWrapper).getRecords();
        JSONObject resp = new JSONObject();
        List<JSONObject> items = new LinkedList<>();
        for(Record record:records){
            User userA = userMapper.selectById(record.getAId());
            User userB = userMapper.selectById(record.getBId());
            JSONObject item = new JSONObject();
            item.put("a_photo",userA.getPhoto());
            item.put("a_username",userA.getUsername());
            item.put("b_photo",userB.getPhoto());
            item.put("b_username",userB.getUsername());
            String result = "平局";
            if("A".equals(record.getLoser()))
                result = "PlayerB胜";
            else if("B".equals(record.getLoser()))
                result = "PlayerA胜";
            item.put("result",result);
            item.put("record",record);
            items.add(item);
        }
        resp.put("records",items);
        //为了前端方便展示分页，需要知道总页数
        resp.put("records_count",recordMapper.selectCount(null));
        return resp;
    }
}
