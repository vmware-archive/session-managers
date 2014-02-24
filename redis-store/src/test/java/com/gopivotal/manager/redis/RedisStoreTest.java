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

import com.gopivotal.manager.JmxSupport;
import com.gopivotal.manager.PropertyChangeSupport;
import com.gopivotal.manager.SessionFlushValve;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
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

    private final JmxSupport jmxSupport = mock(JmxSupport.class);

    private final Manager manager = mock(Manager.class);

    private final PropertyChangeListener propertyChangeListener = mock(PropertyChangeListener.class);

    private final PropertyChangeSupport propertyChangeSupport = mock(PropertyChangeSupport.class);

    private final RedisStore store = new RedisStore(this.jedisPool, this.jmxSupport, this.propertyChangeSupport);

    @Test
    public void connectionPoolSize() {
        this.store.setConnectionPoolSize(1);

        assertEquals(1, this.store.getConnectionPoolSize());
        verify(this.propertyChangeSupport).notify("connectionPoolSize", -1, 1);
    }

    @Test
    public void constructor() {
        new RedisStore();
    }

    @Test
    public void database() {
        this.store.setDatabase(7);

        assertEquals(7, this.store.getDatabase());
        verify(this.propertyChangeSupport).notify("database", 0, 7);
    }

    @Test
    public void getInfo() {
        assertEquals("RedisStore/1.0", this.store.getInfo());
    }

    @Test
    public void host() {
        this.store.setHost("test-host");

        assertEquals("test-host", this.store.getHost());
        verify(this.propertyChangeSupport).notify("host", "localhost", "test-host");
    }

    @Test
    public void initInternal() {
        Context context = mock(Context.class);
        Pipeline pipeline = mock(Pipeline.class);
        SessionFlushValve valve = new SessionFlushValve();

        this.store.setManager(this.manager);
        when(this.manager.getContainer()).thenReturn(context);
        when(context.getPipeline()).thenReturn(pipeline);
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
    public void password() {
        this.store.setPassword("test-password");

        assertEquals("test-password", this.store.getPassword());
        verify(this.propertyChangeSupport).notify("password", null, "test-password");
    }

    @Test
    public void port() {
        this.store.setPort(1234);

        assertEquals(1234, this.store.getPort());
        verify(this.propertyChangeSupport).notify("port", 6379, 1234);
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
        Context context = mock(Context.class);
        Host host = mock(Host.class);
        Jedis jedis = mock(Jedis.class);

        this.store.setHost("test.host");
        this.store.setManager(this.manager);
        when(this.manager.getContainer()).thenReturn(context);
        when(context.getName()).thenReturn("test-context-name");
        when(context.getParent()).thenReturn(host);
        when(host.getName()).thenReturn("test-host-name");
        when(this.jedisPool.getResource()).thenReturn(jedis);

        this.store.startInternal();

        verify(this.jedisPool).returnResource(jedis);
        verify(this.jmxSupport).register("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore", this.store);
    }

    @Test
    public void stopInternal() {
        Context context = mock(Context.class);
        Host host = mock(Host.class);

        this.store.setManager(this.manager);
        when(this.manager.getContainer()).thenReturn(context);
        when(context.getName()).thenReturn("test-context-name");
        when(context.getParent()).thenReturn(host);
        when(host.getName()).thenReturn("test-host-name");

        this.store.stopInternal();

        verify(this.jmxSupport).unregister("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore");
    }

    @Test
    public void stopInternalNoPool() {
        Context context = mock(Context.class);
        Host host = mock(Host.class);

        RedisStore alternateStore = new RedisStore(null, this.jmxSupport, this.propertyChangeSupport);
        alternateStore.setManager(this.manager);
        when(this.manager.getContainer()).thenReturn(context);
        when(context.getName()).thenReturn("test-context-name");
        when(context.getParent()).thenReturn(host);
        when(host.getName()).thenReturn("test-host-name");

        alternateStore.stopInternal();

        verify(this.jmxSupport).unregister("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore");
    }

    @Test
    public void timeout() {
        this.store.setTimeout(1234);

        assertEquals(1234, this.store.getTimeout());
        verify(this.propertyChangeSupport).notify("timeout", 2000, 1234);
    }

    @Test
    public void uri() {
        this.store.setUri("redis://test-username:test-password@test-host:1234/7");

        assertEquals("redis://:test-password@test-host:1234/7", this.store.getUri());
        verify(this.propertyChangeSupport).notify("host", "localhost", "test-host");
        verify(this.propertyChangeSupport).notify("port", 6379, 1234);
        verify(this.propertyChangeSupport).notify("password", null, "test-password");
        verify(this.propertyChangeSupport).notify("database", 0, 7);
    }

}
