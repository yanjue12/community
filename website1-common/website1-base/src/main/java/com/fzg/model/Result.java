package com.fzg.model;

import com.fzg.enums.EnumReturn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目统一响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@ApiModel("项目统一响应结果对象")
public class Result<T> {

   // @ApiModelProperty("状态码")
    private Integer code = 200;

   // @ApiModelProperty("消息")
    private String msg = "success";

  //  @ApiModelProperty("数据")
    private T data;

    /**
     * 成功
     * @param data 数据
     */
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    /**
     * 失败
     * @param code 状态码
     * @param msg 信息
     */
    public static <T> Result<T> fail(Integer code,String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> fail(EnumReturn EnumReturn){
        Result<T> result = new Result<>();
        result.setCode(EnumReturn.getCode());
        result.setMsg(EnumReturn.getDesc());
        result.setData(null);
        return result;
    }

    /**
     * 处理用户的操作
     * @param flag
     * @return
     */
    public static Result<String> handle(Boolean flag) {
        if (flag){
            return Result.success(null);
        }
        return Result.fail(EnumReturn.OPERATION_FAIL);
    }
}