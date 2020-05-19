package com.ceiec.router.netty.filter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 * 过滤器注册表
 * @author : caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 11:48
 **/
@Data
public class ApplicationFilterConfigRegistry {

    private List<ApplicationFilterConfig> applicationFilterConfigs = new ArrayList<>();

    public void register(ApplicationFilterConfig applicationFilterConfig){
        applicationFilterConfigs.add(applicationFilterConfig);
    }


}
