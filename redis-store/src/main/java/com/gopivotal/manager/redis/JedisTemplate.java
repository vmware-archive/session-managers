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
import redis.clients.jedis.exceptions.JedisConnectionException;

final class JedisTemplate {

    private final JedisPool jedisPool;

    JedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    <T> T withJedis(JedisOperation<T> operation) {
        try(Jedis jedis = this.jedisPool.getResource()) {
            return operation.invoke(jedis);
        } catch (JedisConnectionException e) {
            throw e;
        }
    }

    interface JedisOperation<T> {

        /**
         * Invoke the operation
         *
         * @param jedis the {@link Jedis} instance to use
         * @return the return value of the operation
         */
        T invoke(Jedis jedis);
    }

}
