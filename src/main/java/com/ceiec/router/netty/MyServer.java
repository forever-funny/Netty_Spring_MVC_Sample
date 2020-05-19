package com.ceiec.router.netty;

import com.ceiec.router.utils.NamedThreadFactory;
import com.googler.handler.NettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyServer {
	private final static Logger logger = LoggerFactory.getLogger(MyServer.class);

	private final int port;

    private static final NamedThreadFactory namedThreadFactoryForAcceptor = new NamedThreadFactory("netty_acceptor");

    private static final NamedThreadFactory namedThreadFactoryForClientChannel = new NamedThreadFactory("netty_worker");

	public MyServer(int port) {
		this.port = port;
	}


    public void run() {

		ServerBootstrap server = new ServerBootstrap();
        NioEventLoopGroup parentGroup = new NioEventLoopGroup(1,namedThreadFactoryForAcceptor);
        NioEventLoopGroup childGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(),namedThreadFactoryForClientChannel);
		try {
			server.group(parentGroup, childGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(port)
					// .childHandler(new DispatcherServletChannelInitializer())
					.childHandler(new NettyServerInitializer());

			logger.info("Netty server has started on port : " + port);

			server.bind().sync().channel().closeFuture().sync();
		}catch (Exception e){
		    logger.error("exception");
        }
		finally {
            parentGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8081;
		}
		new MyServer(port).run();
	}
}
