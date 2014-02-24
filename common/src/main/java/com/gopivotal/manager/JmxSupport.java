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


/**
 * An API that encapsulates the functionality required to register and unregister MBeans from the JMX {@link
 * javax.management.MBeanServer}
 */
public interface JmxSupport {

    /**
     * Register an MBean with an {@link javax.management.MBeanServer}
     *
     * @param objectName the {@link javax.management.ObjectName} to register the instance with.  Note that this is
     *                   actually a {@link String} so that users do not have to deal with exception handling while
     *                   creating the {@link javax.management.ObjectName}.
     * @param instance   the instance to register.
     */
    void register(String objectName, Object instance);

    /**
     * Unregister an MBean from an {@link javax.management.MBeanServer}
     *
     * @param objectName the {@link javax.management.ObjectName} used to register the instance
     */
    void unregister(String objectName);

}
