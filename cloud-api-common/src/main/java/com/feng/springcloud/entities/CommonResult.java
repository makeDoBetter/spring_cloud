package com.feng.springcloud.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 统一json返回实体
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 10:52
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {
    private int code;

    private String message;

    private T data;

    public CommonResult(int code, String message) {
        this(code, message, null);
    }
}
