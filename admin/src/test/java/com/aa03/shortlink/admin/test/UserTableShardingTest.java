package com.aa03.shortlink.admin.test;

import org.springframework.util.StringUtils;

public class UserTableShardingTest {

    public static final String SQL = "create table t_group_%s\n" +
            "(\n" +
            "    id          bigint auto_increment comment 'ID'\n" +
            "        primary key,\n" +
            "    gid         varchar(32)  null comment '分组标识',\n" +
            "    name        varchar(64)  null comment '分组名称',\n" +
            "    username    varchar(256) null comment '创建用户',\n" +
            "    sort_order  int          null comment '分组排序',\n" +
            "    create_time datetime     null comment '创建时间',\n" +
            "    update_time datetime     null comment '更新时间',\n" +
            "    del_flag    tinyint(1)   null comment '删除标识 0：表示未删除 1：表示已删除',\n" +
            "    constraint idx_unique_username_gid\n" +
            "        unique (gid, username)\n" +
            ");\n" +
            "\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.println(String.format(SQL, i));
        }
    }
}
