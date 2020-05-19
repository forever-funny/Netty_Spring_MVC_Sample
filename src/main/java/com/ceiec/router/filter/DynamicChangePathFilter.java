package com.ceiec.router.filter;

import com.ceiec.router.config.AppConfig;
import com.ceiec.router.netty.handler.FilterNettyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.*;
import java.io.IOException;

/**
 * desc:
 * filter需要手动在{@link AppConfig#getApplicationFilterConfigRegistry()}进行注册
 * netty会将请求传入{@link FilterNettyHandler},该handler内部会创建 filter 链，执行这些filter
 *
 * @author: caokunliang
 * creat_date: 2018/1/4
 * creat_time: 17:33
 **/
@Slf4j
public class DynamicChangePathFilter implements Filter, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE - 1;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        MockHttpServletRequest mockHttpServletRequest = (MockHttpServletRequest) request;
        /**
         * 这里，可以根据需要，修改请求路径;我这里写死为true
         */
        if (true){
            mockHttpServletRequest.setRequestURI("/migrate" + mockHttpServletRequest.getRequestURI());
            log.info("path changed to :{}",mockHttpServletRequest.getRequestURI());
        }


        chain.doFilter(mockHttpServletRequest, response);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getOrder() {
        return order;
    }

}