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

import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public final class SessionFlushValveTest {

    private final Valve next = mock(Valve.class);

    private final Request request = mock(Request.class);

    private final Response response = mock(Response.class);

    private final Session session = mock(Session.class);

    private final Store store = mock(Store.class);

    private final SessionFlushValve valve = new SessionFlushValve();

    @Test
    public void backgroundProcess() {
        this.valve.backgroundProcess();
    }

    @Test
    public void event() {
        this.valve.event(null, null, null);
    }

    @Test
    public void getInfo() {
        assertEquals("SessionFlushValve/1.0", this.valve.getInfo());
    }

    @Before
    public void inject() throws Exception {
        this.valve.setNext(this.next);
        this.valve.setStore(this.store);
    }

    @Test
    public void invokeInvalidSession() throws IOException, ServletException {
        when(this.request.getSessionInternal(false)).thenReturn(this.session);
        when(this.session.isValid()).thenReturn(false);

        this.valve.invoke(this.request, this.response);

        verify(this.next).invoke(this.request, this.response);
        verifyZeroInteractions(this.store);
    }

    @Test
    public void invokeNoSession() throws IOException, ServletException {
        when(this.request.getSessionInternal(false)).thenReturn(null);

        this.valve.invoke(this.request, this.response);

        verify(this.next).invoke(this.request, this.response);
        verifyZeroInteractions(this.store);
    }

    @Test
    public void invokeSession() throws IOException, ServletException {
        when(this.request.getSessionInternal(false)).thenReturn(this.session);
        when(this.session.isValid()).thenReturn(true);

        this.valve.invoke(this.request, this.response);

        verify(this.next).invoke(this.request, this.response);
        verify(this.store).save(this.session);
    }

    @Test
    public void isAsyncSupported() {
        assertFalse(this.valve.isAsyncSupported());
    }

    @Test
    public void next() {
        assertSame(this.next, this.valve.getNext());
    }
}
