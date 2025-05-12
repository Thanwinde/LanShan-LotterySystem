package com.lotterysystem.server.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
* 
* @TableName prize
*/
@TableName("prize")
@Data
public class Prize implements Serializable {

    /**
    * 奖品序号
    */

    @Schema(description = "奖品序号")
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 抽奖池序号
    */
    @Schema(description = "抽奖池序号")
    private Long lotteryId;
    /**
    * 奖品名
    */

    @Schema(description = "奖品名")

    private String name;
    /**
    * 奖品类型
    */
    @Schema(description = "奖品类型")
    private Integer type;
    /**
    * 奖品总数
    */
    @Schema(description = "奖品总数")
    private Integer fullCount;
    /**
    * 目前还剩数量
    */
    @Schema(description = "目前还剩数量")
    private Integer nowCount;
    /**
    * 该抽奖是否已经结束
    */
    @Schema(description = "该抽奖是否已经结束")
    private Integer isEnd;


}
