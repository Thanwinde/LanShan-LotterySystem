package com.lotterysystem.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lotterysystem.server.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author nsh
 * @data 2025/5/10 12:35
 * @description
 **/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
