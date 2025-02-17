package com.baiyi.opscloud.aspect;

import com.baiyi.opscloud.common.exception.common.CommonRuntimeException;
import com.baiyi.opscloud.domain.annotation.BusinessType;
import com.baiyi.opscloud.domain.annotation.RevokeUserPermission;
import com.baiyi.opscloud.domain.types.BusinessTypeEnum;
import com.baiyi.opscloud.facade.user.UserPermissionFacade;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2021/8/18 2:28 下午
 * @Version 1.0
 */
@Aspect
@Component
@Slf4j
public class RevokeUserPermissionAspect {

    @Resource
    private UserPermissionFacade userPermissionFacade;

    @Pointcut(value = "@annotation(com.baiyi.opscloud.domain.annotation.RevokeUserPermission)")
    public void annotationPoint() {
    }

    @Around("@annotation(revokeUserPermission)")
    public Object around(ProceedingJoinPoint joinPoint, RevokeUserPermission revokeUserPermission) throws CommonRuntimeException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] params = methodSignature.getParameterNames();// 获取参数名称
        Object[] args = joinPoint.getArgs();// 获取参数值
        if (params != null && params.length != 0) {
            Integer businessId = Integer.valueOf(args[0].toString());
            if (revokeUserPermission.value() == BusinessTypeEnum.COMMON) {
                // 通过@BusinessType 获取业务类型
                if (joinPoint.getTarget().getClass().isAnnotationPresent(BusinessType.class)) {
                    BusinessType businessType = joinPoint.getTarget().getClass().getAnnotation(BusinessType.class);
                    doRevoke(businessType.value().getType(), businessId);
                }
            } else {
                doRevoke(revokeUserPermission.value().getType(), businessId);
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new CommonRuntimeException(e.getMessage());
        }
    }

    private void doRevoke(Integer businessType, Integer businessId) {
        log.info("撤销当前业务对象的所有用户授权: businessType = {} , businessId = {}", businessType, businessId);
        userPermissionFacade.revokeUserPermissionByBusiness(businessType, businessId);
    }

}
