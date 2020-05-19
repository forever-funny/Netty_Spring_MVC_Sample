package com.ceiec.router.netty;

import com.ceiec.router.netty.handler.DispatcherServletHandler;
import com.ceiec.router.netty.handler.GenerateServletRequestHandler;
import com.ceiec.router.utils.NamedThreadFactory;
import com.ceiec.router.utils.SpringContextUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.servlet.ServletException;

public class DispatcherServletChannelInitializer extends ChannelInitializer<SocketChannel> {


	private static DefaultEventLoopGroup eventExecutors = new DefaultEventLoopGroup(4,new NamedThreadFactory("business_servlet"));


	public DispatcherServletChannelInitializer() throws ServletException {

	}

	@Override
	public void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();

        // 对通信数据进行编解码
        pipeline.addLast(new HttpServerCodec());

        // 把多个HTTP请求中的数据组装成一个
        pipeline.addLast(new HttpObjectAggregator(65536));

        // 用于处理大的数据流
        pipeline.addLast(new ChunkedWriteHandler());

        /**
         * 生成servlet使用的request
         */
		pipeline.addLast("GenerateServletRequestHandler", new GenerateServletRequestHandler());

        /**
         * 过滤器处理器，模拟servlet中的 filter 链
         */
        // FilterNettyHandler filterNettyHandler = SpringContextUtils.getApplicationContext().getBean(FilterNettyHandler.class);
        // pipeline.addLast("FilterNettyHandler", filterNettyHandler);

        /**
         * 真正的业务handler，转交给：spring mvc的dispatcherServlet 处理
         */
        DispatcherServletHandler dispatcherServletHandler = SpringContextUtils.getApplicationContext().getBean(DispatcherServletHandler.class);
//        pipeline.addLast("dispatcherServletHandler", dispatcherServletHandler);
        /**
         * 使用下面的重载方法，第一个参数为线程池，则这里会异步执行我们的业务逻辑，正常也应该这样，避免长时间阻塞io线程
         */
		pipeline.addLast(eventExecutors,"handler", dispatcherServletHandler);
	}


}
