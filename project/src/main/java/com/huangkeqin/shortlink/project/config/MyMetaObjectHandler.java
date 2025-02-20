package com.huangkeqin.shortlink.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatisPlus自动填充字段
 * @author huangkeqin
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 在插入操作时填充通用字段
     * 该方法主要用于在执行插入操作时，自动填充一些通用字段，如创建时间、更新时间和删除标志
     * 使用 MetaObject 来操作元数据，实现对字段的动态填充
     *
     * @param metaObject 元数据对象，用于获取和设置字段值
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间字段，如果字段不存在或不匹配类型，则忽略
        this.strictInsertFill(metaObject, "createTime",Date::new,Date.class);
        // 填充更新时间字段，如果字段不存在或不匹配类型，则忽略
        this.strictInsertFill(metaObject, "updateTime",Date::new,Date.class);
        // 填充删除标志字段，设置为 0，表示未删除，如果字段不存在或不匹配类型，则忽略
        this.strictInsertFill(metaObject, "delFlag", () -> 0 ,Integer.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateTime", Date::new,Date.class);
    }
}
