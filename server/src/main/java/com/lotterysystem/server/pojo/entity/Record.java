package com.lotterysystem.server.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @TableName record
 */
@TableName(value ="record")
@Data
@Accessors(chain = true)
public class Record implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 
     */
    @TableField(value = "lottery_id")
    private Long lotteryId;

    /**
     * 
     */
    @TableField(value = "lottery_name")
    private String lotteryName;

    /**
     * 
     */
    @TableField(value = "prize_id")
    private Long prizeId;

    /**
     * 
     */
    @TableField(value = "prize_name")
    private String prizeName;

    private Integer isEnd;
}