package com.ceiec.router.netty.handler;

import com.ceiec.router.netty.DispatcherServletChannelInitializer;
import com.ceiec.router.netty.filter.ApplicationFilterChain;
import com.ceiec.router.netty.filter.ApplicationFilterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * desc: 模拟servlet的filter链
 * netty handler链的初始化在{@link DispatcherServletChannelInitializer#initChannel(io.netty.channel.socket.SocketChannel)}
 * @author: caokunliang
 * creat_date: 2019/12/10 0010
 * creat_time: 10:14
 **/
@Slf4j
@Component
@Scope(scopeName = "prototype")
public class FilterNettyHandler extends SimpleChannelInboundHandler<MockHttpServletRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MockHttpServletRequest httpServletRequest) throws Exception {
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain(ctx,httpServletRequest);
        if (filterChain == null) {
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
