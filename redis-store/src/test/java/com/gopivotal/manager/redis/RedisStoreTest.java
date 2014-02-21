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

package com.gopivotal.manager.redis;

import com.gopivotal.manager.PropertyChangeSupport;
import com.gopivotal.manager.SessionFlushValve;
import org.apache.catalina.Container;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.beans.PropertyChangeListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RedisStoreTest {

    private final JedisPool jedisPool = mock(JedisPool.class);

    private final Manager manager = mock(Manager.class);

    private final PropertyChangeListener propertyChangeListener = mock(PropertyChangeListener.class);

    private final PropertyChangeSupport propertyChangeSupport = mock(PropertyChangeSupport.class);

    private final RedisStore store = new RedisStore(this.jedisPool, this.propertyChangeSupport);

    @Test
    public void constructor() {
        new RedisStore();
    }

    @Test
    public void getInfo() {
        assertEquals("RedisStore/1.0", this.store.getInfo());
    }

    @Test
    public void host() {
        this.store.setHost("test-host");

        verify(this.propertyChangeSupport).notify("host", "localhost", "test-host");
    }

    @Test
    public void initInternal() {
        Container container = mock(Container.class);
        Pipeline pipeline = mock(Pipeline.class);
        SessionFlushValve valve = new SessionFlushValve();

        this.store.setManager(this.manager);
        when(this.manager.getContainer()).thenReturn(container);
        when(container.getPipeline()).thenReturn(pipeline);
        when(pipeline.getValves()).thenReturn(new Valve[]{mock(Valve.class), valve});

        this.store.initInternal();

        assertSame(this.store, valve.getStore());
    }

    @Test
    public void manager() {
        assertNull(this.store.getManager());

        this.store.setManager(this.manager);

        assertSame(this.manager, this.store.getManager());
        verify(this.propertyChangeSupport).notify("manager", null, this.manager);
    }

    @Test
    public void propertyChangeListeners() {
        this.store.addPropertyChangeListener(this.propertyChangeListener);
        verify(this.propertyChangeSupport).add(this.propertyChangeListener);

        this.store.removePropertyChangeListener(this.propertyChangeListener);
        verify(this.propertyChangeSupport).remove(this.propertyChangeListener);
    }

    @Test
    public void startInternal() {
        Jedis jedis = mock(Jedis.class);

        when(this.jedisPool.getResource()).thenReturn(jedis);

        this.store.setHost("test.host");
        this.store.startInternal();

        verify(this.jedisPool).returnResource(jedis);
    }

    @Test
    public void stopInternal() {
        this.store.stopInternal();
    }

    @Test
    public void stopInternalNoPool() {
        this.store.stopInternal();
    }
}
