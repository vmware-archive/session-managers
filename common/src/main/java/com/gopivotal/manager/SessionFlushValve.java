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

package com.gopivotal.manager;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * An implementation for the {@link Valve} interface that flushes any existing sessions before the response is returned.
 */
public final class SessionFlushValve extends AbstractLifecycle implements Contained, SessionFlushValveManagement,
        Valve {

    private final JmxSupport jmxSupport;

    private final LockTemplate lockTemplate = new LockTemplate();

    private volatile Container container;

    private volatile Valve next;

    private volatile Store store;

    /**
     * Creates a new instance
     */
    public SessionFlushValve() {
        this(new StandardJmxSupport());
    }

    SessionFlushValve(JmxSupport jmxSupport) {
        this.jmxSupport = jmxSupport;
    }

    @Override
    public void backgroundProcess() {
    }

    @Override
    public Container getContainer() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Container>() {

            @Override
            public Container invoke() {
                return SessionFlushValve.this.container;
            }

        });
    }

    @Override
    public void setContainer(final Container container) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                SessionFlushValve.this.container = container;
                return null;
            }

        });
    }

    @Override
    public Valve getNext() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Valve>() {

            @Override
            public Valve invoke() {
                return SessionFlushValve.this.next;
            }

        });
    }

    @Override
    public void setNext(final Valve valve) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                SessionFlushValve.this.next = valve;
                return null;
            }

        });
    }

    /**
     * Returns the store used when flushing the session
     *
     * @return the store used when flushing the session
     */
    public Store getStore() {
        return this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Store>() {

            @Override
            public Store invoke() {
                return SessionFlushValve.this.store;
            }

        });
    }

    /**
     * Sets the store to use when flushing the session
     *
     * @param store the store to use when flushing the session
     */
    public void setStore(final Store store) {
        this.lockTemplate.withWriteLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                SessionFlushValve.this.store = store;
                return null;
            }

        });
    }

    @Override
    public void invoke(final Request request, final Response response) {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() throws IOException, ServletException {
                try {
                    SessionFlushValve.this.next.invoke(request, response);
                } finally {
                    Session session = request.getSessionInternal(false);
                    if (session != null && session.isValid()) {
                        SessionFlushValve.this.store.save(session);
                    }
                }

                return null;
            }

        });

    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    protected void startInternal() {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                SessionFlushValve.this.jmxSupport.register(getObjectName(), SessionFlushValve.this);
                return null;
            }

        });
    }

    @Override
    protected void stopInternal() {
        this.lockTemplate.withReadLock(new LockTemplate.LockedOperation<Void>() {

            @Override
            public Void invoke() {
                SessionFlushValve.this.jmxSupport.unregister(getObjectName());
                return null;
            }

        });
    }

    private String getContext() {
        String name = this.container.getName();
        return name.startsWith("/") ? name : String.format("/%s", name);
    }

    private String getObjectName() {
        String context = getContext();
        String host = this.container.getParent().getName();

        return String.format("Catalina:type=Valve,context=%s,host=%s,name=%s", context, host,
                getClass().getSimpleName());
    }

}
