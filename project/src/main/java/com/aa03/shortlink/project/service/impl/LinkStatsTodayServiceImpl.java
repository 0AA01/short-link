package com.aa03.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aa03.shortlink.project.dao.entity.LinkStatsTodayDo;
import com.aa03.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import com.aa03.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDo> implements LinkStatsTodayService {
}
