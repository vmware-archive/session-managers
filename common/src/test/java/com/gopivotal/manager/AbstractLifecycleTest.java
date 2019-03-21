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

package com.gopivotal.manager;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.junit.Test;

import static org.apache.catalina.LifecycleState.DESTROYED;
import static org.apache.catalina.LifecycleState.DESTROYING;
import static org.apache.catalina.LifecycleState.FAILED;
import static org.apache.catalina.LifecycleState.INITIALIZED;
import static org.apache.catalina.LifecycleState.INITIALIZING;
import static org.apache.catalina.LifecycleState.NEW;
import static org.apache.catalina.LifecycleState.STARTED;
import static org.apache.catalina.LifecycleState.STARTING;
import static org.apache.catalina.LifecycleState.STARTING_PREP;
import static org.apache.catalina.LifecycleState.STOPPED;
import static org.apache.catalina.LifecycleState.STOPPING;
import static org.apache.catalina.LifecycleState.STOPPING_PREP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class AbstractLifecycleTest {

    private final LifecycleListener lifecycleListener = mock(LifecycleListener.class);

    private final LifecycleStateMachine lifecycleStateMachine = mock(LifecycleStateMachine.class);

    private final LifecycleSupport lifecycleSupport = mock(LifecycleSupport.class);

    private final StubLifecycle lifecycle = spy(new StubLifecycle(this.lifecycleStateMachine, this.lifecycleSupport));

    @Test
    public void defaultConstructor() {
        new StubLifecycle();
    }

    @Test
    public void destroy() throws LifecycleException {
        this.lifecycle.destroy();

        verify(this.lifecycleStateMachine).transition(DESTROYING);
        verify(this.lifecycleStateMachine).transition(DESTROYED);
    }

    @Test
    public void destroyException() throws LifecycleException {
        doThrow(new RuntimeException()).when(this.lifecycle).destroyInternal();

        try {
            this.lifecycle.destroy();
            fail();
        } catch (LifecycleException e) {
            verify(this.lifecycleStateMachine).transition(DESTROYED);
        }
    }

    @Test
    public void init() throws LifecycleException {
        this.lifecycle.init();

        verify(this.lifecycleStateMachine).transition(INITIALIZING);
        verify(this.lifecycleStateMachine).transition(INITIALIZED);
    }

    @Test
    public void initException() throws LifecycleException {
        doThrow(new RuntimeException()).when(this.lifecycle).initInternal();

        try {
            this.lifecycle.init();
            fail();
        } catch (LifecycleException e) {
            verify(this.lifecycleStateMachine).transition(FAILED);
        }
    }

    @Test
    public void lifecycleListener() {
        this.lifecycle.addLifecycleListener(this.lifecycleListener);
        verify(this.lifecycleSupport).add(this.lifecycleListener);

        this.lifecycle.findLifecycleListeners();
        verify(this.lifecycleSupport).getLifecycleListeners();

        this.lifecycle.removeLifecycleListener(this.lifecycleListener);
        verify(this.lifecycleSupport).remove(this.lifecycleListener);
    }

    @Test
    public void start() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP)).thenReturn(true);

        this.lifecycle.start();

        verify(this.lifecycleStateMachine).transition(STARTING_PREP);
        verify(this.lifecycleStateMachine).transition(STARTING);
        verify(this.lifecycleStateMachine).transition(STARTED);
    }

    @Test
    public void startAlreadyStarted() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP)).thenReturn(false);

        this.lifecycle.start();

        verify(this.lifecycleStateMachine, times(0)).transition(any(LifecycleState.class));
    }

    @Test
    public void startException() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP)).thenReturn(true);
        doThrow(new RuntimeException()).when(this.lifecycle).startInternal();

        try {
            this.lifecycle.start();
            fail();
        } catch (LifecycleException e) {
            verify(this.lifecycleStateMachine).transition(FAILED);
        }
    }

    @Test
    public void startFromNewCausesInit() throws LifecycleException {
        when(this.lifecycleStateMachine.getLifecycleState()).thenReturn(NEW);
        when(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP)).thenReturn(true);

        this.lifecycle.start();

        verify(this.lifecycleStateMachine).transition(INITIALIZING);
        verify(this.lifecycleStateMachine).transition(INITIALIZED);
        verify(this.lifecycleStateMachine).transition(STARTING_PREP);
        verify(this.lifecycleStateMachine).transition(STARTING);
        verify(this.lifecycleStateMachine).transition(STARTED);
    }

    @Test
    public void state() {
        when(this.lifecycleStateMachine.getLifecycleState()).thenReturn(NEW);

        assertEquals(NEW, this.lifecycle.getState());
    }

    @Test
    public void stateName() {
        when(this.lifecycleStateMachine.getLifecycleState()).thenReturn(NEW);

        assertEquals("NEW", this.lifecycle.getStateName());
    }

    @Test
    public void stop() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP)).thenReturn(true);

        this.lifecycle.stop();

        verify(this.lifecycleStateMachine).transition(STOPPING_PREP);
        verify(this.lifecycleStateMachine).transition(STOPPING);
        verify(this.lifecycleStateMachine).transition(STOPPED);
    }

    @Test
    public void stopAlreadyStopped() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP)).thenReturn(false);

        this.lifecycle.stop();

        verify(this.lifecycleStateMachine, times(0)).transition(any(LifecycleState.class));
    }

    @Test
    public void stopException() throws LifecycleException {
        when(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP)).thenReturn(true);
        doThrow(new RuntimeException()).when(this.lifecycle).stopInternal();

        try {
            this.lifecycle.stop();
            fail();
        } catch (LifecycleException e) {
            verify(this.lifecycleStateMachine).transition(FAILED);
        }
    }

    @Test
    public void stopFromNewCausesStopped() throws LifecycleException {
        when(this.lifecycleStateMachine.getLifecycleState()).thenReturn(NEW);

        this.lifecycle.stop();

        verify(this.lifecycleStateMachine, times(0)).transition(STOPPING_PREP);
        verify(this.lifecycleStateMachine).transition(STOPPED);
    }

    private static class StubLifecycle extends AbstractLifecycle {

        private StubLifecycle() {
            super();
        }

        private StubLifecycle(LifecycleStateMachine lifecycleStateMachine, LifecycleSupport lifecycleSupport) {
            super(lifecycleStateMachine, lifecycleSupport);
        }
    }

}
