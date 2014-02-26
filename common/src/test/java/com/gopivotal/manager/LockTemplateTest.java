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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class LockTemplateTest {

    @SuppressWarnings("unchecked")
    private final LockTemplate.LockedOperation<String> operation = mock(LockTemplate.LockedOperation.class);

    private final Lock readLock = mock(Lock.class);

    private final ReadWriteLock readWriteLock = mock(ReadWriteLock.class);

    private final LockTemplate lockTemplate = new LockTemplate(this.readWriteLock);

    private final Lock writeLock = mock(Lock.class);

    @Test
    public void constructor() {
        new LockTemplate();
    }

    @Before
    public void locks() {
        when(this.readWriteLock.readLock()).thenReturn(this.readLock);
        when(this.readWriteLock.writeLock()).thenReturn(this.writeLock);
    }

    @Test
    public void withReadLock() throws Exception {
        when(this.operation.invoke()).thenReturn("test-value");

        String actual = this.lockTemplate.withReadLock(this.operation);

        assertEquals("test-value", actual);
        verify(this.readLock).lock();
        verify(this.readLock).unlock();
    }

    @Test
    public void withReadLockException() throws Exception {
        when(this.operation.invoke()).thenThrow(new Exception());

        try {
            this.lockTemplate.withReadLock(this.operation);
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof Exception);
        }

        verify(this.readLock).lock();
        verify(this.readLock).unlock();
    }

    @Test
    public void withWriteLock() throws Exception {
        when(this.operation.invoke()).thenReturn("test-value");

        String actual = this.lockTemplate.withWriteLock(this.operation);

        assertEquals("test-value", actual);
        verify(this.writeLock).lock();
        verify(this.writeLock).unlock();
    }

    @Test
    public void withWriteLockException() throws Exception {
        when(this.operation.invoke()).thenThrow(new Exception());

        try {
            this.lockTemplate.withWriteLock(this.operation);
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof Exception);
        }

        verify(this.writeLock).lock();
        verify(this.writeLock).unlock();
    }
}
