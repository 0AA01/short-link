package com.aa03.shortlink.project.test;

public class GenerateTable {

    public static final String SQL = "create table t_link_goto_%s\n" +
            "(\n" +
            "    id             bigint auto_increment comment 'ID'\n" +
            "        primary key,\n" +
            "    full_short_url varchar(32)  null comment '完整短链接',\n" +
            "    gid            varchar(128) null comment '分组标识',\n" +
            "    constraint T_LINK_GOTO_UNIQUE_FULL_SHORT_URL\n" +
            "        unique (full_short_url)\n" +
            ")\n" +
            "    collate = utf8mb3_bin;\n" +
            "\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.println(String.format(SQL, i));
        }
    }
}
