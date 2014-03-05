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
import redis.clients.jedis.exceptions.JedisConnectionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JedisTemplateTest {

    private final Jedis jedis = mock(Jedis.class);

    private final JedisPool jedisPool = mock(JedisPool.class);

    private final JedisTemplate jedisTemplate = new JedisTemplate(this.jedisPool);

    @SuppressWarnings("unchecked")
    private final JedisTemplate.JedisOperation<String> operation = mock(JedisTemplate.JedisOperation.class);

    @Before
    public void jedis() throws Exception {
        when(this.jedisPool.getResource()).thenReturn(this.jedis);
    }

    @Test
    public void returnBrokenResourceException() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.operation.invoke(this.jedis)).thenThrow(expected);
        doThrow(new JedisConnectionException("test-message")).when(this.jedisPool).returnBrokenResource(this.jedis);

        try {
            this.jedisTemplate.withJedis(this.operation);
            fail();
        } catch (JedisConnectionException e) {
            assertSame(expected, e);
        }
    }

    @Test
    public void returnResourceException() throws Exception {
        when(this.operation.invoke(this.jedis)).thenReturn("test-value");
        doThrow(new JedisConnectionException("test-message")).when(this.jedisPool).returnResource(this.jedis);

        String result = this.jedisTemplate.withJedis(this.operation);

        assertEquals("test-value", result);
    }

    @Test
    public void withJedis() throws Exception {
        when(this.operation.invoke(this.jedis)).thenReturn("test-value");

        String result = this.jedisTemplate.withJedis(this.operation);

        assertEquals("test-value", result);
        verify(this.jedisPool).returnResource(this.jedis);
    }

    @Test
    public void withJedisException() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.operation.invoke(this.jedis)).thenThrow(expected);

        try {
            this.jedisTemplate.withJedis(this.operation);
            fail();
        } catch (JedisConnectionException e) {
            assertSame(expected, e);
        }

        verify(this.jedisPool).returnBrokenResource(this.jedis);
    }

    @Test
    public void withJedisResourceException() throws Exception {
        JedisConnectionException expected = new JedisConnectionException("test-message");
        when(this.jedisPool.getResource()).thenThrow(expected);

        try {
            this.jedisTemplate.withJedis(this.operation);
            fail();
        } catch (JedisConnectionException e) {
            assertSame(expected, e);
        }

        verify(this.jedisPool, times(0)).returnBrokenResource(this.jedis);
    }
}
