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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;

import java.util.Arrays;

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

final class NotifyingLifecycleStateMachine implements LifecycleStateMachine {

    private final LifecycleSupport lifecycleSupport;

    private final Object monitor = new Object();

    private volatile LifecycleState lifecycleState = NEW;

    NotifyingLifecycleStateMachine(LifecycleSupport lifecycleSupport) {
        this.lifecycleSupport = lifecycleSupport;
    }

    @Override
    public LifecycleState getLifecycleState() {
        synchronized (this.monitor) {
            return this.lifecycleState;
        }
    }

    @Override
    public boolean isMeaningfulTransition(LifecycleState lifecycleState) {
        synchronized (this.monitor) {
            if (STARTING_PREP == lifecycleState) {
                return !matches(this.lifecycleState, STARTING_PREP, STARTING, STARTED);
            } else if (STOPPING_PREP == lifecycleState) {
                return !matches(this.lifecycleState, STOPPING_PREP, STOPPING, STOPPED);
            }

            return true;
        }
    }

    // CHECKSTYLE:OFF
    @Override
    public void transition(LifecycleState lifecycleState) throws LifecycleException {
        synchronized (this.monitor) {
            if (NEW == this.lifecycleState) {
                transition(lifecycleState, INITIALIZING, STARTING_PREP, STOPPED, DESTROYING);
            } else if (INITIALIZING == this.lifecycleState) {
                transition(lifecycleState, INITIALIZED);
            } else if (INITIALIZED == this.lifecycleState) {
                transition(lifecycleState, STARTING_PREP, DESTROYING);
            } else if (STARTING_PREP == this.lifecycleState) {
                transition(lifecycleState, STARTING);
            } else if (STARTING == this.lifecycleState) {
                transition(lifecycleState, STARTED);
            } else if (STARTED == this.lifecycleState) {
                transition(lifecycleState, STOPPING_PREP);
            } else if (STOPPING_PREP == this.lifecycleState) {
                transition(lifecycleState, STOPPING);
            } else if (STOPPING == this.lifecycleState) {
                transition(lifecycleState, STOPPED);
            } else if (STOPPED == this.lifecycleState) {
                transition(lifecycleState, STARTING_PREP, DESTROYING);
            } else if (DESTROYING == this.lifecycleState) {
                transition(lifecycleState, DESTROYED);
            } else if (DESTROYED == this.lifecycleState) {
                transition(lifecycleState, new LifecycleState[0]);
            } else if (FAILED == this.lifecycleState) {
                transition(lifecycleState, STOPPING_PREP, DESTROYING);
            }
        }
    }
    // CHECKSTYLE:ON

    private boolean matches(LifecycleState lifecycleState, LifecycleState... candidates) {
        for (LifecycleState candidate : candidates) {
            if (candidate == lifecycleState) {
                return true;
            }
        }

        return false;
    }

    private void transition(LifecycleState lifecycleState, LifecycleState... candidates) throws LifecycleException {
        LifecycleState[] augmentedCandidates = Arrays.copyOf(candidates, candidates.length + 1);
        augmentedCandidates[candidates.length] = FAILED;

        if (matches(lifecycleState, augmentedCandidates)) {
            this.lifecycleState = lifecycleState;

            String lifecycleEvent = lifecycleState.getLifecycleEvent();
            if (lifecycleEvent != null) {
                this.lifecycleSupport.notify(lifecycleEvent, null);
            }
        } else {
            throw new LifecycleException(String.format("Illegal transition from %s to %s attempted",
                    this.lifecycleState, lifecycleState));
        }
    }

}
