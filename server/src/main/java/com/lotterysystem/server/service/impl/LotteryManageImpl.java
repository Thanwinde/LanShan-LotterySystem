package com.lotterysystem.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lotterysystem.server.mapper.LotteryManageMapper;
import com.lotterysystem.server.pojo.dto.LotteryDTO;
import com.lotterysystem.server.pojo.dto.Result;
import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.service.LotteryManage;

/**
 * @author nsh
 * @data 2025/5/10 14:43
 * @description
 **/
public class LotteryManageImpl extends ServiceImpl<LotteryManageMapper, Lottery> implements LotteryManage {

    @Override
    public Result addLottery(LotteryDTO lotteryDTO) {
        Lottery lottery = BeanUtil.toBean(lotteryDTO, Lottery.class);

    }
}
