/*
 * Copyright 2014 the original author or authors.
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

import com.gopivotal.manager.AbstractLifecycle;
import com.gopivotal.manager.JmxSupport;
import com.gopivotal.manager.LockTemplate;
import com.gopivotal.manager.PropertyChangeSupport;
import com.gopivotal.manager.SessionFlushValve;
import com.gopivotal.manager.SessionSerializationUtils;
import com.gopivotal.manager.StandardJmxSupport;
import com.gopivotal.manager.StandardPropertyChangeSupport;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static redis.clients.jedis.Protocol.DEFAULT_TIMEOUT;

/**
 * An implementation of {@link Store} that persists data to Redis
 */
public final class RedisStore extends AbstractLifecycle implements RedisStoreManagement, Store {

    public static final int DEFAULT_SO_TIMEOUT = 2000;
    private static final String SESSIONS_KEY = "sessions";
    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private final JmxSupport jmxSupport;
    private final LockTemplate lockTemplate = new LockTemplate();
    private final Logger logger = LoggerFactory.getLogger(RedisStore.class);
    private final PropertyChangeSupport propertyChangeSupport;
    protected volatile JedisClient jedisClient;
    private volatile Manager manager;
    private volatile int connectionPoolSize = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    private volatile int database = Protocol.DEFAULT_DATABASE;
    private volatile String host = "localhost";
    private boolean cluster = false;
    private volatile String password;
    private volatile int port = Protocol.DEFAULT_PORT;
    private volatile int timeout = DEFAULT_TIMEOUT;
    private volatile SessionSerializationUtils sessionSerializationUtils;

    /**
     * Create a new instance
     *
     * @see com.gopivotal.manager.StandardPropertyChangeSupport
     */
    public RedisStore() {
        Package pkg = this.getClass().getPackage();
        this.logger.info("{} {}, {}", pkg.getImplementationVendor(), pkg.getImplementationTitle(),
                pkg.getImplementationVersion());
        this.logger.info(String.format("Sessions will be persisted to Redis using a %s", this.getClass().getName()));
        this.jmxSupport = new StandardJmxSupport();
        this.propertyChangeSupport = new StandardPropertyChangeSupport(this);
    }

    RedisStore(JmxSupport jmxSupport, PropertyChangeSupport propertyChangeSupport,
               SessionSerializationUtils sessionSerializationUtils, JedisClient jedisClient) {
        this.jedisClient = jedisClient;
        this.jmxSupport = jmxSupport;
        this.propertyChangeSupport = propertyChangeSupport;
        this.sessionSerializationUtils = sessionSerializationUtils;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.add(propertyChangeListener);
    }

    @Override
    public void clear() {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                try {
                    RedisStore.this.jedisClient.clean(SESSIONS_KEY);
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.error("Unable to clear persisted sessions", e);
                }
                return null;
            }

        });
    }

    @Override
    public int getConnectionPoolSize() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Integer>() {

            @Override
            public Integer invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting connectionPoolSize={}", connectionPoolSize);
                int previous = RedisStore.this.connectionPoolSize;
                RedisStore.this.connectionPoolSize = connectionPoolSize;
                RedisStore.this.propertyChangeSupport.notify("connectionPoolSize", previous,
                        RedisStore.this.connectionPoolSize);
                return null;
            }

        });
    }

    @Override
    public int getDatabase() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Integer>() {

            @Override
            public Integer invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting database={}", database);
                int previous = RedisStore.this.database;
                RedisStore.this.database = database;
                RedisStore.this.propertyChangeSupport.notify("database", previous, RedisStore.this.database);
                return null;
            }

        });
    }

    @Override
    public String getHost() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<String>() {

            @Override
            public String invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting host={}", host);
                String previous = RedisStore.this.host;
                RedisStore.this.host = host;
                RedisStore.this.propertyChangeSupport.notify("host", previous, RedisStore.this.host);
                return null;
            }

        });
    }

    @Override
    public Manager getManager() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Manager>() {

            @Override
            public Manager invoke() {
                return RedisStore.this.manager;
            }

        });
    }

    @Override
    public void setManager(final Manager manager) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                Manager previous = RedisStore.this.manager;
                RedisStore.this.manager = manager;
                RedisStore.this.sessionSerializationUtils = new SessionSerializationUtils(manager);
                RedisStore.this.propertyChangeSupport.notify("manager", previous, RedisStore.this.manager);
                return null;
            }

        });
    }

    @Override
    public String getPassword() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<String>() {

            @Override
            public String invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting password=*");
                String previous = RedisStore.this.password;
                RedisStore.this.password = password;
                RedisStore.this.propertyChangeSupport.notify("password", previous, RedisStore.this.password);
                return null;
            }

        });
    }

    @Override
    public int getPort() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Integer>() {

            @Override
            public Integer invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting port={}", port);
                int previous = RedisStore.this.port;
                RedisStore.this.port = port;
                RedisStore.this.propertyChangeSupport.notify("port", previous, RedisStore.this.port);
                return null;
            }

        });
    }

    @Override
    public int getSize() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Integer>() {

            @Override
            public Integer invoke() {
                try {
                    return RedisStore.this.jedisClient.count(SESSIONS_KEY);
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.error("Unable to get the number of persisted sessions", e);
                    return Integer.MIN_VALUE;
                }
            }

        });
    }

    @Override
    public int getTimeout() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Integer>() {

            @Override
            public Integer invoke() {
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
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting timeout={}", timeout);
                int previous = RedisStore.this.timeout;
                RedisStore.this.timeout = timeout;
                RedisStore.this.propertyChangeSupport.notify("timeout", previous, RedisStore.this.timeout);
                return null;
            }

        });
    }

    @Override
    public String getUri() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<String>() {
            @Override
            public String invoke() {
                return String.format("redis://%s%s:%d/%d", getUserInfo(), RedisStore.this.host,
                        RedisStore.this.port, RedisStore.this.database);
            }
        });
    }

    @Override
    public boolean getCluster() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Boolean>() {

            @Override
            public Boolean invoke() {
                return RedisStore.this.cluster;
            }

        });
    }

    /**
     * Sets the cluster mode
     *
     * @param cluster
     */
    public void setCluster(final boolean cluster) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                boolean previous = RedisStore.this.cluster;
                RedisStore.this.cluster = cluster;
                RedisStore.this.propertyChangeSupport.notify("cluster", previous, RedisStore.this.cluster);
                return null;
            }
        });
    }

    /**
     * Sets the connection URI
     *
     * @param uri the connection URI
     */
    public void setUri(final String uri) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                RedisStore.this.logger.info("setting uri={}", uri);
                URI richUri = URI.create(uri);
                setHost(richUri.getHost());
                setPort(richUri.getPort());
                setPassword(parsePassword(richUri));
                setDatabase(parseDatabase(richUri));
                return null;
            }

        });
    }

    @Override
    public String[] keys() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<String[]>() {

            @Override
            public String[] invoke() {
                try {
                    Set<String> sessions = RedisStore.this.jedisClient.getSessions(SESSIONS_KEY);
                    return sessions.toArray(new String[sessions.size()]);
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.error("Unable to get the keys of persisted sessions", e);
                    return new String[0];
                }
            }
        });
    }

    @Override
    public Session load(final String id) {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Session>() {

            @Override
            public Session invoke() {
                try {
                    byte[] session = RedisStore.this.jedisClient.get(id);
                    return session == null ? RedisStore.this.manager.createSession(id) : RedisStore.this.sessionSerializationUtils.deserialize(session);
                } catch (JedisConnectionException e) {
                    return logAndCreateEmptySession(id, e);
                } catch (ClassNotFoundException e) {
                    return logAndCreateEmptySession(id, e);
                } catch (IOException e) {
                    return logAndCreateEmptySession(id, e);
                }
            }
        });
    }

    @Override
    public void remove(final String id) {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                try {
                    RedisStore.this.jedisClient.del(SESSIONS_KEY, id);
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.error("Unable to remove session {}", id, e);
                }

                return null;
            }

        });
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.remove(propertyChangeListener);
    }

    @Override
    public void save(final Session session) {
        this.lockTemplate.withReadLock(
                new LockTemplate.LockedOperation<Void>() {
                    @Override
                    public Void invoke() {
                        try {
                            byte[] serialized = RedisStore.this.sessionSerializationUtils.serialize(session);
                            RedisStore.this.jedisClient.set(session.getId(), SESSIONS_KEY, serialized, session.getMaxInactiveInterval());
                        } catch (JedisConnectionException e) {
                            RedisStore.this.logger.error("Unable to persist session {}", session.getId(), e);
                        } catch (IOException e) {
                            RedisStore.this.logger.error("Unable to save session {}", session.getId(), e);
                        }
                        return null;
                    }
                }

        );
    }

    @Override
    protected void initInternal() {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                for (Valve valve : RedisStore.this.manager.getContext().getPipeline().getValves()) {
                    if (valve instanceof SessionFlushValve) {
                        RedisStore.this.logger.debug("Setting '{}' as the store for '{}'", this, valve);
                        ((SessionFlushValve) valve).setStore(RedisStore.this);
                    }
                }

                return null;
            }

        });
    }

    @Override
    protected void startInternal() {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                if (RedisStore.this.jedisClient != null) {
                    try {
                        RedisStore.this.jedisClient.close();
                    } catch (IOException e) {
                        RedisStore.this.logger.error("Error closing previous template", e);
                    }
                }
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(RedisStore.this.connectionPoolSize);
                poolConfig.setTestOnBorrow(true);
                poolConfig.setTestOnReturn(true);
                poolConfig.setTestWhileIdle(true);

                if (cluster) {
                    Set<HostAndPort> jedisClusterNodes = new HashSet<>();
                    for (String host : RedisStore.this.host.split(";")) {
                        jedisClusterNodes.add(HostAndPort.parseString(host));
                    }

                    JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes, RedisStore.this.timeout, DEFAULT_SO_TIMEOUT,
                            DEFAULT_MAX_ATTEMPTS, RedisStore.this.password, poolConfig);
                    RedisStore.this.jedisClient = new JedisClusterClient(jedisCluster);
                } else {
                    JedisPool jedisPool = new JedisPool(poolConfig, RedisStore.this.host, RedisStore.this.port,
                            RedisStore.this.timeout, RedisStore.this.password, RedisStore.this.database);
                    RedisStore.this.jedisClient = new JedisNodeClient(jedisPool);
                }

                RedisStore.this.jmxSupport.register(getObjectName(), RedisStore.this);

                return null;
            }

        });
    }

    @Override
    protected void stopInternal() {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() throws IOException {
                if (RedisStore.this.jedisClient != null) {
                    RedisStore.this.logger.info("Closing connection to Redis Server");
                    RedisStore.this.jedisClient.close();
                }

                RedisStore.this.jmxSupport.unregister(getObjectName());

                return null;
            }

        });
    }

    private String getContext() {
        String name = this.manager.getContext().getName();
        return name.startsWith("/") ? name : String.format("/%s", name);
    }

    private String getObjectName() {
        String contextPath = getContext();
        String hostName = this.manager.getContext().getParent().getName();

        return String.format("Catalina:type=Store,context=%s,host=%s,name=%s", contextPath, hostName,
                getClass().getSimpleName());
    }

    private String getUserInfo() {
        String candidate = RedisStore.this.password;
        return candidate == null ? "" : String.format(":%s@", candidate);
    }

    private Session logAndCreateEmptySession(String id, Exception e) {
        RedisStore.this.logger.error("Unable to load session {}. Empty session created.", id, e);
        return RedisStore.this.manager.createSession(id);
    }

    private int parseDatabase(URI uri) {
        return Integer.parseInt(uri.getPath().split("/", 2)[1]);
    }

    private String parsePassword(URI uri) {
        String userInfo = uri.getUserInfo();

        if (userInfo == null) {
            return null;
        }

        return userInfo.split(":", 2)[1];
    }
}
