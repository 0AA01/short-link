package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDo> {

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

}
