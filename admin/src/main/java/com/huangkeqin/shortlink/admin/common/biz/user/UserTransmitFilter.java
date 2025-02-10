package com.huangkeqin.shortlink.admin.common.biz.user;


import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.huangkeqin.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    /**
     * 执行过滤操作，验证用户登录信息并设置用户上下文
     *
     * @param servletRequest Servlet请求对象，用于获取请求中的头部信息
     * @param servletResponse Servlet响应对象，用于向客户端发送响应
     * @param filterChain 过滤链对象，用于将请求传递给下一个过滤器或目标资源
     * @throws ServletException 如果过滤过程中发生Servlet异常
     * @throws IOException 如果过滤过程中发生I/O异常
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        // 将通用的ServletRequest和ServletResponse转换为HTTP特定的类型
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // 从请求头中获取用户名和token
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");
        // 从Redis中根据用户名和token获取用户信息JSON字符串
        Object userInfoJsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
        // 如果用户信息存在，则解析并设置用户上下文
        if (userInfoJsonStr != null) {
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
        // 执行过滤链的下一个过滤器或目标资源
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // 移除用户上下文，避免内存泄漏
            UserContext.removeUser();
        }
    }
}
