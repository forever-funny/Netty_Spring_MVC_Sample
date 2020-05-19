package com.ceiec.router;

import com.ceiec.router.netty.DispatcherServletChannelInitializer;
import com.ceiec.router.netty.MyServer;
import com.ceiec.router.netty.handler.DispatcherServletHandler;
import com.ceiec.router.netty.handler.FilterNettyHandler;
import com.ceiec.router.netty.handler.GenerateServletRequestHandler;
import com.ceiec.router.utils.MyReflectionUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;

/**
 * desc:
 * 使用netty + spring mvc + spring boot 实现web服务器
 *
 * 请求处理流程：
 * {@link DispatcherServletChannelInitializer#initChannel(io.netty.channel.socket.SocketChannel)}
 * 1：首先经过http编解码器，变成netty的http请求类型,{@link FullHttpRequest}
 * 2：经过第一个handler， {@link GenerateServletRequestHandler},转变为servlet的httpRequest，这里使用了{@link MockHttpServletRequest}
 * 3：经过第二个handler，{@link FilterNettyHandler},在这个handler里，会使用filter链对请求进行处理，方便扩展
 * 4：进入业务handler，{@link DispatcherServletHandler}，这个handler里，将请求交给spring mvc的 {@link org.springframework.web.servlet.DispatcherServlet }
 *
 * 响应处理过程：
 * 上面几个handler都是inbound类型的handler，不会对响应进行处理，直接交给netty处理
 *
 * @author: caokunliang
 * creat_date: 2019/1/10 0010
 * creat_time: 10:18
 **/
@SpringBootApplication()
@ComponentScan(basePackages = "com.ceiec")
@Slf4j
public class Bootstrap {



    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Bootstrap.class, args);

        /**
         * 从spring上下文获取DispatcherServlet，对其进行处理
         */
        DispatcherServlet dispatcherServlet = applicationContext.getBean(DispatcherServlet.class);
        MockServletConfig myServletConfig = new MockServletConfig();
        MyReflectionUtils.setFieldValue(dispatcherServlet,"config",myServletConfig);

        /**
         * 初始化servlet
         */
        try {
            dispatcherServlet.init();
        } catch (ServletException e) {
            log.error("e:{}",e);
        }

        /**
         * 启动 netty 服务器
         */
        Environment environment = applicationContext.getBean(Environment.class);
        String port = environment.getProperty("netty.server.port");

        new MyServer(Integer.valueOf(port)).run();
    }


}
