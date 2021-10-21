package com.feng.springcloud.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Description: 进行token校验
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 16:15
 * @since JDK 1.8
 */
public class TokenInterceptor implements HandlerInterceptor {
    private final static String target = "2daf4qrfdagag432r1235";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        Object token = request.getAttribute("token");
        if (!target.equals(token)){
            response.getWriter().print("token错误，请查看！");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
