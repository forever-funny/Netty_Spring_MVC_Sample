package com.ceiec.router.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * desc: 将http编解码器传过来的FullHttpRequest，转换为 MockHttpServletRequest，再传递给下一个handler
 * @author: caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 15:44
 **/
@Slf4j
public class GenerateServletRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    /**
     * 会话ID
     */
    private final static String SESSION_KEY = "sessionId";


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            sendError(channelHandlerContext, BAD_REQUEST);
            return;
        }

        // 设置请求的会话id
        String token = UUID.randomUUID().toString().replace("-", "");
        MDC.put(SESSION_KEY, token);

        String remoteIP = getRemoteIP(fullHttpRequest, channelHandlerContext);
        MockHttpServletRequest servletRequest = createServletRequest(fullHttpRequest);
        String s = fullHttpRequest.content().toString(CharsetUtil.UTF_8);

        log.info("{},request:{},param:{}", remoteIP, fullHttpRequest.uri(), s);
        try {
            channelHandlerContext.fireChannelRead(servletRequest);
        } finally {
            // 删除SessionId
//            log.warn("token deleted");
            MDC.remove(SESSION_KEY);
        }

    }


    private MockHttpServletRequest createServletRequest(FullHttpRequest fullHttpRequest) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.uri()).build();

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(uriComponents.getPath());
        servletRequest.setPathInfo(uriComponents.getPath());
        servletRequest.setMethod(fullHttpRequest.method().name());

        if (uriComponents.getScheme() != null) {
            servletRequest.setScheme(uriComponents.getScheme());
        }
        if (uriComponents.getHost() != null) {
            servletRequest.setServerName(uriComponents.getHost());
        }
        if (uriComponents.getPort() != -1) {
            servletRequest.setServerPort(uriComponents.getPort());
        }

        for (String name : fullHttpRequest.headers().names()) {
            servletRequest.addHeader(name, fullHttpRequest.headers().get(name));
        }


        ByteBuf bbContent = fullHttpRequest.content();
        String s = bbContent.toString(CharsetUtil.UTF_8);
        servletRequest.setContent(s.getBytes(CharsetUtil.UTF_8));


        if (uriComponents.getQuery() != null) {
            String query = UriUtils.decode(uriComponents.getQuery(), "UTF-8");
            servletRequest.setQueryString(query);
        }

        for (Entry<String, List<String>> entry : uriComponents.getQueryParams().entrySet()) {
            for (String value : entry.getValue()) {
                servletRequest.addParameter(
                        UriUtils.decode(entry.getKey(), "UTF-8"),
                        UriUtils.decode(value, "UTF-8"));
            }
        }

        return servletRequest;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8);

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                status,
                content
        );
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }


    public String getRemoteIP(FullHttpRequest httpRequest, ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
        String ip = "";
        try {
            String ipForwarded = httpRequest.headers().get("x-forwarded-for");
            if (StringUtils.isBlank(ipForwarded) || "unknown".equalsIgnoreCase(ipForwarded)) {
                InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
                ip = insocket.getAddress().getHostAddress();
            } else {
                ip = ipForwarded;
            }
        } catch (Exception e) {
            log.error("getRemoteIP(): get remote ip fail!", e);
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
