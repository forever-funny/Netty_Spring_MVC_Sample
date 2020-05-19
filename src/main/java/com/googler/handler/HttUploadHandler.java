package com.googler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class HttUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
    
    private static final Logger logger = LoggerFactory.getLogger(HttUploadHandler.class);

    public HttUploadHandler() {
        super(false);
    }

    private static final HttpDataFactory FACTORY = new DefaultHttpDataFactory(true);
    private static final String URI = "/upload";
    private HttpPostRequestDecoder httpDecoder;
    HttpRequest request;
    /** 保存上传文件的元信息 */
    private Map<String, String> metadataResource = new HashMap<>();
    private String finalFileName;
    private String resourceName;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject)
            throws Exception {
        if (httpObject instanceof HttpRequest) {
            request = (HttpRequest) httpObject;
            if (request.uri().startsWith(URI) && request.method().equals(HttpMethod.POST)) {
                httpDecoder = new HttpPostRequestDecoder(FACTORY, request);
                httpDecoder.setDiscardThreshold(0);
            } else {
                logger.warn("httpObject != HttpRequest, skip upload handler, do next handler...");
                ctx.fireChannelRead(httpObject);
            }
        }
        if (httpObject instanceof HttpContent) {
            if (httpDecoder != null) {
                final HttpContent chunk = (HttpContent) httpObject;
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    // String hdfsPath = writeChunk(ctx);
                    // 关闭httpDecoder
                    httpDecoder.destroy();
                    httpDecoder = null;
                    // 这里结束了,需要调接口记录元数据信息
                    logger.info("show request header:{}", request.headers());
                }
                ReferenceCountUtil.release(httpObject);
            } else {
                logger.warn("skip upload handler, do next handler...");
                ctx.fireChannelRead(httpObject);
            }
        }
    }

    private String writeChunk(ChannelHandlerContext ctx) throws IOException {
        String hdfsPath = null;
        while (httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null && HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                final FileUpload fileUpload = (FileUpload) data;
                String newFileName = request.headers().get("newfilename");
                String userName = request.headers().get("username");
                
                if (newFileName != null) {
                    // finalFileName = FILE_UPLOAD.concat(newFileName);
                } else {
                    // finalFileName = FILE_UPLOAD.concat(fileUpload.getFilename());
                }
                final File file = new File(finalFileName);
                // 设置资源名称
                resourceName = file.getName();
                logger.info("upload file name: {}", file);
                String hdfsFilePath = "Constant.HDFS_BASE_PATH.concat(userName)";
                try (FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                     FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                    ResponseUtil.response(ctx, request, new GeneralResponse(HttpResponseStatus.OK,
                            "File upload successfully!, file tmp path:" + file.getAbsolutePath() + 
                                    " HDFS path:" + hdfsFilePath, null));
                }
                logger.info("start move local file:{} to HDFS:{}", finalFileName, hdfsFilePath);
                // HdfsUtil.uploadFile(finalFileName, hdfsFilePath);
                logger.info("move local file:{} to HDFS:{} successfully!", finalFileName, hdfsFilePath);
                hdfsPath = hdfsFilePath.concat("/").concat(resourceName);
            }
        }
        return hdfsPath;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("catch exception!!!", cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }
}