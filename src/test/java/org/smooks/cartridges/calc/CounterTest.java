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

import static org.testng.AssertJUnit.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.Configurator;
import org.smooks.container.MockExecutionContext;
import org.smooks.javabean.context.BeanContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Unit test for the Counter class
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class CounterTest {

    private final String selector = "x";

    private final String beanId = "bean";


    private SmooksResourceConfiguration config;

    private MockExecutionContext executionContext;
    private BeanContext beanContext;


    @Test(groups = "unit")
    public void test_default_count() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(1, value.longValue());

    }

    @Test(groups = "unit")
    public void test__static_amount() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("amount", "10");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(10, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(20, value.longValue());

    }

    @Test(groups = "unit")
    public void test_static_start() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("start", "100");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(100, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(101, value.longValue());

    }

    @Test(groups = "unit")
    public void test_direction() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("direction", "DECREMENT");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(-1, value.longValue());

    }

    @Test(groups = "unit")
    public void test_amountExpression() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("amountExpression", "5*5");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(25, value.longValue());

    }


    @Test(groups = "unit")
    public void test_startExpression() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("startExpression", "5*5");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(25, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(26, value.longValue());

    }


    @Test(groups = "unit")
    public void test_resetCondition() throws ParserConfigurationException, SAXException, IOException {

        config.setParameter("beanId", beanId);
        config.setParameter("resetCondition", "bean == 1");

        Counter counter = new Counter();
        Configurator.configure(counter, config, executionContext.getContext());

        counter.visitBefore((Element) null, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(1, value.longValue());

        counter.visitBefore((Element) null, executionContext);

        value = getCounterValue();

        assertEquals(0, value.longValue());

    }


    private long getCounterValue() {
        return getCounterValue(beanId);
    }

    private long getCounterValue(String beanId) {
        Object valueObj = beanContext.getBean(beanId);

        assertNotNull(valueObj);
        assertTrue(valueObj instanceof Long);

        return (Long) valueObj;
    }

    @BeforeMethod(groups = "unit")
    public void init() {

        config = new SmooksResourceConfiguration(selector, Counter.class.getName());
        executionContext = new MockExecutionContext();
        beanContext = executionContext.getBeanContext();
    }

}
