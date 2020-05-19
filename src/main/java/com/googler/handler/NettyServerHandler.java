package com.googler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        GeneralResponse generalResponse;
        // 错误处理,响应结果
        generalResponse = new GeneralResponse(HttpResponseStatus.BAD_REQUEST,
                "Please check your request method and URL", null);
        ResponseUtil.response(ctx, request, generalResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.warn("NettyServerHandler catch exception!!!", e);
        ctx.close();
    }
}
