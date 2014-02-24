/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gopivotal.manager;

import org.junit.Test;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StandardJmxSupportTest {

    private final Object instance = new Object();

    private final MBeanServer mBeanServer = mock(MBeanServer.class);

    private final StandardJmxSupport jmxSupport = new StandardJmxSupport(this.mBeanServer);

    @Test
    public void constructor() {
        new StandardJmxSupport();
    }

    @Test
    public void register() throws JMException {
        this.jmxSupport.register("domain:key=value", this.instance);

        verify(this.mBeanServer).registerMBean(this.instance, new ObjectName("domain:key=value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerJMException() throws JMException {
        when(this.mBeanServer.registerMBean(eq(this.instance), any(ObjectName.class))).thenThrow(new
                InstanceAlreadyExistsException());

        this.jmxSupport.register("domain:key=value", this.instance);
    }

    @Test
    public void unregister() throws JMException {
        this.jmxSupport.unregister("domain:key=value");

        verify(this.mBeanServer).unregisterMBean(new ObjectName("domain:key=value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unregisterJMException() throws JMException {
        doThrow(new InstanceNotFoundException()).when(this.mBeanServer).unregisterMBean(any(ObjectName.class));

        this.jmxSupport.unregister("domain:key=value");
    }
}
