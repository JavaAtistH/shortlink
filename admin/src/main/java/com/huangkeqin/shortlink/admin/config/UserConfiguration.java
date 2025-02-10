package com.huangkeqin.shortlink.admin.config;


import com.huangkeqin.shortlink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 用户配置自动装配
 */
@Configuration
public class UserConfiguration {

    /**
     * 用户信息传递过滤器
     */
    /**
     * 配置全局用户传输过滤器
     *
     * 该方法用于创建和配置一个FilterRegistrationBean，其中包含了一个UserTransmitFilter实例
     * UserTransmitFilter的作用是在用户相关请求传递过程中，进行必要的处理或验证
     *
     * @param stringRedisTemplate Redis模板，用于UserTransmitFilter与Redis数据库之间的交互
     * @return 返回配置好的FilterRegistrationBean实例
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        // 创建FilterRegistrationBean对象
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        // 设置过滤器实例，UserTransmitFilter需要一个StringRedisTemplate参数
        registration.setFilter(new UserTransmitFilter(stringRedisTemplate));
        // 添加URL模式，/*表示该过滤器将应用于所有请求
        registration.addUrlPatterns("/*");
        // 设置过滤器的执行顺序，0表示最高优先级
        registration.setOrder(0);
        // 返回配置好的FilterRegistrationBean对象
        return registration;
    }
}

