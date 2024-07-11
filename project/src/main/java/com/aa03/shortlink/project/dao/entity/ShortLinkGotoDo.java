package com.aa03.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 短链接跳转实体
 */
@Data
@TableName("t_link_goto")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkGotoDo {

    /**
     * ID
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
