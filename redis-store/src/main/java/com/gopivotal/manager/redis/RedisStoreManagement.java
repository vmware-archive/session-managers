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
     * Will be ignore in case of cluster
     *
     * @return the Redis connection database
     */
    int getDatabase();

    /**
     * Returns the Redis sessions key prefix
     * Allows to configure a prefix that's added to the session id when a session is stored in Redis.
     * Useful for situations where 1 redis cluster serves multiple application clusters with potentially conflicting session IDs.
     * 
     * @return the Redis sessions key prefix
     */
    String getSessionKeyPrefix();
    
    /**
     * Returns the Redis connection host
     * In case of cluster must follow this pattern:
     * <p>
     *     <host>:<port>;<host>:<port>;<host>:<port>
     * </p>
     * All hosts, including the master, should be provided
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
     * Will be ignore in case of cluster
     *
     * @return the Redis connection port
     */
    int getPort();

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

    /**
     * Returns if it is a redis cluster
     * @return redis cluster indication
     */
    boolean getCluster();
}
