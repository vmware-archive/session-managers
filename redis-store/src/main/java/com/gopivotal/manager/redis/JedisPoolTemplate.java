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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Transaction;

import java.io.UnsupportedEncodingException;
import java.util.Set;

final class JedisPoolTemplate implements JedisTemplate {

    private final JedisPool jedisPool;

    JedisPoolTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Set<String> getSessions(String sessionsKey) {
        try(Jedis jedis = this.jedisPool.getResource()) {
            return jedis.smembers(sessionsKey);
        }
    }

    @Override
    public void del(String sessionsKey, String key) {
        try(Jedis jedis = this.jedisPool.getResource()) {
            Transaction t = jedis.multi();
            t.srem(sessionsKey, key);
            t.del(key);
            t.exec();
        }
    }

    @Override
    public Integer count(String sessionsKey) {
        try(Jedis jedis = this.jedisPool.getResource()) {
            return jedis.scard(sessionsKey).intValue();
        }
    }

    @Override
    public byte[] get(String key) throws UnsupportedEncodingException {
        try(Jedis jedis = this.jedisPool.getResource()) {
            return jedis.get(key.getBytes(Protocol.CHARSET));
        }
    }

    @Override
    public void set(String key, String sessionsKey, byte[] session) throws UnsupportedEncodingException {
        try(Jedis jedis = this.jedisPool.getResource()) {
            Transaction t = jedis.multi();
            t.set(key.getBytes(Protocol.CHARSET), session);
            t.sadd(sessionsKey, key);
            t.exec();
        }
    }

    @Override
    public void clean(String sessionsKey) {
        try(Jedis jedis = this.jedisPool.getResource()) {
            Set<String> sessions = jedis.smembers(sessionsKey);
            String[] sessionsArray = sessions.toArray(new String[sessions.size()]);

            Transaction t = jedis.multi();
            t.srem(sessionsKey, sessionsArray);
            t.del(sessionsArray);
            t.exec();
        }
    }

    @Override
    public void close() {
        jedisPool.destroy();
    }
}
