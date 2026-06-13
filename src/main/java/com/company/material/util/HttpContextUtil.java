package com.company.material.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HttpContextUtil {

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        return (String) request.getAttribute("username");
    }

    public static String getCurrentRole() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        return (String) request.getAttribute("role");
    }

    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static boolean hasConfirmFlag() {
        HttpServletRequest request = getRequest();
        if (request == null) return false;
        String confirm = request.getParameter("confirm");
        if (confirm != null && "true".equalsIgnoreCase(confirm)) {
            return true;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try {
                request.getParameterMap();
            } catch (Exception e) {
            }
        }
        return false;
    }
}
