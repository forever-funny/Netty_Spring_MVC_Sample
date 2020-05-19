# Netty_Spring_MVC_Sample

#### 介绍
简单说，一个spring boot应用，默认使用了tomcat作为底层容器来接收和处理连接。
我这里，在依赖中排除了tomcat，使用netty作为了替代品。
处理流程大概是：
 * 1：首先经过http编解码器，变成netty的http请求类型,{@link FullHttpRequest}
 * 2：经过第一个handler， {@link GenerateServletRequestHandler},转变为servlet的httpRequest，这里使用了{@link MockHttpServletRequest}
 * 3：经过第二个handler，{@link FilterNettyHandler},在这个handler里，会使用filter链对请求进行处理，方便扩展
 * 4：进入业务handler，{@link DispatcherServletHandler}，这个handler里，将请求交给spring mvc的 {@link org.springframework.web.servlet.DispatcherServlet }

![输入图片说明](https://images.gitee.com/uploads/images/2019/1210/131338_38abefee_1720963.png "屏幕截图.png")

对应的博客文章：
[曹工杂谈：Spring boot应用，自己动手用Netty替换底层Tomcat容器](https://www.cnblogs.com/grey-wolf/p/12017818.html)
