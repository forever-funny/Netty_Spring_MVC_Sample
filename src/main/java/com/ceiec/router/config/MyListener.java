package com.ceiec.router.config;

import com.ceiec.router.config.servletconfig.MyServletContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/5/24 0024
 * creat_time: 20:07
 **/
@Data
@Slf4j
public class MyListener implements SpringApplicationRunListener {


    public MyListener(SpringApplication application, String[] args) {
        super();
    }


    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.info("contextPrepared");
        ServletContext servletContext = new MyServletContext();
        ServletWebServerApplicationContext applicationContext = (ServletWebServerApplicationContext) context;
        applicationContext.setServletContext(servletContext);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        //Not used.
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        System.out.println("started");
    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }


}
