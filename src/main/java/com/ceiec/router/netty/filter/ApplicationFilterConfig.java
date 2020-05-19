package com.ceiec.router.netty.filter;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;


/**
 * Implementation of a <code>javax.servlet.FilterConfig</code> useful in
 * managing the filter instances instantiated when a web application
 * is first started.
 *
 * @author Craig R. McClanahan
 */
@Slf4j
@Data
public final class ApplicationFilterConfig implements FilterConfig {
    /**
     * filter对象
     */
    private Filter filter;

    /**
     * 过滤器名称
     */
    private String filterName;

    /**
     * 过滤器的url模式
     * 遵循传统web.xml中的语法
     * https://www.cnblogs.com/51kata/p/5152400.html
     */
    private String urlPatterns;

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
