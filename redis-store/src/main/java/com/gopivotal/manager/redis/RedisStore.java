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
import com.gopivotal.manager.JmxSupport;
import com.gopivotal.manager.LockTemplate;
import com.gopivotal.manager.PropertyChangeSupport;
import com.gopivotal.manager.SessionFlushValve;
import com.gopivotal.manager.StandardJmxSupport;
import com.gopivotal.manager.StandardPropertyChangeSupport;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * An implementation of {@link Store} that persists data to Redis
 */
public final class RedisStore extends AbstractLifecycle implements RedisStoreManagement, Store {

    private static final String INFO = "RedisStore/1.0";

    private final JmxSupport jmxSupport;

    private final LockTemplate lockTemplate = new LockTemplate();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final PropertyChangeSupport propertyChangeSupport;

    private volatile int connectionPoolSize = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;

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
        this.logger.info(String.format("Sessions will be persisted to Redis using a %s", this.getClass().getName()));
        this.jmxSupport = new StandardJmxSupport();
        this.propertyChangeSupport = new StandardPropertyChangeSupport(this);
    }

    RedisStore(JedisPool jedisPool, JmxSupport jmxSupport, PropertyChangeSupport propertyChangeSupport) {
        this.jedisPool = jedisPool;
        this.jmxSupport = jmxSupport;
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
    public int getConnectionPoolSize() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<Integer>() {

            @Override
            public Integer invoke()  {
                return RedisStore.this.connectionPoolSize;
            }

        });
    }

    /**
     * Sets the connection pool size
     *
     * @param connectionPoolSize the connectionPoolSize
     */
    public void setConnectionPoolSize(final int connectionPoolSize) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                int previous = RedisStore.this.connectionPoolSize;
                RedisStore.this.connectionPoolSize = connectionPoolSize;
                RedisStore.this.propertyChangeSupport.notify("connectionPoolSize", previous,
                        RedisStore.this.connectionPoolSize);
            }

        });
    }

    @Override
    public int getDatabase() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<Integer>() {

            @Override
            public Integer invoke()  {
                return RedisStore.this.database;
            }

        });
    }

    /**
     * Sets the database to connect to
     *
     * @param database the database to connect to
     */
    public void setDatabase(final int database) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                int previous = RedisStore.this.database;
                RedisStore.this.database = database;
                RedisStore.this.propertyChangeSupport.notify("database", previous, RedisStore.this.database);
            }

        });
    }

    @Override
    public String getHost() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<String>() {

            @Override
            public String invoke()  {
                return RedisStore.this.host;
            }

        });
    }

    /**
     * Sets the host to connect to
     *
     * @param host the host to connect to
     */
    public void setHost(final String host) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                String previous = RedisStore.this.host;
                RedisStore.this.host = host;
                RedisStore.this.propertyChangeSupport.notify("host", previous, RedisStore.this.host);
            }

        });
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public Manager getManager() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<Manager>() {

            @Override
            public Manager invoke()  {
                return RedisStore.this.manager;
            }

        });
    }

    @Override
    public void setManager(final Manager manager) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke() {
                Manager previous = RedisStore.this.manager;
                RedisStore.this.manager = manager;
                RedisStore.this.propertyChangeSupport.notify("manager", previous, RedisStore.this.manager);
            }

        });
    }

    @Override
    public String getPassword() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<String>() {

            @Override
            public String invoke()  {
                return RedisStore.this.password;
            }

        });
    }

    /**
     * Sets the password to use when connecting
     *
     * @param password the password to use when connecting
     */
    public void setPassword(final String password) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                String previous = RedisStore.this.password;
                RedisStore.this.password = password;
                RedisStore.this.propertyChangeSupport.notify("password", previous, RedisStore.this.password);
            }

        });
    }

    @Override
    public int getPort() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<Integer>() {

            @Override
            public Integer invoke()  {
                return RedisStore.this.port;
            }

        });
    }

    /**
     * Sets the port to connect to
     *
     * @param port the port to connect to
     */
    public void setPort(final int port) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                int previous = RedisStore.this.port;
                RedisStore.this.port = port;
                RedisStore.this.propertyChangeSupport.notify("port", previous, RedisStore.this.port);
            }

        });
    }

    @Override
    public int getSize() throws IOException {
        return 0;                                      // TODO
    }

    @Override
    public int getTimeout() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<Integer>() {

            @Override
            public Integer invoke()  {
                return RedisStore.this.timeout;
            }

        });
    }

    /**
     * Sets the connection timeout
     *
     * @param timeout the connection timeout
     */
    public void setTimeout(final int timeout) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                int previous = RedisStore.this.timeout;
                RedisStore.this.timeout = timeout;
                RedisStore.this.propertyChangeSupport.notify("timeout", previous, RedisStore.this.timeout);
            }

        });
    }

    @Override
    public String getUri() {
        return this.lockTemplate.withReadLock(new LockTemplate.ReturningOperation<String>() {
            @Override
            public String invoke()  {
                return String.format("redis://:%s@%s:%d/%d", RedisStore.this.password, RedisStore.this.host,
                        RedisStore.this.port, RedisStore.this.database);
            }
        });
    }

    /**
     * Sets the connection URI
     *
     * @param uri the connection URI
     */
    public void setUri(final URI uri) {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                setHost(uri.getHost());
                setPort(uri.getPort());
                setPassword(parsePassword(uri));
                setDatabase(parseDatabase(uri));
            }

        });
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

    @Override
    protected void initInternal() {
        this.lockTemplate.withReadLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                for (Valve valve : RedisStore.this.manager.getContainer().getPipeline().getValves()) {
                    if (valve instanceof SessionFlushValve) {
                        RedisStore.this.logger.fine(String.format("Setting '%s' as the store for '%s'", this, valve));
                        ((SessionFlushValve) valve).setStore(RedisStore.this);
                    }
                }
            }

        });
    }

    @Override
    protected void startInternal() {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                if (RedisStore.this.jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(RedisStore.this.connectionPoolSize);

                    RedisStore.this.jedisPool = new JedisPool(poolConfig, RedisStore.this.host, RedisStore.this.port,
                            RedisStore.this.timeout, RedisStore.this.password, RedisStore.this.database);
                }

                connect();
                RedisStore.this.jmxSupport.register(getObjectName(), RedisStore.this);
            }

        });
    }

    @Override
    protected void stopInternal() {
        this.lockTemplate.withWriteLock(new LockTemplate.Operation() {

            @Override
            public void invoke()  {
                if (RedisStore.this.jedisPool != null) {
                    RedisStore.this.logger.info("Closing connection to Redis Server");
                    RedisStore.this.jedisPool.destroy();
                }

                RedisStore.this.jmxSupport.unregister(getObjectName());
            }

        });
    }

    private void connect() {
        Jedis jedis = null;
        try {
            this.logger.info(String.format("Connecting to Redis Server at redis://%s:%d/%d", this.host, this.port,
                    this.database));
            this.logger.info("Connection to Redis Server successful");
            jedis = this.jedisPool.getResource();
        } finally {
            returnResourceQuietly(jedis);
        }
    }

    private String getContext() {
        String name = this.manager.getContainer().getName();
        return name.startsWith("/") ? name : String.format("/%s", name);
    }

    private String getObjectName() {
        String contextPath = getContext();
        String hostName = this.manager.getContainer().getParent().getName();

        return String.format("Catalina:type=Store,context=%s,host=%s,name=%s", contextPath, hostName,
                getClass().getSimpleName());
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
