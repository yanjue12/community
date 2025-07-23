/*
package com.fzg.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    public Result notLoginException(NotLoginException e) {
        log.error("用户未登录:{}",e.getMessage());
        return Result.fail(EnumReturn.UN_AUTHORIZATION);
    }



    @ExceptionHandler(BusinessException.class)
    public Result<String> businessException(BusinessException e){
        log.error(e.getMessage());
        return Result.fail(EnumReturn.OPERATION_FAIL.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> runtimeException(RuntimeException e){
        log.error(e.getMessage());
        return Result.fail(EnumReturn.ACCESS_DENY_FAIL.SERVER_INNER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> accessDeniedException(AccessDeniedException e){
        log.error(e.getMessage());
        e.printStackTrace();
        return null;
    }

}
*/
