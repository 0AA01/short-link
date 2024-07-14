package com.aa03.shortlink.project.test;

public class GenerateTable {

    public static final String SQL = "create table t_link_stats_today_%s\n" +
            "(\n" +
            "    id             bigint auto_increment comment 'ID'\n" +
            "        primary key,\n" +
            "    gid            varchar(32) default 'default' null comment '分组标识',\n" +
            "    full_short_url varchar(128)                  null comment '短链接',\n" +
            "    date           date                          null comment '日期',\n" +
            "    today_pv       int         default 0         null comment '今日PV',\n" +
            "    today_uv       int         default 0         null comment '今日UV',\n" +
            "    today_uip      int         default 0         null comment '今日IP数',\n" +
            "    create_time    datetime                      null comment '创建时间',\n" +
            "    update_time    datetime                      null comment '修改时间',\n" +
            "    del_flag       tinyint(1)                    null comment '删除标识 0：未删除 1：已删除',\n" +
            "    constraint idx_unique_today_stats\n" +
            "        unique (full_short_url, gid, date)\n" +
            ");\n" +
            "\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.println(String.format(SQL, i));
        }
    }
}
