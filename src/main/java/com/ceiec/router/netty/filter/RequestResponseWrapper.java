package com.ceiec.router.netty.filter;

import lombok.Data;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * desc:
 * 包装类，主要是netty在给下一个handler传递参数时，只支持一个参数
 * @author : caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 14:00
 **/
@Data
public class RequestResponseWrapper {
    ServletRequest servletRequest;

    ServletResponse servletResponse;
}
