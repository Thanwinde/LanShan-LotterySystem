package com.lotterysystem.server.pojo.dto;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;



@Data
public class PrizeDTO implements Serializable {

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
    private Integer full_count;
    /**
    * 目前还剩数量
    */
    @Schema(description = "目前还剩数量")
    private Integer now_count;

}
