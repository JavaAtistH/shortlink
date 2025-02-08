package com.huangkeqin.shortlink.admin.test;

public class UserTableShardingTest {
    public static final String SQL="CREATE TABLE `t_user_%d`  (\n" +
            "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `username` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',\n" +
            "  `password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',\n" +
            "  `real_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',\n" +
            "  `phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码',\n" +
            "  `mail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',\n" +
            "  `deletion_time` bigint NULL DEFAULT NULL COMMENT '删除时间',\n" +
            "  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',\n" +
            "  `del_flag` tinyint NULL DEFAULT NULL COMMENT '删除标识0表示未删除1表示已删除',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "UNIQUE KEY `idx_unique_username` (`username`) USING BTREE\n"+
            ") ENGINE = InnoDB AUTO_INCREMENT = 854159363 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL)+"%n",i);
        }

    }
}
