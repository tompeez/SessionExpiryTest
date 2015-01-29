package de.arbeitsagentur.ruei.session.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import oracle.adf.share.logging.ADFLogger;


public class RUEISessionExpiryFilter implements Filter {
    private static ADFLogger LOGGER = ADFLogger.createADFLogger(RUEISessionExpiryFilter.class);


    public RUEISessionExpiryFilter() {
        super();
    }


    private FilterConfig filterConfig;

    /**
     * init servlet filter
     * 
     * save configuration in private variable
     * @param filterConfig FilterConfig Objekt welches vom Application Server übergeben wird
     * @throws ServletException {@link javax.servlet.Filter#init(FilterConfig)}
     * @see javax.servlet.Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        final String METHOD_NAME = "init";
        LOGGER.info("Entering " + METHOD_NAME);

        this.filterConfig = filterConfig;

        LOGGER.info("Exiting " + METHOD_NAME);
    }

    /**
     * Methode vergleicht die Request Sesion ID und die aktuelle Session ID.
     * Wenn die IDs sich unterscheiden ist die Request Session ID abgelaufen.
     * In diesem Fall wird der Client über einen Redirect an die Request URI
     * umgeleitet. Wenn die Session IDs gleich sind, wird mit der normalen
     * Filter Chain fortgefahren.
     *
     * @param servletRequest {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * @param servletResponse {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * @param filterChain {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * @throws IOException {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * @throws ServletException {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final String METHOD_NAME = "doFilter";
        LOGGER.info("Entering " + METHOD_NAME);

        // check if the session is still valid (e.g. no timeout has occured)
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        boolean bSessionValid = request.isRequestedSessionIdValid();
        LOGGER.info("isRequestedSessionIdValid = " + bSessionValid);
        String requestedSessionId = request.getRequestedSessionId();

        //get current http session and don't create on if not already there!
        String currentSessionId = "";
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            LOGGER.info("+++++ +++++ No current HttpSession!");
            //force redirect
            bSessionValid = false;
        } else {
            currentSessionId = httpSession.getId();
            LOGGER.info("RequestredSesionID = " + requestedSessionId);
            LOGGER.info("currentSessionId   = " + currentSessionId);
        }

        // if the requestSession is not valid the current session is a new sesion. In this case we redirect to
        // a special page, otherwise the normal filter chain continous
        if (!bSessionValid) {
            //            String redirectUri = request.getRequestURI();
            String redirectUri = "http://127.0.0.1:7101/ruei/timeout.html";
            LOGGER.info("Redirect to: " + redirectUri);
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
            httpResponse.setDateHeader("Expires", 0); // Proxies.
            httpResponse.sendRedirect(redirectUri);
        } else {
            LOGGER.info("Continue normal Filter Chain.");

            filterChain.doFilter(servletRequest, servletResponse);
        }

        LOGGER.info("Exiting " + METHOD_NAME);
    }

    /**
     * Methode setzt die private Filter Config auf null.
     *
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        final String METHOD_NAME = "destroy";
        LOGGER.info("Entering " + METHOD_NAME);

        this.filterConfig = null;

        LOGGER.info("Exiting " + METHOD_NAME);
    }
}
