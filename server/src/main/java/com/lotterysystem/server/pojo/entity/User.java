package com.lotterysystem.server.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author nsh
 * @data 2025/5/10 12:22
 * @description
 **/
@Data
@TableName("user")
public class User {
    @TableId(value = "id",type = IdType.AUTO)
    Long id;
    String username;
    String password;
    String authority;
}
