package com.ceiec.router.config;

import com.ceiec.router.filter.DynamicChangePathFilter;
import com.ceiec.router.netty.filter.ApplicationFilterConfig;
import com.ceiec.router.netty.filter.ApplicationFilterConfigRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * desc: 配置过滤器
 * @author: caokunliang
 * creat_date: 2019/12/10 0010
 * creat_time: 15:11
 **/
@Configuration
public class AppConfig {

    @Bean
    public ApplicationFilterConfigRegistry getApplicationFilterConfigRegistry() {
        ApplicationFilterConfigRegistry registry = new ApplicationFilterConfigRegistry();

        ApplicationFilterConfig dynamicChangePathFilter = new ApplicationFilterConfig();
        dynamicChangePathFilter.setFilter(new DynamicChangePathFilter());
        dynamicChangePathFilter.setFilterName("DynamicChangePathFilter");
        dynamicChangePathFilter.setUrlPatterns("/*");

        registry.register(dynamicChangePathFilter);
        return registry;
    }

}
