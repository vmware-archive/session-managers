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
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RedisStoreTest {

    private static final String SESSIONS_KEY = "sessions";
    
    private final JedisClient jedisClient = mock(JedisClient.class);

    private final JmxSupport jmxSupport = mock(JmxSupport.class);

    private final Manager manager = new StandardManager();

    private final SessionSerializationUtils sessionSerializationUtils = new SessionSerializationUtils(this.manager);

    private final PropertyChangeListener propertyChangeListener = mock(PropertyChangeListener.class);

    private final PropertyChangeSupport propertyChangeSupport = mock(PropertyChangeSupport.class);

    private final RedisStore store = new RedisStore(this.jmxSupport, this.propertyChangeSupport,
            this.sessionSerializationUtils, jedisClient);

    @Test
    public void clear() throws IOException {
        Set<String> sessionIds = new HashSet<>();
        sessionIds.add("test-id");
        when(this.jedisClient.getSessions(SESSIONS_KEY)).thenReturn(sessionIds);

        this.store.clear();

        verify(this.jedisClient).clean(SESSIONS_KEY);
    }

    @Test
    public void clearJedisConnectionException() {
        doThrow(new JedisConnectionException("test-message"))
                .when(this.jedisClient)
                .clean(SESSIONS_KEY);

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
        when(this.jedisClient.count(SESSIONS_KEY)).thenReturn(Integer.MAX_VALUE);

        int result = this.store.getSize();

        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    public void getSizeJedisConnectionException() {
        doThrow(new JedisConnectionException("test-message"))
                .when(this.jedisClient)
                .count(SESSIONS_KEY);

        int result = this.store.getSize();

        assertEquals(Integer.MIN_VALUE, result);
    }

    @Test
    public void sessionKeyPrefix() {
        this.store.setSessionKeyPrefix("_rsm_");

        assertEquals("_rsm_", this.store.getSessionKeyPrefix());
        verify(this.propertyChangeSupport).notify("sessionKeyPrefix", SESSIONS_KEY, "_rsm_");
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
        Set<String> response = new HashSet<String>(Arrays.asList("test-id"));
        when(this.jedisClient.getSessions(SESSIONS_KEY)).thenReturn(response);

        String[] result = this.store.keys();

        assertEquals(1, result.length);
        assertArrayEquals(new String[]{"test-id"}, result);
    }

    @Test
    public void keysJedisConnectionException() {
        doThrow(new JedisConnectionException("test-message"))
                .when(this.jedisClient)
                .getSessions(SESSIONS_KEY);

        String[] result = this.store.keys();

        assertArrayEquals(new String[0], result);
    }

    @Test
    public void load() throws IOException {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");
        byte[] response = this.sessionSerializationUtils.serialize(session);

        when(this.jedisClient.get("test-id")).thenReturn(response);

        Session result = this.store.load("test-id");

        assertEquals(session.getId(), result.getId());
    }

    @Test
    public void loadJedisConnectionException() throws UnsupportedEncodingException {
        when(this.jedisClient.get("test-id")).thenThrow(new JedisConnectionException("test-message"));
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

        verify(this.jedisClient).del(SESSIONS_KEY, "test-id");
    }

    @Test
    public void removeJedisConnectionException() {
        doThrow(new JedisConnectionException("test-message"))
                .when(this.jedisClient)
                .del(SESSIONS_KEY, "test-id");

        this.store.remove("test-id");
    }

    @Test
    public void save() throws IOException {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");

        this.store.save(session);

        verify(this.jedisClient).set(getRedisSessionId(session), SESSIONS_KEY, this.sessionSerializationUtils.serialize(session), session.getMaxInactiveInterval());
    }

    private String getRedisSessionId(Session session) {
        return SESSIONS_KEY + session.getId();
    }

    @Test
    public void saveJedisConnectionException() throws UnsupportedEncodingException {
        Session session = new StandardSession(this.manager);
        session.setId("test-id");

        doThrow(new JedisConnectionException("test-message"))
                .when(this.jedisClient)
                .set(anyString(), anyString(), any((byte[].class)), eq(session.getMaxInactiveInterval()));

        this.store.save(session);
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
    public void startInternalWithPool() throws IOException {
        this.store.setHost("test.host");
        this.store.setManager(this.manager);

        this.store.startInternal();

        verify(this.jedisClient).close();
        verify(this.jmxSupport).register("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore", this.store);
        assertEquals(this.store.jedisClient.getClass(), JedisNodeClient.class);
    }

    @Test
    public void startInternalWithCluster() throws IOException {
        this.store.setHost("test.host:123;test.host2:456");
        this.store.setManager(this.manager);
        this.store.setCluster(true);

        this.store.startInternal();

        verify(this.jedisClient).close();
        verify(this.jmxSupport).register("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore", this.store);
        assertEquals(this.store.jedisClient.getClass(), JedisClusterClient.class);
    }

    @Test
    public void stopInternal() {
        this.store.setManager(this.manager);

        this.store.stopInternal();

        verify(this.jmxSupport).unregister("Catalina:type=Store,context=/test-context-name,host=test-host-name," +
                "name=RedisStore");
    }

    @Test
    public void stopInternalNoTemplate() {
        RedisStore alternateStore = new RedisStore(this.jmxSupport, this.propertyChangeSupport,
                this.sessionSerializationUtils, null);
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
}
