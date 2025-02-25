package com.huangkeqin.shortlink.admin.test;

public class UserTableShardingTest {
    public static final String SQL="CREATE TABLE `t_group_%d`  (\n" +
            "  `id` bigint NOT NULL AUTO_INCREMENT,\n" +
            "  `gid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组标识',\n" +
            "  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',\n" +
            "  `username` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建分组用户名',\n" +
            "  `sort_order` int NULL DEFAULT NULL COMMENT '分组排序',\n" +
            "  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',\n" +
            "  `del_flag` tinyint NULL DEFAULT NULL COMMENT '删除标识0表示未删除1表示已删除',\n" +
            "  PRIMARY KEY (`id`) USING BTREE,\n" +
            "  UNIQUE INDEX `idx_unique_username_gid`(`gid` ASC, `username` ASC) USING BTREE\n" +
            ") ENGINE = InnoDB AUTO_INCREMENT = 1888839915138572290 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL)+"%n",i);
        }

    }
}
