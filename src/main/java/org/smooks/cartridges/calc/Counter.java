/*-
 * ========================LICENSE_START=================================
 * smooks-calc-cartridge
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.calc;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.smooks.SmooksException;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.annotation.VisitAfterIf;
import org.smooks.delivery.annotation.VisitBeforeIf;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.repository.BeanId;
import org.smooks.util.CollectionsUtil;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The counter can increment or decrement a value.
 * <p/>
 * This counter has extended xml schema configuration. Take a look at the
 * schema {@link https://www.smooks.org/xsd/smooks/calc-1.2.xsd} for more
 * information.
 * <p/>
 * Example basic configuration:
 * <pre>
 * &lt;resource-config selector="orderItems"&gt;
 *    &lt;resource&gt;org.smooks.calc.Counter&lt;/resource&gt;
 *    &lt;param name="beanId">count&lt;/param&gt;
 * &lt;/resource-config&gt;
 * <p/>
 * Optional parameters:
 *    &lt;param name="start"&gt;1&lt;/param&gt;
 *    &lt;param name="amount"&gt;2&lt;/param&gt;
 *    &lt;param name="amountExpression"&gt;incrementAmount&lt;/param&gt;
 *    &lt;param name="startExpression"&gt;startValue&lt;/param&gt;
 *    &lt;param name="resetCondition"&gt;count == 10&lt;/param&gt;
 *    &lt;param name="direction"&gt;DECREMENT&lt;/param&gt;
 *    &lt;param name="executeAfter&gt;false&lt;/param&gt;
 * </pre>
 * Description of configuration properties:
 *
 * <ul>
 * <li><i>beanId</i>: The beanId in which the counter value is stored. The value is always stored as a Long type.</li>
 * <li><i>start</i>: The counter start value.</li>
 * <li><i>startExpression</i>: The result of this expression is the counter start value.
 * 							   This expression is executed at the first count and every time the counter
 * 							   is reset. The expression must result in an integer or a long.
 * 							   If the startIndex attribute of the counter is set then this expression never gets
 * 							   executed.</li>
 * <li><i>amount</i>: The amount that the counter increments or decrements the counter value.</li>
 * <li><i>amountExpression</i>: The result of this expression is the amount the counter increments or decrements.
 * 								This expression is executed every time the counter counts.
 * 								The expression must result in an integer.
 * 								If the amount attribute of the counter is set then this expression never gets
 * 								executed.</li>
 * <li><i>resetCondition</i>: When the expression is set and results in a true value then the counter is reset to
 * 							  the start index. The expression must result in a boolean.</li>
 * <li><i>direction</i>: The direction that the counter counts. Can be INCREMENT (default) or DECREMENT.</li>
 * <li><i>executeAfter</i>: If the counter is executed after the element else it will execute before the element.
 * 			    			Default is 'false'.</li>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @since 1.1
 */
@VisitBeforeIf(condition = "!parameters.containsKey('executeAfter') || parameters.executeAfter.value != 'true'")
@VisitAfterIf(condition = "parameters.containsKey('executeAfter') && parameters.executeAfter.value == 'true'")
public class Counter implements SAXVisitBefore, SAXVisitAfter, DOMVisitBefore, DOMVisitAfter, Producer {

    public static final Long DEFAULT_START_INDEX = new Long(0);

    public static final int DEFAULT_AMOUNT = 1;

    @Inject
    @Named("beanId")
    private String beanIdName;

    @Inject
    private Optional<Long> start;

    @Inject
    private Optional<Integer> amount;

    @Inject
    private Optional<ExpressionEvaluator> amountExpression;

    @Inject
    private Optional<ExpressionEvaluator> startExpression;

    @Inject
    private Optional<ExpressionEvaluator> resetCondition;

    @Inject
    private CountDirection direction = CountDirection.INCREMENT;

    private BeanId beanId;

    @Inject
    private ApplicationContext appContext;

    @PostConstruct
    public void initialize() {
        beanId = appContext.getBeanIdStore().register(beanIdName);
    }

    public void visitBefore(SAXElement element,
                            ExecutionContext executionContext) throws SmooksException,
            IOException {
        count(executionContext, new Fragment(element));
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext)
            throws SmooksException, IOException {
        count(executionContext, new Fragment(element));
    }

    public void visitBefore(Element element, ExecutionContext executionContext)
            throws SmooksException {

        count(executionContext, new Fragment(element));
    }

    public void visitAfter(Element element, ExecutionContext executionContext)
            throws SmooksException {
        count(executionContext, new Fragment(element));
    }

    public void count(ExecutionContext executionContext, Fragment source) {
        BeanContext beanContext = executionContext.getBeanContext();
        Long value = (Long) beanContext.getBean(beanId);
        if (value == null || (resetCondition.isPresent() && resetCondition.get().eval(beanContext.getBeanMap()))) {
            value = getStart(beanContext);
        } else {
            int amount = getAmount(beanContext);

            if (direction == CountDirection.INCREMENT) {
                value = value + amount;
            } else {
                value = value - amount;
            }
        }
        beanContext.addBean(beanId, value, source);
    }


    private Long getStart(BeanContext beanContext) {
        if (!start.isPresent() && !startExpression.isPresent()) {
            return DEFAULT_START_INDEX;
        } else if (start.isPresent()) {
            return start.get();
        } else {
            Object result = startExpression.get().getValue(beanContext.getBeanMap());
            if (!(result instanceof Long || result instanceof Integer)) {
                throw new SmooksException("The start expression must result in a Integer or a Long");
            }
            return new Long(result.toString());
        }
    }

    private int getAmount(BeanContext beanContext) {
        if (!amount.isPresent() && !amountExpression.isPresent()) {
            return DEFAULT_AMOUNT;
        } else if (amount.isPresent()) {
            return amount.get();
        } else {
            Object result = amountExpression.get().getValue(beanContext.getBeanMap());
            if (!(result instanceof Integer)) {
                throw new SmooksException("The amount expression must result in a Integer");
            }
            return (Integer) result;
        }
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName);
    }
}
