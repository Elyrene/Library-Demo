package com.Library.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// 统一返回
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataInfo<T> {
    // 代码
    private Integer code;

    // 数据
    private String mes;

    // 分页
    private Long count;

    // 数据
    private T data;

    public static <T> DataInfo<T> ok() {
        return new DataInfo<>(0, "ok", null, null);
    }

    public static <T> DataInfo<T> ok(T data)  {
        return new DataInfo<>(0, "ok", null, data);
    }

    public static <T> DataInfo<T> ok(T data, long count) {
        return new DataInfo<>(0, "ok", count, data);
    }

    public static  <T> DataInfo<T> fail(String mes) {
        return  new DataInfo<T>(-1, mes, null, null );
    }

    public static <T> DataInfo<T> fail(Integer code, String msg) {
        return new DataInfo<>(code, msg, null, null); }
}
