/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gopivotal.manager;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * The standard implementation of the {@link JmxSupport} interface
 */
public final class StandardJmxSupport implements JmxSupport {

    private final MBeanServer mBeanServer;

    /**
     * Creates a new instance
     */
    public StandardJmxSupport() {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    StandardJmxSupport(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public void register(String objectName, Object instance) {
        try {
            this.mBeanServer.registerMBean(instance, new ObjectName(objectName));
        } catch (JMException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void unregister(String objectName) {
        try {
            this.mBeanServer.unregisterMBean(new ObjectName(objectName));
        } catch (JMException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
