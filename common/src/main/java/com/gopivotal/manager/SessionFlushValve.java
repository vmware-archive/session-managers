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

import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An implementation fo the {@link Valve} interface that flushes any existing sessions before the response is returned.
 */
public final class SessionFlushValve implements Valve {

    private static final String INFO = "SessionFlushValve/1.0";

    private final ReadWriteLock monitor = new ReentrantReadWriteLock();

    private volatile Valve next;

    private volatile Store store;

    @Override
    public void backgroundProcess() {
    }

    @Override
    public void event(Request request, Response response, CometEvent event) {
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public Valve getNext() {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            return this.next;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setNext(Valve valve) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            this.next = valve;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the store used when flushing the session
     *
     * @return the store used when flushing the session
     */
    public Store getStore() {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            return this.store;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the store to use when flushing the session
     *
     * @param store the store to use when flushing the session
     */
    public void setStore(Store store) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            this.store = store;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            try {
                getNext().invoke(request, response);
            } finally {
                Session session = request.getSessionInternal(false);
                if (session != null && session.isValid()) {
                    this.store.save(session);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }
}
