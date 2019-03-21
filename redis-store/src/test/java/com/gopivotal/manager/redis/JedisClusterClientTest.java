/*
 * Copyright 2017 the original author or authors.
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

package com.gopivotal.manager.redis;

import org.junit.Test;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JedisClusterClientTest {

    public static final String SESSIONS_KEY = "sessions";
    private final JedisCluster jedisCluster = mock(JedisCluster.class);
    private final JedisClusterClient jedisPoolTemplate = new JedisClusterClient(this.jedisCluster);
    private int timeout = 10;

    @Test
    public void getSessions() {
        Set<String> sessions = new HashSet<String>(Arrays.asList("s1"));
        when(this.jedisCluster.smembers(SESSIONS_KEY)).thenReturn(sessions);

        Set<String> result = this.jedisPoolTemplate.getSessions(SESSIONS_KEY);

        assertEquals(1, result.size());
        assertTrue(result.contains("s1"));
    }

    @Test
    public void del() {
        this.jedisPoolTemplate.del(SESSIONS_KEY, "key");

        verify(this.jedisCluster, times(1)).srem(SESSIONS_KEY, "key");
        verify(this.jedisCluster, times(1)).del("key");
    }

    @Test
    public void count() {
        when(this.jedisCluster.scard(SESSIONS_KEY)).thenReturn(10L);

        Integer result = this.jedisPoolTemplate.count(SESSIONS_KEY);

        assertEquals(10, result.intValue());
        verify(this.jedisCluster, times(1)).scard(SESSIONS_KEY);
    }

    @Test
    public void get() throws UnsupportedEncodingException {
        byte[] expected = "result".getBytes();
        when(this.jedisCluster.get("key".getBytes(Protocol.CHARSET))).thenReturn(expected);

        byte[] result = this.jedisPoolTemplate.get("key");

        assertEquals(expected, result);
        verify(this.jedisCluster, times(1)).get("key".getBytes(Protocol.CHARSET));
    }

    @Test
    public void set() throws UnsupportedEncodingException {
        byte[] session = "session".getBytes();
        this.jedisPoolTemplate.set("key", SESSIONS_KEY, session, timeout);

        verify(this.jedisCluster, times(1)).setex("key".getBytes(Protocol.CHARSET), timeout, session);
        verify(this.jedisCluster, times(1)).sadd(SESSIONS_KEY, "key");
    }

    @Test
    public void clean() throws UnsupportedEncodingException {
        HashSet<String> sessions = new HashSet<String>(Arrays.asList("key"));
        String[] sessionsArray = sessions.toArray(new String[sessions.size()]);
        when(this.jedisCluster.smembers(SESSIONS_KEY)).thenReturn(sessions);

        this.jedisPoolTemplate.clean(SESSIONS_KEY);

        verify(this.jedisCluster, times(1)).srem(SESSIONS_KEY, sessionsArray);
        verify(this.jedisCluster, times(1)).del(sessionsArray);
    }
}
