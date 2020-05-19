package com.ceiec.router.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.ceiec.router.netty.DispatcherServletChannelInitializer;
import com.ceiec.router.netty.filter.RequestResponseWrapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 *
 * desc:
 * 请求交给，Spring的dispatcherServlet处理
 * netty handler链的初始化在{@link DispatcherServletChannelInitializer#initChannel(io.netty.channel.socket.SocketChannel)}
 * @author: caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 15:46
 **/
@Slf4j
@Component
@Scope(scopeName = "prototype")
public class DispatcherServletHandler extends SimpleChannelInboundHandler<RequestResponseWrapper> {

    @Autowired
    private DispatcherServlet dispatcherServlet;


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RequestResponseWrapper requestResponseWrapper) throws Exception {
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) requestResponseWrapper.getServletRequest();
        MockHttpServletResponse servletResponse = (MockHttpServletResponse) requestResponseWrapper.getServletResponse();
        dispatcherServlet.service(servletRequest, servletResponse);

		HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse.getStatus());
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		for (String name : servletResponse.getHeaderNames()) {
            response.headers().add(name, servletResponse.getHeader(name));
		}
		response.headers().add("Content-Type","application/json;charset=UTF-8");
		// Write the initial line and the header.
		// ctx.write(response);
		// InputStream contentStream = new ByteArrayInputStream(servletResponse.getContentAsByteArray());
        // ChunkedStream stream = new ChunkedStream(contentStream);
        // ChannelFuture writeFuture = channelHandlerContext.writeAndFlush(stream);
		final JSONObject result = new JSONObject();
		result.put("code", 200);
		result.put("reason", "Data received successfully");
		final ChannelFuture writeFuture = ctx.writeAndFlush(result);
		// writeFuture.addListener(ChannelFutureListener.CLOSE);
	}
}
