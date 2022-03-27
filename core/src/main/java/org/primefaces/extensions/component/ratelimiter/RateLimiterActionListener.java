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

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.primefaces.util.LangUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.ratelimiter.RateLimiter;

public class RateLimiterActionListener implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterActionListener.class);

    private boolean _transient;

    private ValueExpression timeoutDurationValueExpression;

    private ValueExpression limitRefreshPeriodValueExpression;

    private ValueExpression limitForPeriodValueExpression;

    private ValueExpression nameValueExpression;

    public RateLimiterActionListener() {
        super();
    }

    public RateLimiterActionListener(ValueExpression timeoutDurationValueExpression,
                ValueExpression limitRefreshPeriodValueExpression,
                ValueExpression limitForPeriodValueExpression,
                ValueExpression nameValueExpression) {
        this.timeoutDurationValueExpression = timeoutDurationValueExpression;
        this.limitRefreshPeriodValueExpression = limitRefreshPeriodValueExpression;
        this.limitForPeriodValueExpression = limitForPeriodValueExpression;
        this.nameValueExpression = nameValueExpression;
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        final Object[] values = new Object[4];
        values[0] = this.timeoutDurationValueExpression;
        values[1] = this.limitRefreshPeriodValueExpression;
        values[2] = this.limitForPeriodValueExpression;
        values[3] = this.nameValueExpression;
        return values;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        Object[] stateObjects = (Object[]) state;
        this.timeoutDurationValueExpression = (ValueExpression) stateObjects[0];
        this.limitRefreshPeriodValueExpression = (ValueExpression) stateObjects[1];
        this.limitForPeriodValueExpression = (ValueExpression) stateObjects[2];
        this.nameValueExpression = (ValueExpression) stateObjects[3];
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        int timeoutDuration;
        if (null != this.timeoutDurationValueExpression) {
            timeoutDuration = (int) this.timeoutDurationValueExpression.getValue(elContext);
        }
        else {
            timeoutDuration = 5;
        }

        int limitRefreshPeriod;
        if (null != this.limitRefreshPeriodValueExpression) {
            limitRefreshPeriod = (int) this.limitRefreshPeriodValueExpression.getValue(elContext);
        }
        else {
            limitRefreshPeriod = 5;
        }

        int limitForPeriod;
        if (null != this.limitForPeriodValueExpression) {
            limitForPeriod = (int) this.limitForPeriodValueExpression.getValue(elContext);
        }
        else {
            limitForPeriod = 5;
        }

        String name;
        if (null != this.nameValueExpression) {
            name = (String) this.nameValueExpression.getValue(elContext);
            if (LangUtils.isBlank(name)) {
                name = "default";
            }
        }
        else {
            ExternalContext externalContext = facesContext.getExternalContext();
            name = externalContext.getSessionId(false);
            if (LangUtils.isBlank(name)) {
                name = "default";
            }
        }

        RateLimiterController rateLimiterController = RateLimiterController.getRateLimiterController(facesContext);
        RateLimiter rateLimiter = rateLimiterController.getRateLimiter(timeoutDuration, limitRefreshPeriod, limitForPeriod, name);
        LOGGER.debug("rate limiter for {}", name);
        boolean result = rateLimiter.acquirePermission();
        if (!result) {
            LOGGER.warn("rate limiter activated for: {}", name);
        }
    }
}
