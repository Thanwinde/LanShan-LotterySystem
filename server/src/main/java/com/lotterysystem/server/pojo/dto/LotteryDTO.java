package com.lotterysystem.server.pojo.dto;

import com.lotterysystem.server.pojo.entity.Prize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Data
public class LotteryDTO implements Serializable {

    /**
     * 序号
     */

    @Schema(description = "序号")
    private Long id;
    /**
     * 名字
     */

    @Schema(description = "名字")

    private String name;
    /**
     * 基本抽奖类型
     */

    @Schema(description = "基本抽奖类型")
    private Integer type;
    /**
     *
     */

    @Schema(description = "开始时间")
    private Date startTime;
    /**
     *
     */

    @Schema(description = "结束时间")
    private Date endTime;
    /**
     * 模式相关参数，如阈值、等级权重等
     */

    @Schema(description ="模式相关参数，如阈值、等级权重等")
    private Object ruleConfig;

    @Schema(description = "奖品池")
    private ArrayList<PrizeDTO> prizes;

}
