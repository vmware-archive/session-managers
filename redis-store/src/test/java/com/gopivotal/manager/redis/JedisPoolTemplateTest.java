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

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JedisPoolTemplateTest {

    public static final String SESSIONS_KEY = "sessions";
    private final Jedis jedis = mock(Jedis.class);

    private final JedisPool jedisPool = mock(JedisPool.class);

    private final Transaction transaction = mock(StubTransaction.class);

    private final JedisPoolTemplate jedisPoolTemplate = new JedisPoolTemplate(this.jedisPool);

    @Before
    public void jedis() throws Exception {
        when(this.jedisPool.getResource()).thenReturn(this.jedis);
        when(this.jedis.multi()).thenReturn(this.transaction);
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnGetSessionsFail() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.jedis.smembers(SESSIONS_KEY)).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();

        this.jedisPoolTemplate.getSessions(SESSIONS_KEY);

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void getSessions() {
        Set<String> sessions = new HashSet<String>(Arrays.asList("s1"));
        when(this.jedis.smembers(SESSIONS_KEY)).thenReturn(sessions);

        Set<String> result = this.jedisPoolTemplate.getSessions(SESSIONS_KEY);

        assertEquals(1, result.size());
        assertTrue(result.contains("s1"));

        verify(this.jedis, times(1)).close();
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnDelFail() throws Exception {
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();

        this.jedisPoolTemplate.del(SESSIONS_KEY,"key");

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void del() {
        this.jedisPoolTemplate.del(SESSIONS_KEY, "key");

        verify(this.transaction, times(1)).srem(SESSIONS_KEY, "key");
        verify(this.transaction, times(1)).del("key");
        verify(this.transaction, times(1)).exec();

        verify(this.jedis, times(1)).close();
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnCountFail() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.jedis.scard(SESSIONS_KEY)).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();

        this.jedisPoolTemplate.count(SESSIONS_KEY);

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void count() {
        when(this.jedis.scard(SESSIONS_KEY)).thenReturn(10L);

        Integer result = this.jedisPoolTemplate.count(SESSIONS_KEY);

        assertEquals(10, result.intValue());
        verify(this.jedis, times(1)).scard(SESSIONS_KEY);
        verify(this.jedis, times(1)).close();
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnGetFail() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.jedis.get("key".getBytes(Protocol.CHARSET))).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();

        this.jedisPoolTemplate.get("key");

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void get() throws UnsupportedEncodingException {
        byte[] expected = "result".getBytes();
        when(this.jedis.get("key".getBytes(Protocol.CHARSET))).thenReturn(expected);

        byte[] result = this.jedisPoolTemplate.get("key");

        assertEquals(expected, result);
        verify(this.jedis, times(1)).get("key".getBytes(Protocol.CHARSET));
        verify(this.jedis, times(1)).close();
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnSetFail() throws Exception {
        byte[] session = "session".getBytes();
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.transaction.set("key".getBytes(Protocol.CHARSET), session)).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();

        this.jedisPoolTemplate.set("key", SESSIONS_KEY, session);

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void set() throws UnsupportedEncodingException {
        byte[] session = "session".getBytes();
        this.jedisPoolTemplate.set("key", SESSIONS_KEY, session);

        verify(this.transaction, times(1)).set("key".getBytes(Protocol.CHARSET), session);
        verify(this.transaction, times(1)).sadd(SESSIONS_KEY,"key");
        verify(this.transaction, times(1)).exec();

        verify(this.jedis, times(1)).close();
    }

    @Test(expected = JedisConnectionException.class)
    public void returnResourceOnCleanFail() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.transaction.del(new String[]{"key"})).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedis).close();
        when(this.jedis.smembers(SESSIONS_KEY)).thenReturn(new HashSet<String>(Arrays.asList("key")));

        this.jedisPoolTemplate.clean(SESSIONS_KEY);

        verify(this.jedis, times(1)).close();
    }

    @Test
    public void clean() throws UnsupportedEncodingException {
        HashSet<String> sessions = new HashSet<String>(Arrays.asList("key"));
        String[] sessionsArray = sessions.toArray(new String[sessions.size()]);
        when(this.jedis.smembers(SESSIONS_KEY)).thenReturn(sessions);

        this.jedisPoolTemplate.clean( SESSIONS_KEY);

        verify(this.transaction, times(1)).srem(SESSIONS_KEY, sessionsArray);
        verify(this.transaction, times(1)).del(sessionsArray);
        verify(this.transaction, times(1)).exec();

        verify(this.jedis, times(1)).close();
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
