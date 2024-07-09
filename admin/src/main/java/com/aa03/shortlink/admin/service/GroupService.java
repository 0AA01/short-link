package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.ShortLinkGroupRespDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 查询用户短链接分组集合
     *
     * @return 用户短链接分组集合
     */
    List<ShortLinkGroupRespDto> listGroup();

    /**
     * 修改短链接分组名称
     *
     * @param requestParam 修改短链接分组名称请求参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDto requestParam);
}
