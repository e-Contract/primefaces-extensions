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

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

public class RateLimiterTagHandler extends TagHandler {

    private final TagAttribute timeoutDurationAttribute;
    private final TagAttribute limitRefreshPeriodAttribute;
    private final TagAttribute limitForPeriodAttribute;
    private final TagAttribute nameAttribute;

    public RateLimiterTagHandler(TagConfig config) {
        super(config);
        this.timeoutDurationAttribute = getAttribute("timeoutDuration");
        this.limitRefreshPeriodAttribute = getAttribute("limitRefreshPeriod");
        this.limitForPeriodAttribute = getAttribute("limitForPeriod");
        this.nameAttribute = getAttribute("name");
    }

    @Override
    public void apply(FaceletContext faceletContext, UIComponent parent) throws IOException {
        if (parent == null) {
            return;
        }
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        if (!(parent instanceof ActionSource)) {
            throw new TagException(this.tag, "RateLimiter can only be attached to ActionSource components.");
        }

        ValueExpression timeoutDuration;
        if (null != this.timeoutDurationAttribute) {
            timeoutDuration = this.timeoutDurationAttribute.getValueExpression(faceletContext, Integer.class);
        }
        else {
            timeoutDuration = null;
        }

        ValueExpression limitRefreshPeriod;
        if (null != this.limitRefreshPeriodAttribute) {
            limitRefreshPeriod = this.limitRefreshPeriodAttribute.getValueExpression(faceletContext, Integer.class);
        }
        else {
            limitRefreshPeriod = null;
        }

        ValueExpression limitForPeriod;
        if (null != this.limitForPeriodAttribute) {
            limitForPeriod = this.limitForPeriodAttribute.getValueExpression(faceletContext, Integer.class);
        }
        else {
            limitForPeriod = null;
        }

        ValueExpression name;
        if (null != this.nameAttribute) {
            name = this.nameAttribute.getValueExpression(faceletContext, String.class);
        }
        else {
            name = null;
        }

        RateLimiterActionListener actionListener = new RateLimiterActionListener(timeoutDuration, limitRefreshPeriod, limitForPeriod, name);
        ActionSource actionSource = (ActionSource) parent;
        actionSource.addActionListener(actionListener);
    }
}
