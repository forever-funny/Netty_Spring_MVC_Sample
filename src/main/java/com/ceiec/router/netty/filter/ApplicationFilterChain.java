package com.ceiec.router.netty.filter;


import io.netty.channel.ChannelHandlerContext;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of <code>javax.servlet.FilterChain</code> used to manage
 * the execution of a set of filters for a particular request.  When the
 * set of defined filters has all been executed, the next call to
 * <code>doFilter()</code> will execute the servlet's <code>service()</code>
 * method itself.
 *
 * @author Craig R. McClanahan
 */
public final class ApplicationFilterChain implements FilterChain {

    /**
     * Filters.
     */
    private List<ApplicationFilterConfig> filters = new ArrayList<>();


    /**
     * The int which is used to maintain the current position
     * in the filter chain.
     */
    private int pos = 0;




    private ChannelHandlerContext ctx;



    // ---------------------------------------------------- FilterChain Methods


    /**
     * Invoke the next filter in this chain, passing the specified request
     * and response.  If there are no more filters in this chain, invoke
     * the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {

        internalDoFilter(request,response);
    }

    private void internalDoFilter(ServletRequest request,
                                  ServletResponse response)
        throws IOException, ServletException {

        // Call the next filter if there is one
        // if (pos < filters.size()) {
        //     ApplicationFilterConfig filterConfig = filters.get(pos);
        //     pos++;
        //     Filter filter = null;
        //     try {
        //         // filter = filterConfig.getFilter();
        //
        //
        //         // filter.doFilter(request, response, this);
        //
        //     } catch (IOException | ServletException | RuntimeException e) {
        //
        //         throw e;
        //     } catch (Throwable e) {
        //         throw new ServletException();
        //     }
        //     return;
        // }


        // We fell off the end of the chain -- call the servlet instance
        try {
            RequestResponseWrapper wrapper = new RequestResponseWrapper();
            wrapper.setServletRequest(request);
            wrapper.setServletResponse(response);

            ctx.fireChannelRead(wrapper);
        } catch (Throwable e) {
            throw new ServletException();
        } finally {

        }
    }






    // -------------------------------------------------------- Package Methods


    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {

        // Prevent the same filter being added multiple times
        for(ApplicationFilterConfig filter:filters) {
            if (filter == filterConfig) {
                return;
            }
        }

        filters.add(filterConfig);
    }


    void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}
