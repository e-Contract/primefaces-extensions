/*
 * Copyright (c) 2011-2022 PrimeFaces Extensions
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.primefaces.extensions.component.ratelimiter;

import java.time.Duration;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

public class RateLimiterController {

    public static final String ATTRIBUTE_NAME = "RateLimiterController";

    private final RateLimiterRegistry rateLimiterRegistry;

    private RateLimiterController() {
        this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    }

    public static RateLimiterController getRateLimiterController(FacesContext facesContext) {
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
        ServletContext servletContext = httpServletRequest.getServletContext();
        RateLimiterController rateLimiterController = getRateLimiterController(servletContext);
        return rateLimiterController;
    }

    public static RateLimiterController getRateLimiterController(ServletContext servletContext) {
        RateLimiterController rateLimiterController = (RateLimiterController) servletContext.getAttribute(ATTRIBUTE_NAME);
        if (null == rateLimiterController) {
            rateLimiterController = new RateLimiterController();
            servletContext.setAttribute(ATTRIBUTE_NAME, rateLimiterController);
        }
        return rateLimiterController;
    }

    public RateLimiter getRateLimiter(int timeoutDuration,
                int limitRefreshPeriod,
                int limitForPeriod, String name) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriod))
                    .limitForPeriod(limitForPeriod)
                    .timeoutDuration(Duration.ofSeconds(timeoutDuration))
                    .build();

        RateLimiter rateLimiter = this.rateLimiterRegistry.rateLimiter(name, config);
        return rateLimiter;
    }

    public void cleanup(String name) {
        this.rateLimiterRegistry.remove(name);
    }
}
