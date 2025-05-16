package com.lotterysystem.server.mapper;

import com.lotterysystem.server.pojo.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author thanw
* @description 针对表【record】的数据库操作Mapper
* @createDate 2025-05-14 14:09:21
* @Entity com.lotterysystem.server.pojo.entity.Record
*/
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

}




