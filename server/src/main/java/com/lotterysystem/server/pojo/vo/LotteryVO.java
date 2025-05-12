package com.lotterysystem.server.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lotterysystem.server.pojo.dto.PrizeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Data
public class LotteryVO implements Serializable {

    /**
     * 序号
     */

    @Schema(description = "序号")
    @TableId(value = "id",type = IdType.AUTO)
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
     * 是否启用
     */
    @Schema(description ="是否启用")
    private Integer isActive;
    /**
     * 创建时间
     */
    @Schema(description ="创建时间")
    private Date createdAt;
    /**
     * 创建者
     */
    @Schema(description ="创建者")
    private Long createdBy;

    @Schema(description = "创建者用户名")
    private String creatorName;
    /**
     * 最后一次修改
     */
    @Schema(description ="最后一次修改")
    private Date updatedAt;

    @Schema(description = "是否结束")
    private Integer isEnd;

    @Schema(description = "奖品池")
    private ArrayList<PrizeVO> prizes;

}
