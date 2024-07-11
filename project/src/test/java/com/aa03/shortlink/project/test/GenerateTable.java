package com.aa03.shortlink.project.test;

public class GenerateTable {

    public static final String SQL = "create table t_link_%s\n" +
            "(\n" +
            "    id              bigint auto_increment comment 'ID'\n" +
            "        primary key,\n" +
            "    gid             varchar(32)   null comment '分组标识',\n" +
            "    domain          varchar(128)  null comment '域名',\n" +
            "    short_uri       varchar(8)    null comment '短链接',\n" +
            "    full_short_url  varchar(128)  null comment '完整短链接',\n" +
            "    origin_url      varchar(1024) null comment '原始短链接',\n" +
            "    favicon         varchar(256)  null comment '网站图标',\n" +
            "    click_num       int default 0 null comment '点击量',\n" +
            "    enable_status   tinyint(1)    null comment '启用标识 0：表示为启用 1：表示未启用',\n" +
            "    create_type     tinyint(1)    null comment '创建类型：0表示接口创建 1表示控制台创建',\n" +
            "    valid_date      datetime      null comment '有效期',\n" +
            "    valid_date_type tinyint(1)    null comment '有效期类型  0有效  1无效',\n" +
            "    `describe`      varchar(1024) null comment '描述',\n" +
            "    create_time     datetime      null comment '创建时间',\n" +
            "    update_time     datetime      null comment '更新时间',\n" +
            "    del_flag        tinyint(1)    null comment '是否删除：0表示未删除 1表示删除',\n" +
            "    constraint idex_unique_full_short_url\n" +
            "        unique (full_short_url)\n" +
            ")\n" +
            "    collate = utf8mb4_bin;\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.println(String.format(SQL, i));
        }
    }
}
