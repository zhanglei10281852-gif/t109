package com.company.material.interceptor;

import com.company.material.context.DataPermissionContext;
import com.company.material.service.DataPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class DataPermissionInterceptor implements HandlerInterceptor {

    private final DataPermissionService dataPermissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        try {
            dataPermissionService.loadCurrentUserPermission();
        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        DataPermissionContext.clear();
    }
}
