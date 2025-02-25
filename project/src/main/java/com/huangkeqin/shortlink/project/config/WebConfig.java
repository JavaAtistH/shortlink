package com.huangkeqin.shortlink.project.config;

import com.huangkeqin.shortlink.project.common.biz.user.UserTransmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebConfig {
    @Autowired
    private UserTransmitInterceptor userTransmitInterceptor;
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTransmitInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/static/**", "/public/**"); // 过滤静态资源
    }
}
