package com.lotterysystem.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Record;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;

/**
* @author thanw
* @description 针对表【record】的数据库操作Service
* @createDate 2025-05-14 14:09:21
*/
public interface RecordActionService extends IService<Record> {

    void batchingSave(ArrayList<Record> buffer);
}
