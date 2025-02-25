package com.huangkeqin.shortlink.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库配置类，用于配置MyBatis-Plus的相关功能
 * 代码配置了MyBatis-Plus的分页插件
 */
@Configuration
public class DataBaseConfiguration {
    /**
     * 创建并配置MyBatis-Plus拦截器
     *
     * @return MybatisPlusInterceptor对象，包含分页拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建MyBatis-Plus拦截器实例
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 返回配置好的拦截器实例
        return interceptor;
    }
}
