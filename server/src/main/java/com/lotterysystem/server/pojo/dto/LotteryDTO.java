package com.lotterysystem.server.pojo.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("lottery")
public class LotteryDTO implements Serializable {

    /**
     * 序号
     */
    @NotNull(message="[序号]不能为空")
    @Schema(description = "序号")
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 名字
     */
    @NotBlank(message="[名字]不能为空")
    @Size(max= 64,message="编码长度不能超过64")
    @Schema(description = "名字")
    @Length(max= 64,message="编码长度不能超过64")
    private String name;
    /**
     * 基本抽奖类型
     */
    @NotNull(message="[基本抽奖类型]不能为空")
    @Schema(description = "基本抽奖类型")
    private Integer type;
    /**
     *
     */
    @NotNull(message="[]不能为空")
    @Schema(description = "开始时间")
    private Date startTime;
    /**
     *
     */
    @NotNull(message="[]不能为空")
    @Schema(description = "结束时间")
    private Date endTime;
    /**
     * 模式相关参数，如阈值、等级权重等
     */
    @NotNull(message="[模式相关参数，如阈值、等级权重等]不能为空")
    @Schema(description ="模式相关参数，如阈值、等级权重等")
    private Object ruleConfig;


}
