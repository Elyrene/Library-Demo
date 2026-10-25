package com.Library.config;

import com.Library.common.Contents;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录校验拦截器：
 * 受保护接口（/api/admins/**、/api/readers/**）必须携带有效 Session，
 * 否则直接返回 401 与统一响应体，无需各 Controller 自行判断。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(Contents.SESSION_USER);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(com.Library.common.DataInfo.fail(401, "未登录或登录已过期")));
            return false;
        }
        return true;
    }
}
