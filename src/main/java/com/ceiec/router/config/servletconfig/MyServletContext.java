package com.ceiec.router.config.servletconfig;

import org.springframework.mock.web.MockServletContext;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/2/12 0012
 * creat_time: 17:21
 **/
public class MyServletContext extends MockServletContext{

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter){
        return null;
    }
}
