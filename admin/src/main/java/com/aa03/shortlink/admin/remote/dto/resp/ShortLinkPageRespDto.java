package com.aa03.shortlink.admin.remote.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 短链接分页返回响应参数
 */
@Data
public class ShortLinkPageRespDto {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始短链接
     */
    private String originUrl;

    /**
     * 网站图标
     */
    private String favicon;

    /**
     * 点击量
     */
    private Integer clickNum;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 历史PV
     */
    private Integer totalPv;

    /**
     * 当日PV
     */
    private Integer toDayPv;

    /**
     * 历史UV
     */
    private Integer totalUv;

    /**
     * 当日UV
     */
    private Integer toDayUv;

    /**
     * 历史UIP
     */
    private Integer totalUip;

    /**
     * 当日UIP
     */
    private Integer toDayUip;

}