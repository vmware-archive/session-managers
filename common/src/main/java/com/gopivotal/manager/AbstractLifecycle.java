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

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;

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

/**
 * An implementation of the {@link Lifecycle} interface that encapsulates the specified transition behavior.
 *
 * @see com.gopivotal.manager.StandardLifecycleSupport
 * @see com.gopivotal.manager.StandardLifecycleSupport
 */
public abstract class AbstractLifecycle implements Lifecycle {

    private final LifecycleStateMachine lifecycleStateMachine;

    private final LifecycleSupport lifecycleSupport;

    protected AbstractLifecycle() {
        this.lifecycleSupport = new StandardLifecycleSupport(this);
        this.lifecycleStateMachine = new NotifyingLifecycleStateMachine(this.lifecycleSupport);
    }

    protected AbstractLifecycle(LifecycleStateMachine lifecycleStateMachine, LifecycleSupport lifecycleSupport) {
        this.lifecycleStateMachine = lifecycleStateMachine;
        this.lifecycleSupport = lifecycleSupport;
    }

    @Override
    public final void addLifecycleListener(LifecycleListener lifecycleListener) {
        this.lifecycleSupport.add(lifecycleListener);
    }

    @Override
    public final void destroy() throws LifecycleException {
        try {
            this.lifecycleStateMachine.transition(DESTROYING);
            destroyInternal();
        } catch (RuntimeException e) {
            throw new LifecycleException("Destruction Failed", e);
        } finally {
            this.lifecycleStateMachine.transition(DESTROYED);
        }
    }

    @Override
    public final LifecycleListener[] findLifecycleListeners() {
        return this.lifecycleSupport.getLifecycleListeners();
    }

    @Override
    public final LifecycleState getState() {
        return this.lifecycleStateMachine.getLifecycleState();
    }

    @Override
    public final String getStateName() {
        return getState().name();
    }

    @Override
    public final void init() throws LifecycleException {
        try {
            this.lifecycleStateMachine.transition(INITIALIZING);
            initInternal();
            this.lifecycleStateMachine.transition(INITIALIZED);
        } catch (RuntimeException e) {
            this.lifecycleStateMachine.transition(FAILED);
            throw new LifecycleException("Initialization Failed", e);
        }
    }

    @Override
    public final void removeLifecycleListener(LifecycleListener lifecycleListener) {
        this.lifecycleSupport.remove(lifecycleListener);
    }

    @Override
    public final void start() throws LifecycleException {
        try {
            if (NEW == this.lifecycleStateMachine.getLifecycleState()) {
                init();
            }

            if (!this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP)) {
                return;
            }

            this.lifecycleStateMachine.transition(STARTING_PREP);
            startPrepInternal();
            this.lifecycleStateMachine.transition(STARTING);
            startInternal();
            this.lifecycleStateMachine.transition(STARTED);
        } catch (RuntimeException e) {
            this.lifecycleStateMachine.transition(FAILED);
            throw new LifecycleException("Start Failed", e);
        }
    }

    @Override
    public final void stop() throws LifecycleException {
        try {
            if (NEW == this.lifecycleStateMachine.getLifecycleState()) {
                this.lifecycleStateMachine.transition(STOPPED);
                return;
            }

            if (!this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP)) {
                return;
            }

            this.lifecycleStateMachine.transition(STOPPING_PREP);
            stopPrepInternal();
            this.lifecycleStateMachine.transition(STOPPING);
            stopInternal();
            this.lifecycleStateMachine.transition(STOPPED);
        } catch (RuntimeException e) {
            this.lifecycleStateMachine.transition(FAILED);
            throw new LifecycleException("Stop Failed", e);
        }
    }

    protected void destroyInternal() {
    }

    protected void initInternal() {
    }

    protected void startInternal() {
    }

    protected void startPrepInternal() {
    }

    protected void stopInternal() {
    }

    protected void stopPrepInternal() {
    }

}
