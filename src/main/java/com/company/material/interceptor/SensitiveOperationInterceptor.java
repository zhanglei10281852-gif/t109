package com.company.material.interceptor;

import com.company.material.annotation.SensitiveOperation;
import com.company.material.filter.RequestCachingFilter;
import com.company.material.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SensitiveOperationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        SensitiveOperation annotation = handlerMethod.getMethodAnnotation(SensitiveOperation.class);
        if (annotation == null) {
            return true;
        }
        if (hasConfirmFlag(request)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + annotation.message() + "，请在请求中添加 confirm=true 参数进行确认\"}");
        return false;
    }

    private boolean hasConfirmFlag(HttpServletRequest request) {
        String confirm = request.getParameter("confirm");
        if (confirm != null && "true".equalsIgnoreCase(confirm)) {
            return true;
        }
        if (request instanceof RequestCachingFilter.CachedBodyHttpServletRequest) {
            RequestCachingFilter.CachedBodyHttpServletRequest wrapped = (RequestCachingFilter.CachedBodyHttpServletRequest) request;
            String body = wrapped.getCachedBody();
            if (body != null && !body.isEmpty()) {
                if (body.contains("\"confirm\":true") || body.contains("\"confirm\": true")) {
                    return true;
                }
                try {
                    Map<String, Object> bodyMap = JsonUtil.fromJson(body, new TypeReference<Map<String, Object>>() {});
                    if (bodyMap != null) {
                        Object confirmVal = bodyMap.get("confirm");
                        if (confirmVal != null && (Boolean.TRUE.equals(confirmVal) || "true".equalsIgnoreCase(confirmVal.toString()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
}
