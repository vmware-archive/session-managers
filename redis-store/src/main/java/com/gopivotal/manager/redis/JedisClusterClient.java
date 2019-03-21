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

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Created by marcelo on 23/02/17.
 */
public class JedisClusterClient implements JedisClient {
    private JedisCluster jedisCluster;

    JedisClusterClient(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Set<String> getSessions(String sessionsKey) {
        return jedisCluster.smembers(sessionsKey);
    }

    @Override
    public void del(String sessionsKey, String key) {
        jedisCluster.srem(sessionsKey, key);
        jedisCluster.del(key);
    }

    @Override
    public Integer count(String sessionsKey) {
        return jedisCluster.scard(sessionsKey).intValue();
    }

    @Override
    public byte[] get(String key) throws UnsupportedEncodingException {
        return jedisCluster.get(key.getBytes(Protocol.CHARSET));
    }

    @Override
    public void set(String key, String sessionsKey, byte[] session, int timeout) throws UnsupportedEncodingException {
        jedisCluster.setex(key.getBytes(Protocol.CHARSET), timeout, session);
        jedisCluster.sadd(sessionsKey, key);
    }

    @Override
    public void clean(String sessionsKey) {
        Set<String> sessions = jedisCluster.smembers(sessionsKey);
        String[] sessionsArray = sessions.toArray(new String[sessions.size()]);

        jedisCluster.srem(sessionsKey, sessionsArray);
        jedisCluster.del(sessionsArray);
    }

    @Override
    public void close() throws IOException {
        jedisCluster.close();
    }
}
