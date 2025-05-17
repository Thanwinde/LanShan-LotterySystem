package com.lotterysystem.server.pojo.vo;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;



@Data
public class PrizeVO implements Serializable {



    /**
     * 奖品序号
     */

    @Schema(description = "奖品序号")
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
    @Schema(description = "奖品稀有度")
    private Integer rarity;
    /**
     * 奖品总数
     */
    @Schema(description = "奖品总数")
    private Integer fullCount;
    /**
     * 目前还剩数量
     */
    @Schema(description = "抢到的数量")
    private Integer outCount;
    /**
     * 该抽奖是否已经结束
     */
    @Schema(description = "该抽奖是否已经结束")
    private Integer isEnd;



}
