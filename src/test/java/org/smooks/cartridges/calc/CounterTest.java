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

import org.smooks.cdr.ResourceConfig;
import org.smooks.container.MockExecutionContext;
import org.smooks.injector.Scope;
import org.smooks.javabean.context.BeanContext;
import org.smooks.lifecycle.LifecycleManager;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.LifecycleManagerLookup;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.testng.AssertJUnit.*;

/**
 * Unit test for the Counter class
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class CounterTest {

    private final String selector = "x";

    private final String beanId = "bean";

    private ResourceConfig resourceConfig;

    private MockExecutionContext executionContext;
    private BeanContext beanContext;
    private Registry registry;
    private LifecycleManager lifecycleManager;

    private Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("foo");

    public CounterTest() throws ParserConfigurationException {
    }

    @Test(groups = "unit")
    public void test_default_count() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));

        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(1, value.longValue());

    }

    @Test(groups = "unit")
    public void test__static_amount() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("amount", "10");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(10, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(20, value.longValue());

    }

    @Test(groups = "unit")
    public void test_static_start() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("start", "100");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(100, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(101, value.longValue());

    }

    @Test(groups = "unit")
    public void test_direction() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("direction", "DECREMENT");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(-1, value.longValue());

    }

    @Test(groups = "unit")
    public void test_amountExpression() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("amountExpression", "5*5");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(25, value.longValue());

    }


    @Test(groups = "unit")
    public void test_startExpression() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("startExpression", "5*5");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(25, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(26, value.longValue());

    }


    @Test(groups = "unit")
    public void test_resetCondition() throws ParserConfigurationException, SAXException, IOException {

        resourceConfig.setParameter("beanId", beanId);
        resourceConfig.setParameter("resetCondition", "bean == 1");

        Counter counter = new Counter();
        lifecycleManager.applyPhase(counter, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, counter)));
        
        counter.visitBefore(element, executionContext);

        Long value = getCounterValue();

        assertEquals(0, value.longValue());

        counter.visitBefore(element, executionContext);

        value = getCounterValue();

        assertEquals(1, value.longValue());

        counter.visitBefore(element, executionContext);

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

        resourceConfig = new ResourceConfig(selector, Counter.class.getName());
        executionContext = new MockExecutionContext();
        registry = executionContext.getApplicationContext().getRegistry();
        lifecycleManager = registry.lookup(new LifecycleManagerLookup());
        beanContext = executionContext.getBeanContext();
    }

}
