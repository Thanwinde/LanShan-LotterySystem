package com.lotterysystem.server.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lotterysystem.server.pojo.entity.Lottery;
import org.apache.ibatis.annotations.Mapper;

/**
* @author thanw
* @description 针对表【lottery】的数据库操作Mapper
* @createDate 2025-05-10 18:35:53
* @Entity generator.domain.Lottery
*/
@Mapper
public interface LotteryMapper extends BaseMapper<Lottery> {

}




