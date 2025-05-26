package com.lotterysystem.server.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.mapper.RecordMapper;
import com.lotterysystem.server.pojo.entity.Record;
import com.lotterysystem.server.service.RecordActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * @author nsh
 * @data 2025/5/23 17:22
 * @description
 **/
@Service
@Transactional
public class RecordActionServiceImpl extends ServiceImpl<RecordMapper, Record>
        implements RecordActionService {

    @Override
    @DS("master")
    public void batchingSave(ArrayList<Record> buffer) {
        saveOrUpdateBatch(buffer);
    }
}
