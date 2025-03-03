package com.huangkeqin.shortlink.admin.common.biz.user;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.huangkeqin.shortlink.admin.common.convention.exception.ClientException;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

/**
 * 用户信息传输过滤器
 */
public class UserTransmitFilter implements Filter {
    /**
     * 不需要拦截的三个接口
     * 用户注册和登陆，以及检查用户名是否可用（检查用户名是否可用就是注册里面的）
     */
/*    private static final List<String> IGNORE_URI= Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/v1/admin/user/has-username"
    );*/
    /**
     * 执行过滤操作，验证用户登录信息并设置用户上下文
     *
     * @param servletRequest Servlet请求对象，用于获取请求中的头部信息
     * @param servletResponse Servlet响应对象，用于向客户端发送响应
     * @param filterChain 过滤链对象，用于将请求传递给下一个过滤器或目标资源
     * @throws ServletException 如果过滤过程中发生Servlet异常
     * @throws IOException 如果过滤过程中发生I/O异常
     */
    /**
     * 在过滤器中应该将登陆接口放行，不然会出现错误，如果尝试登陆，会到过滤器中，此时请求头中并没有信息，查询redis的参数为null,会报错
     * 当登陆之后，后端会将token返回给前端,前端存储器起来，每次请求就放在请求头中，过滤器此时就会从请求头中获取token,检查是否合法，决定是否放行
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader("username");
        if (StrUtil.isNotBlank(username)) {
            String userId = httpServletRequest.getHeader("userId");
            String realName = httpServletRequest.getHeader("realName");
            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception{
        PrintWriter writer=null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try{
            writer=response.getWriter();
            writer.write(json);
        }catch (IOException e){
        }finally {
            if(writer!=null){
                writer.close();
            }
        }
    }
}
