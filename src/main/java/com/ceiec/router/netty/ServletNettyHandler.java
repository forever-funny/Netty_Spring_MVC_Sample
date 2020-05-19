package com.ceiec.router.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.Servlet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class ServletNettyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final Servlet servlet;

    /**
     * 会话ID
     */
    private final static String SESSION_KEY = "sessionId";

	public ServletNettyHandler(Servlet servlet) {
		this.servlet = servlet;
	}

	private MockHttpServletRequest createServletRequest(FullHttpRequest fullHttpRequest) {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.getUri()).build();

		MockHttpServletRequest servletRequest = new MockHttpServletRequest();
		servletRequest.setRequestURI(uriComponents.getPath());
		servletRequest.setPathInfo(uriComponents.getPath());
		servletRequest.setMethod(fullHttpRequest.getMethod().name());

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
		fullHttpResponse.headers().add(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
		if (!fullHttpRequest.getDecoderResult().isSuccess()) {
			sendError(channelHandlerContext, BAD_REQUEST);
			return;
		}

        // 设置请求的会话id
        String token = UUID.randomUUID().toString().replace("-", "");
        MDC.put(SESSION_KEY, token);

        String remoteIP = getRemoteIP(fullHttpRequest, channelHandlerContext);
        MockHttpServletRequest servletRequest = createServletRequest(fullHttpRequest);
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        String s = fullHttpRequest.content().toString(CharsetUtil.UTF_8);

        log.info("{},request:{},param:{}",remoteIP,fullHttpRequest.uri(),s);
        this.servlet.service(servletRequest, servletResponse);

        // 删除SessionId
        MDC.remove(SESSION_KEY);

		HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse.getStatus());
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		for (String name : servletResponse.getHeaderNames()) {
			for (Object value : servletResponse.getHeaderValues(name)) {
				response.headers().add(name, value);
			}
		}
		response.headers().add("Content-Type","application/json;charset=UTF-8");

		// Write the initial line and the header.
		channelHandlerContext.write(response);

		InputStream contentStream = new ByteArrayInputStream(servletResponse.getContentAsByteArray());

		// Write the content and flush it.
        ChunkedStream stream = new ChunkedStream(contentStream);
//        new String(stream, "utf-8");
        ChannelFuture writeFuture = channelHandlerContext.writeAndFlush(stream);
		writeFuture.addListener(ChannelFutureListener.CLOSE);
	}

    public String getRemoteIP(FullHttpRequest httpRequest,ChannelHandlerContext channelHandlerContext) {
        Channel channel = channelHandlerContext.channel();
        String ip = "";
        try{
            String ipForwarded = httpRequest.headers().get("x-forwarded-for");
            if (StringUtils.isEmpty(ipForwarded) || "unknown".equalsIgnoreCase(ipForwarded)) {
                InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
                ip = insocket.getAddress().getHostAddress();
            } else {
                ip = ipForwarded;
            }
        }catch(Exception e){
            log.error("getRemoteIP(): get remote ip fail!", e);
        }
        if("0:0:0:0:0:0:0:1".equals(ip)){
            ip = "127.0.0.1";
        }
        return ip;
    }
}
