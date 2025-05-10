package com.lotterysystem.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lotterysystem.server.pojo.entity.Lottery;
import com.lotterysystem.server.service.LotteryService;
import com.lotterysystem.server.mapper.LotteryMapper;
import org.springframework.stereotype.Service;

/**
* @author thanw
* @description 针对表【lottery】的数据库操作Service实现
* @createDate 2025-05-10 18:35:53
*/
@Service
public class LotteryServiceImpl extends ServiceImpl<LotteryMapper, Lottery>
    implements LotteryService{


}




