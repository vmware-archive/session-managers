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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Utility methods that encapsulate {@link ReadWriteLock} idioms into closure-like methods.
 */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class LockTemplate {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final ReadWriteLock monitor;

    /**
     * Creates a new instance
     */
    public LockTemplate() {
        this(new ReentrantReadWriteLock());
    }

    LockTemplate(ReadWriteLock monitor) {
        this.monitor = monitor;
    }

    /**
     * Execute an operation that returns a value with read locking
     *
     * @param operation the operation to execute
     * @param <T>       the type of the return value
     * @return the return value
     */
    public <T> T withReadLock(ReturningOperation<T> operation) {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            return operation.invoke();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute an operation that does not return a value with read locking
     *
     * @param operation the operation to execute
     */
    public void withReadLock(Operation operation) {
        Lock lock = this.monitor.readLock();
        lock.lock();

        try {
            operation.invoke();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute an operation that returns a value with write locking
     *
     * @param operation the operation to execute
     * @param <T>       the type of the return value
     * @return the return value
     */
    public <T> T withWriteLock(ReturningOperation<T> operation) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            return operation.invoke();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute an operation that does not return a value with write locking
     *
     * @param operation the operation to execute
     */
    public void withWriteLock(Operation operation) {
        Lock lock = this.monitor.writeLock();
        lock.lock();

        try {
            operation.invoke();
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * A closure-like interface for operations that do not return a value
     */
    public interface Operation {

        /**
         * Invoke the operation
         *
         * @throws Exception
         */
        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
        void invoke() throws Exception;
    }

    /**
     * A closure-like interface for operations that return a value
     *
     * @param <T> the return type of the operation
     */
    public interface ReturningOperation<T> {

        /**
         * Invoke the operation
         *
         * @return the return value of the operation
         * @throws Exception
         */
        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
        T invoke() throws Exception;
    }

}
