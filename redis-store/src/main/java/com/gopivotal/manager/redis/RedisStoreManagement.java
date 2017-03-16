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

import javax.management.MXBean;

/**
 * Management interface for the {@link com.gopivotal.manager.redis.RedisStore}
 */
@MXBean
public interface RedisStoreManagement {

    /**
     * Returns the Redis connection pool size;
     *
     * @return the Redis connection pool size;
     */
    int getConnectionPoolSize();

    /**
     * Returns the Redis connection database
     *
     * @return the Redis connection database
     */
    int getDatabase();

    /**
     * Returns the Redis connection host
     *
     * @return the Redis connection host
     */
    String getHost();

    /**
     * Returns the Redis connection password
     *
     * @return the Redis connection password
     */
    String getPassword();

    /**
     * Returns the Redis connection port
     *
     * @return the Redis connection port
     */
    int getPort();

    /**
     * Returns whether SSL enabled for Redis
     *
     * @return whether Redis SSL enabled
     */
    boolean isSSLEnabled();

    /**
     * Returns the Redis connection timeout
     *
     * @return the Redis connection timeout
     */
    int getTimeout();

    /**
     * Returns the Redis connection uri
     *
     * @return the Redis connection uri
     */
    String getUri();
}
