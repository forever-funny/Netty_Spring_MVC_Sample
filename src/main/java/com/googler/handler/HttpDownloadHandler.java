package com.googler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;

public class HttpDownloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpDownloadHandler.class);

    public HttpDownloadHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        String uri = request.uri();
        logger.info("uri:{}", uri);
        GeneralResponse generalResponse;
        logger.info("show header:{}", request.headers());
        generalResponse = new GeneralResponse(HttpResponseStatus.NOT_FOUND, "nothing", null);
        ctx.writeAndFlush("nothing");
        ResponseUtil.response(ctx, request, generalResponse);
    }

    private void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        logger.warn("download handler catch exception!!!", e);
        ctx.close();
    }
}
