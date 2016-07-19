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
import com.gopivotal.manager.SessionSerializationUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.valves.RemoteIpValve;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RedisStoreTest {

    private final Jedis jedis = mock(Jedis.class);

    private final JedisPool jedisPool = mock(JedisPool.class);

    private final JmxSupport jmxSupport = mock(JmxSupport.class);

    private final Manager manager = new StandardManager();

    private final SessionSerializationUtils sessionSerializationUtils = new SessionSerializationUtils(this.manager);

    private final PropertyChangeListener propertyChangeListener = mock(PropertyChangeListener.class);

    private final PropertyChangeSupport propertyChangeSupport = mock(PropertyChangeSupport.class);

    private final RedisStore store = new RedisStore(this.jedisPool, this.jmxSupport, this.propertyChangeSupport,
            this.sessionSerializationUtils);

    private final Transaction transaction = mock(StubTransaction.class);

    @Test
    public void clear() throws IOException {
        Set<String> sessionIds = new HashSet<String>();
        sessionIds.add("test-id");
        when(this.jedis.smembers("sessions")).thenReturn(sessionIds);

        this.store.clear();

        verify(this.transaction).srem("sessions", "test-id");
        verify(this.transaction).del(new String[]{"test-id"});
        verify(this.transaction).exec();
    }

    @Test
    public void clearJedisConnectionException() {
        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        this.store.clear();
    }

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
    public void getSize() throws IOException {
        Response<Long> response = new Response<Long>(BuilderFactory.LONG);
        response.set((long) Integer.MAX_VALUE);

        when(this.transaction.scard("sessions")).thenReturn(response);

        int result = this.store.getSize();

        assertEquals(Integer.MAX_VALUE, result);
        verify(this.transaction).exec();
    }

    @Test
    public void getSizeJedisConnectionException() {
        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        int result = this.store.getSize();

        assertEquals(Integer.MIN_VALUE, result);
    }

    @Test
    public void host() {
        this.store.setHost("test-host");

        assertEquals("test-host", this.store.getHost());
        verify(this.propertyChangeSupport).notify("host", "localhost", "test-host");
    }

    @Test
    public void initInternal() {
        SessionFlushValve valve = new SessionFlushValve();

        StandardContext context = (StandardContext) this.manager.getContext();
        context.addValve(new RemoteIpValve());
        context.addValve(valve);
        this.store.setManager(this.manager);

        this.store.initInternal();

        assertSame(this.store, valve.getStore());
    }

    @Test
    public void keys() throws IOException {
        Response<Set<String>> response = new Response<Set<String>>(BuilderFactory.STRING_SET);
        response.set(Arrays.asList("test-id".getBytes(Protocol.CHARSET)));

        when(this.transaction.smembers("sessions")).thenReturn(response);

        String[] result = this.store.keys();

        assertArrayEquals(new String[]{"test-id"}, result);
        verify(this.transaction).exec();
    }

    @Test
    public void keysJedisConnectionException() {
        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        String[] result = this.store.keys();

        assertArrayEquals(new String[0], result);
    }

    @Test
    public void load() throws IOException {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");

        Response<byte[]> response = new Response<byte[]>(BuilderFactory.BYTE_ARRAY);
        response.set(this.sessionSerializationUtils.serialize(session));

        when(this.transaction.get("test-id".getBytes(Protocol.CHARSET))).thenReturn(response);

        Session result = this.store.load("test-id");

        assertEquals(session.getId(), result.getId());
        verify(this.transaction).exec();
    }

    @Test
    public void loadJedisConnectionException() {
        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        this.store.setManager(this.manager);
        Session result = this.store.load("test-id");

        assertEquals(result.getId(), result.getId());
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
    public void remove() throws IOException {
        this.store.remove("test-id");

        verify(this.transaction).srem("sessions", "test-id");
        verify(this.transaction).del("test-id");
        verify(this.transaction).exec();
    }

    @Test
    public void removeJedisConnectionException() {
        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        this.store.remove("test-id");
    }

    @Test
    public void save() throws IOException {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");

        this.store.save(session);

        verify(this.transaction).set(session.getId().getBytes(Protocol.CHARSET), this.sessionSerializationUtils.serialize
                (session));
        verify(this.transaction).sadd("sessions", "test-id");
        verify(this.transaction).exec();
    }

    @Test
    public void saveJedisConnectionException() {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");

        when(this.jedisPool.getResource()).thenThrow(new JedisConnectionException("test-message"));

        this.store.save(session);

    }

    @Before
    public void setupJedis() throws Exception {
        when(this.jedisPool.getResource()).thenReturn(this.jedis);
        when(this.jedis.multi()).thenReturn(this.transaction);
    }

    @Before
    public void setupManager() {
        Context context = new StandardContext();
        Host host = new StandardHost();

        this.manager.setContext(context);
        context.setName("test-context-name");
        context.setParent(host);
        host.setName("test-host-name");
    }

    @Test
    public void startInternal() {
        this.store.setHost("test.host");
        this.store.setManager(this.manager);

        this.store.startInternal();

        verify(this.jedisPool).returnResource(this.jedis);
        verify(this.jmxSupport).register("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore", this.store);
    }

    @Test
    public void stopInternal() {
        this.store.setManager(this.manager);

        this.store.stopInternal();

        verify(this.jmxSupport).unregister("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore");
    }

    @Test
    public void stopInternalNoPool() {
        RedisStore alternateStore = new RedisStore(null, this.jmxSupport, this.propertyChangeSupport,
                this.sessionSerializationUtils);
        alternateStore.setManager(this.manager);

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

    @Test
    public void uriWithoutCredentials() {
        this.store.setUri("redis://test-host:1234/7");

        assertEquals("redis://test-host:1234/7", this.store.getUri());
        verify(this.propertyChangeSupport).notify("host", "localhost", "test-host");
        verify(this.propertyChangeSupport).notify("port", 6379, 1234);
        verify(this.propertyChangeSupport).notify("password", null, null);
        verify(this.propertyChangeSupport).notify("database", 0, 7);
    }


    private static class StubTransaction extends Transaction {

        @Override
        public Response<Long> del(String key) {
            return null;
        }

        @Override
        public Response<Long> del(String... keys) {
            return null;
        }

        @Override
        public Response<byte[]> get(byte[] key) {
            return null;
        }

        @Override
        public Response<Long> sadd(String key, String... member) {
            return null;
        }

        @Override
        public Response<Long> scard(String key) {
            return null;
        }

        @Override
        public Response<String> set(byte[] key, byte[] value) {
            return null;
        }

        @Override
        public Response<Set<String>> smembers(String key) {
            return null;
        }

        @Override
        public Response<Long> srem(String key, String... member) {
            return null;
        }
    }

}
