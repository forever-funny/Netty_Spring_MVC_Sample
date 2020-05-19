package com.googler.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyServerInitializer.class);
    
    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        // HTTP服务的解码器
        p.addLast(new HttpServerCodec(4096, 8192, 1024 * 1024 * 10))
                // 用于上传文件
                // .addLast(new HttUploadHandler())
                // HTTP消息的合并处理
                .addLast(new HttpObjectAggregator(1024 * 1024 * 10))
                // 新增ChunkedHandler，主要作用是支持异步发送大的码流（例如大文件传输），但是不占用过多的内存，防止发生java内存溢出错误
                .addLast(new ChunkedWriteHandler())
                // 用于下载文件
                .addLast(new HttpDownloadHandler())
                // 服务器端逻辑处理
                .addLast(new NettyServerHandler());
        logger.info("NettyServerInitializer add pipeline complete...");
    }
}
