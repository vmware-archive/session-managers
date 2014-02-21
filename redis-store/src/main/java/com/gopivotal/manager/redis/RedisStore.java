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

import com.gopivotal.manager.AbstractLifecycle;
import com.gopivotal.manager.PropertyChangeSupport;
import com.gopivotal.manager.SessionFlushValve;
import com.gopivotal.manager.StandardPropertyChangeSupport;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * An implementation of {@link Store} that persists data to Redis
 */
public final class RedisStore extends AbstractLifecycle implements Store {

    private static final String INFO = "RedisStore/1.0";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ReadWriteLock monitor = new ReentrantReadWriteLock();

    private final PropertyChangeSupport propertyChangeSupport;

    private volatile int database = Protocol.DEFAULT_DATABASE;

    private volatile String host = "localhost";

    private volatile JedisPool jedisPool;

    private volatile Manager manager;

    private volatile String password;

    private volatile int port = Protocol.DEFAULT_PORT;

    private volatile int timeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * Create a new instance
     *
     * @see com.gopivotal.manager.StandardPropertyChangeSupport
     */
    public RedisStore() {
        this.propertyChangeSupport = new StandardPropertyChangeSupport(this);
    }

    RedisStore(JedisPool jedisPool, PropertyChangeSupport propertyChangeSupport) {
        this.jedisPool = jedisPool;
        this.propertyChangeSupport = propertyChangeSupport;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.add(propertyChangeListener);
    }

    @Override
    public void clear() throws IOException {
        // TODO
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public Manager getManager() {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            return this.manager;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setManager(Manager manager) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            Manager previous = this.manager;
            this.manager = manager;
            this.propertyChangeSupport.notify("manager", previous, this.manager);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getSize() throws IOException {
        return 0;                                      // TODO
    }

    @Override
    public String[] keys() throws IOException {
        return new String[0];                  // TODO
    }

    @Override
    public Session load(String id) throws ClassNotFoundException, IOException {
        return null;                                                           // TODO
    }

    @Override
    public void remove(String id) throws IOException {
        // TODO
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.remove(propertyChangeListener);
    }

    @Override
    public void save(Session session) throws IOException {
        // TODO
    }

    /**
     * Sets the database to connect to
     *
     * @param database the database to connect to
     */
    public void setDatabase(int database) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            int previous = this.database;
            this.database = database;
            this.propertyChangeSupport.notify("database", previous, this.database);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the host to connect to
     *
     * @param host the host to connect to
     */
    public void setHost(String host) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            String previous = this.host;
            this.host = host;
            this.propertyChangeSupport.notify("host", previous, this.host);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the password to use when connecting
     *
     * @param password the password to use when connecting
     */
    public void setPassword(String password) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            String previous = this.password;
            this.password = password;
            this.propertyChangeSupport.notify("password", previous, this.password);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the port to connect to
     *
     * @param port the port to connect to
     */
    public void setPort(int port) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            int previous = this.port;
            this.port = port;
            this.propertyChangeSupport.notify("port", previous, this.port);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the connection timeout
     *
     * @param timeout the connection timeout
     */
    public void setTimeout(int timeout) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            int previous = this.timeout;
            this.timeout = timeout;
            this.propertyChangeSupport.notify("timeout", previous, this.timeout);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the connection URI
     *
     * @param uri the connection URI
     */
    public void setUri(URI uri) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            setHost(uri.getHost());
            setPort(uri.getPort());
            setPassword(parsePassword(uri));
            setDatabase(parseDatabase(uri));
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void initInternal() {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            for (Valve valve : this.manager.getContainer().getPipeline().getValves()) {
                if (valve instanceof SessionFlushValve) {
                    this.logger.fine(String.format("Setting '%s' as the store for '%s'", this, valve));
                    ((SessionFlushValve) valve).setStore(this);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void startInternal() {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            if (jedisPool == null) {
                this.jedisPool = new JedisPool(new JedisPoolConfig(), this.host, this.port, this.timeout,
                        this.password, this.database);
            }
            connect();
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void stopInternal() {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            if (this.jedisPool != null) {
                this.logger.info("Closing connection to Redis Server");
                this.jedisPool.destroy();
            }
        } finally {
            lock.unlock();
        }
    }

    private void connect() {
        Jedis jedis = null;
        try {
            this.logger.info(String.format("Connecting to Redis Server at redis://%s:%d/%d", this.host, this.port,
                    this.database));
            jedis = this.jedisPool.getResource();
        } finally {
            returnResourceQuietly(jedis);
        }
    }

    private int parseDatabase(URI uri) {
        return Integer.parseInt(uri.getPath().split("/", 2)[1]);
    }

    private String parsePassword(URI uri) {
        return uri.getUserInfo().split(":", 2)[1];
    }

    private void returnResourceQuietly(Jedis jedis) {
        if (jedis != null) {
            try {
                this.jedisPool.returnResource(jedis);
            } catch (RuntimeException e) {
                this.logger.warning(String.format("Exception encountered when return Jedis resource: %s",
                        e.getMessage()));
            }
        }
    }
}
