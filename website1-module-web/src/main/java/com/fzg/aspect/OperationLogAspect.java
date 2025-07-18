package com.fzg.aspect;

import com.fzg.annotation.OperationLogAnnotation;
import com.fzg.model.OperationLog;
import com.fzg.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private OperationLogService operationLogService;

    @Around("@annotation(com.fzg.annotation.OperationLogAnnotation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLogAnnotation annotation = signature.getMethod().getAnnotation(OperationLogAnnotation.class);

        // 获取注解信息
        String operationDesc = annotation.operationDesc();
        String operationType = annotation.operationType();

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 记录日志
        OperationLog log = new OperationLog();
        log.setOperationType(operationType);
        log.setOperationDetail(operationDesc);
        log.setIpAddress(getClientIp());
        log.setCreatedAt(LocalDateTime.now());

        operationLogService.save(log); // 保存日志

        return result;
    }

    private String getClientIp() {
        return request.getRemoteAddr();
    }
}