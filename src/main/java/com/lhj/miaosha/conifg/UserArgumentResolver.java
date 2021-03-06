package com.lhj.miaosha.conifg;

import com.alibaba.druid.util.StringUtils;
import com.lhj.miaosha.access.UserContext;
import com.lhj.miaosha.domain.MiaoshaUser;
import com.lhj.miaosha.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ：LHJ
 * @date ：2019/7/26 20:22
 * @description：${description}
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    MiaoshaUserService userService;

    /**
     * 当参数类型为MiaoshaUser才做处理
     *
     * @param methodParameter
     * @return
     */
    //当supportsParameter返回true时，才会调用resolveArgument方法
    //测试此 service(如"/goods/to_list") 是否能使用指定的参数。如果此服务不能使用该参数，则返回 false。
    // 如果此服务能使用该参数、快速测试不可行或状态未知，则返回 true。
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //获取参数类型
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    /**
     * 思路：先获取到已有参数HttpServletRequest，从中获取到token，再用token作为key从redis拿到User，而HttpServletResponse作用是为了延迟有效期
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
//        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

//        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
//        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
//        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//            return null;
//        }
//        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//        return userService.getByToken(response, token);
        return UserContext.gerUser();
    }

    //遍历所有cookie，找到需要的那个cookie
//    private String getCookieValue(HttpServletRequest request, String cookiName) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null || cookies.length <= 0) {
//            return null;
//        }
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals(cookiName)) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
}

