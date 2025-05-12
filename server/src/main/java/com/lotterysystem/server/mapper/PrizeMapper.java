package com.lotterysystem.server.mapper;

import com.lotterysystem.server.pojo.entity.Prize;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author thanw
* @description 针对表【prize】的数据库操作Mapper
* @createDate 2025-05-12 12:08:52
* @Entity com.lotterysystem.server.pojo.entity.Prize
*/
@Mapper
public interface PrizeMapper extends BaseMapper<Prize> {

}




