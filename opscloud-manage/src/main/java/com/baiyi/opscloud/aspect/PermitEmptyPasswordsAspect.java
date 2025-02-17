package com.baiyi.opscloud.aspect;

import com.baiyi.opscloud.common.exception.auth.AuthRuntimeException;
import com.baiyi.opscloud.domain.ErrorEnum;
import com.baiyi.opscloud.domain.annotation.PermitEmptyPasswords;
import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.param.auth.LoginParam;
import com.baiyi.opscloud.facade.auth.UserTokenFacade;
import com.baiyi.opscloud.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/4 3:44 下午
 * @Version 1.0
 */
@Aspect
@Component
@Slf4j
public class PermitEmptyPasswordsAspect {

    @Resource
    private UserService userService;

    @Resource
    private UserTokenFacade userTokenFacade;

    @Pointcut(value = "@annotation(com.baiyi.opscloud.domain.annotation.PermitEmptyPasswords)")
    public void annotationPoint() {
    }

    @Around("@annotation(permitEmptyPasswords)")
    public Object around(ProceedingJoinPoint joinPoint, PermitEmptyPasswords permitEmptyPasswords) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] params = methodSignature.getParameterNames();// 获取参数名称
        Object[] args = joinPoint.getArgs();// 获取参数值
        if (params != null && params.length != 0) {
            Object obj = args[0];
            if (obj instanceof LoginParam.Login) {
                LoginParam.Login loginParam = (LoginParam.Login) obj;
                User user = userService.getByUsername(loginParam.getUsername());
                // 判断用户是否有效
                if (user == null || !user.getIsActive())
                    throw new AuthRuntimeException(ErrorEnum.AUTH_USER_LOGIN_FAILURE);
                if (loginParam.isEmptyPassword()) {
                    if (StringUtils.isEmpty(user.getPassword()))
                        return userTokenFacade.userLogin(user);     // 空密码登录成功
                    throw new AuthRuntimeException(ErrorEnum.AUTH_USER_LOGIN_FAILURE);
                }
            }
        }
        return joinPoint.proceed();
    }

}
