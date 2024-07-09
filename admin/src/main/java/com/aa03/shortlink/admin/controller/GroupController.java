package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupSavaReqDto;
import com.aa03.shortlink.admin.dto.resp.ShortLinkGroupRespDto;
import com.aa03.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接分组管理控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     *
     * @param requestParam 新增短链接分组请求参数
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSavaReqDto requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询用户短链接分组集合
     *
     * @return 用户短链接分组集合
     */
    @GetMapping("/api/short-link/group")
    public Result<List<ShortLinkGroupRespDto>> listGroup() {
        List<ShortLinkGroupRespDto> result = groupService.listGroup();
        return Results.success(result);
    }
}
