package com.fzg.handler;

import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(BusinessException.class)
    public Result<String> businessException(BusinessException e){
        log.error(e.getMessage());
        return Result.fail(EnumReturn.OPERATION_FAIL.getCode(),e.getMessage());
    }

   /* @ExceptionHandler(RuntimeException.class)
    public Result<String> runtimeException(RuntimeException e){
        log.error(e.getMessage());
        return Result.fail(EnumReturn.ACCESS_DENY_FAIL.SERVER_INNER_ERROR);
    }*/

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> accessDeniedException(AccessDeniedException e){
        log.error(e.getMessage());
        e.printStackTrace();
        return null;
    }

}
