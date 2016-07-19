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

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class SessionSerializationUtilsTest {

    private final Context context = new StandardContext();

    private final Manager manager = new StandardManager();

    private final SessionSerializationUtils sessionSerializationUtils = new SessionSerializationUtils(this.manager);

    @Before
    public void manager() throws Exception {
        this.manager.setContext(this.context);
    }

    @Test
    public void test() throws IOException, ClassNotFoundException {
        Session initial = this.manager.createEmptySession();
        initial.setValid(true);
        initial.getSession().setAttribute("test-key", "test-value");

        Session result = this.sessionSerializationUtils.deserialize(this.sessionSerializationUtils.serialize(initial));

        assertEquals("test-value", result.getSession().getAttribute("test-key"));
    }

    @Test
    public void testNullData() throws IOException, ClassNotFoundException {
        assertNull(this.sessionSerializationUtils.deserialize(null));
    }
}
