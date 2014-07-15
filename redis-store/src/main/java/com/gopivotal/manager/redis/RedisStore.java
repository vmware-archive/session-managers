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
import com.gopivotal.manager.SessionSerializationUtils;
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
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * An implementation of {@link Store} that persists data to Redis
 */
public final class RedisStore extends AbstractLifecycle implements RedisStoreManagement, Store {

    private static final String INFO = "RedisStore/1.0";

    private static final String SESSIONS_KEY = "sessions";

    private final JmxSupport jmxSupport;

    private final LockTemplate lockTemplate = new LockTemplate();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final PropertyChangeSupport propertyChangeSupport;

    private volatile int connectionPoolSize = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;

    private volatile int database = Protocol.DEFAULT_DATABASE;

    private volatile String host = "localhost";

    private volatile JedisPool jedisPool;

    private volatile JedisTemplate jedisTemplate;

    private volatile Manager manager;

    private volatile String password;

    private volatile int port = Protocol.DEFAULT_PORT;

    private volatile SessionSerializationUtils sessionSerializationUtils;

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

    RedisStore(JedisPool jedisPool, JmxSupport jmxSupport, PropertyChangeSupport propertyChangeSupport,
               SessionSerializationUtils sessionSerializationUtils) {
        this.jedisPool = jedisPool;
        this.jedisTemplate = new JedisTemplate(this.jedisPool);
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
                    RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Void>() {

                        @Override
                        public Void invoke(Jedis jedis) {
                            Set<String> sessions = jedis.smembers(SESSIONS_KEY);
                            String[] sessionsArray = sessions.toArray(new String[sessions.size()]);

                            Transaction t = jedis.multi();
                            t.srem(SESSIONS_KEY, sessionsArray);
                            t.del(sessionsArray);
                            t.exec();

                            return null;
                        }

                    });
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.log(SEVERE, "Unable to clear persisted sessions", e);
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
                String previous = RedisStore.this.host;
                RedisStore.this.host = host;
                RedisStore.this.propertyChangeSupport.notify("host", previous, RedisStore.this.host);
                return null;
            }

        });
    }

    @Override
    public String getInfo() {
        return INFO;
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
                int size;

                try {
                    size = RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Integer>() {

                        @Override
                        public Integer invoke(Jedis jedis) {
                            Transaction t = jedis.multi();
                            Response<Long> count = t.scard(SESSIONS_KEY);
                            t.exec();

                            return count.get().intValue();
                        }

                    });
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.log(SEVERE, "Unable to get the number of persisted sessions", e);
                    size = Integer.MIN_VALUE;
                }

                return size;
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

    /**
     * Sets the connection URI
     *
     * @param uri the connection URI
     */
    public void setUri(final String uri) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
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
                String[] keys;

                try {
                    keys = RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<String[]>() {

                        @Override
                        public String[] invoke(Jedis jedis) {
                            Transaction t = jedis.multi();
                            Response<Set<String>> sessionIds = t.smembers(SESSIONS_KEY);
                            t.exec();

                            return sessionIds.get().toArray(new String[sessionIds.get().size()]);
                        }

                    });
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.log(SEVERE, "Unable to get the keys of persisted sessions", e);
                    keys = new String[0];
                }

                return keys;
            }

        });
    }

    @Override
    public Session load(final String id) {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Session>() {

            @Override
            public Session invoke() {
                Session session;

                try {
                    session = RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Session>() {

                        @Override
                        public Session invoke(Jedis jedis) {
                            try {
                                Transaction t = jedis.multi();
                                Response<byte[]> session = t.get(id.getBytes(Protocol.CHARSET));
                                t.exec();

                                return RedisStore.this.sessionSerializationUtils.deserialize(session.get());
                            } catch (ClassNotFoundException e) {
                                return logAndCreateEmptySession(id, e);
                            } catch (IOException e) {
                                return logAndCreateEmptySession(id, e);
                            }
                        }
                    });
                } catch (JedisConnectionException e) {
                    session = logAndCreateEmptySession(id, e);
                }

                return session;
            }

        });
    }

    @Override
    public void remove(final String id) {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                try {
                    RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Void>() {

                        @Override
                        public Void invoke(Jedis jedis) {
                            Transaction t = jedis.multi();
                            t.srem(SESSIONS_KEY, id);
                            t.del(id);
                            t.exec();

                            return null;
                        }

                    });
                } catch (JedisConnectionException e) {
                    RedisStore.this.logger.log(SEVERE, String.format("Unable to remove session %s", id), e);
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
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

                                           @Override
                                           public Void invoke() {
                                               final String sessionId = session.getId();

                                               try {
                                                   RedisStore.this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Void>() {

                                                                                               @Override
                                                                                               public Void invoke(Jedis jedis) {
                                                                                                   try {
                                                                                                       Transaction t = jedis.multi();
                                                                                                       t.set(session.getId().getBytes(Protocol.CHARSET), RedisStore.this.sessionSerializationUtils
                                                                                                               .serialize(session));
                                                                                                       t.sadd(SESSIONS_KEY, sessionId);
                                                                                                       t.exec();
                                                                                                   } catch (IOException e) {
                                                                                                       RedisStore.this.logger.log(SEVERE, String.format("Unable to save session %s",
                                                                                                               sessionId), e);
                                                                                                   }

                                                                                                   return null;
                                                                                               }

                                                                                           }

                                                   );
                                               } catch (JedisConnectionException e) {
                                                   RedisStore.this.logger.log(SEVERE, String.format("Unable to persist session %s", sessionId), e);
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
                for (Valve valve : RedisStore.this.manager.getContainer().getPipeline().getValves()) {
                    if (valve instanceof SessionFlushValve) {
                        RedisStore.this.logger.fine(String.format("Setting '%s' as the store for '%s'", this, valve));
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
                if (RedisStore.this.jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(RedisStore.this.connectionPoolSize);

                    RedisStore.this.jedisPool = new JedisPool(poolConfig, RedisStore.this.host, RedisStore.this.port,
                            RedisStore.this.timeout, RedisStore.this.password, RedisStore.this.database);
                }


                RedisStore.this.jedisTemplate = new JedisTemplate(RedisStore.this.jedisPool);
                connect();
                RedisStore.this.jmxSupport.register(getObjectName(), RedisStore.this);

                return null;
            }

        });
    }

    @Override
    protected void stopInternal() {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                if (RedisStore.this.jedisPool != null) {
                    RedisStore.this.logger.info("Closing connection to Redis Server");
                    RedisStore.this.jedisPool.destroy();
                }

                RedisStore.this.jmxSupport.unregister(getObjectName());

                return null;
            }

        });
    }

    private void connect() {
        this.logger.info(String.format("Connecting to Redis Server at redis://%s:%d/%d", this.host, this.port,
                this.database));

        this.jedisTemplate.withJedis(new JedisTemplate.JedisOperation<Void>() {

            @Override
            public Void invoke(Jedis jedis) {
                RedisStore.this.logger.info("Connection to Redis Server successful");
                return null;
            }

        });
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

    private String getUserInfo() {
        String candidate = RedisStore.this.password;
        return candidate == null ? "" : String.format(":%s@", candidate);
    }

    private Session logAndCreateEmptySession(String id, Exception e) {
        RedisStore.this.logger.log(SEVERE, String.format("Unable to load session %s. Empty session created.", id), e);
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
