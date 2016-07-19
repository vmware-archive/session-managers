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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class NotifyingLifecycleStateMachineTest {

    private final LifecycleSupport lifecycleSupport = mock(LifecycleSupport.class);

    private final NotifyingLifecycleStateMachine lifecycleStateMachine = new NotifyingLifecycleStateMachine(this
            .lifecycleSupport);

    @Test
    public void begin() {
        assertEquals(NEW, this.lifecycleStateMachine.getLifecycleState());
    }

    @Test
    public void isMeaningfulTransitionOther() throws LifecycleException {
        assertTrue(this.lifecycleStateMachine.isMeaningfulTransition(INITIALIZING));
    }

    @Test
    public void isMeaningfulTransitionStartingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP));

        this.lifecycleStateMachine.transition(STARTING);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP));

        this.lifecycleStateMachine.transition(STARTED);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP));

        this.lifecycleStateMachine.transition(STOPPING_PREP);
        assertTrue(this.lifecycleStateMachine.isMeaningfulTransition(STARTING_PREP));
    }

    @Test
    public void isMeaningfulTransitionStoppingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP));

        this.lifecycleStateMachine.transition(STOPPING);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP));

        this.lifecycleStateMachine.transition(STOPPED);
        assertFalse(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP));

        this.lifecycleStateMachine.transition(DESTROYING);
        assertTrue(this.lifecycleStateMachine.isMeaningfulTransition(STOPPING_PREP));
    }

    @Test(expected = LifecycleException.class)
    public void transitionDestroyedFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        this.lifecycleStateMachine.transition(DESTROYING);
        this.lifecycleStateMachine.transition(DESTROYED);
        assertTransition(NEW);
    }

    @Test
    public void transitionDestroyingDestroyed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        this.lifecycleStateMachine.transition(DESTROYING);
        assertTransition(DESTROYED);
    }

    @Test
    public void transitionDestroyingFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        this.lifecycleStateMachine.transition(DESTROYING);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionDestroyingIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        this.lifecycleStateMachine.transition(DESTROYING);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionFailedDestroying() throws LifecycleException {
        this.lifecycleStateMachine.transition(FAILED);
        assertTransition(DESTROYING);
    }

    @Test(expected = LifecycleException.class)
    public void transitionFailedIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(FAILED);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionFailedStoppingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(FAILED);
        assertTransition(STOPPING_PREP);
    }

    @Test
    public void transitionInitializedDestroying() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        assertTransition(DESTROYING);
    }

    @Test
    public void transitionInitializedFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionInitializedIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionInitializedStartingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        assertTransition(STARTING_PREP);
    }

    @Test
    public void transitionInitializingFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionInitializingIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionInitializingInitialized() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        assertTransition(INITIALIZED);
    }

    @Test
    public void transitionNewDestroying() throws LifecycleException {
        assertTransition(DESTROYING);
    }

    @Test
    public void transitionNewFailed() throws LifecycleException {
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionNewIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionNewInitializing() throws LifecycleException {
        assertTransition(INITIALIZING);
    }

    @Test
    public void transitionNewStartingPrep() throws LifecycleException {
        assertTransition(STARTING_PREP);
    }

    @Test
    public void transitionNewStopped() throws LifecycleException {
        assertTransition(STOPPED);
    }

    @Test
    public void transitionStartedFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStartedIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStartedStoppingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        assertTransition(STOPPING_PREP);
    }

    @Test
    public void transitionStartingFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStartingIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStartingPrepFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStartingPrepIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStartingPrepStarting() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        assertTransition(STARTING);
    }

    @Test
    public void transitionStartingStarted() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        assertTransition(STARTED);
    }

    @Test
    public void transitionStoppedDestroying() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        assertTransition(DESTROYING);
    }

    @Test
    public void transitionStoppedFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStoppedIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStoppedStartingPrep() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(STOPPED);
        assertTransition(STARTING_PREP);
    }

    @Test
    public void transitionStoppingFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStoppingIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStoppingPrepFailed() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        assertTransition(FAILED);
    }

    @Test(expected = LifecycleException.class)
    public void transitionStoppingPrepIllegal() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(NEW);
    }

    @Test
    public void transitionStoppingPrepStopping() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        assertTransition(STOPPING);
    }

    @Test
    public void transitionStoppingStopped() throws LifecycleException {
        this.lifecycleStateMachine.transition(INITIALIZING);
        this.lifecycleStateMachine.transition(INITIALIZED);
        this.lifecycleStateMachine.transition(STARTING_PREP);
        this.lifecycleStateMachine.transition(STARTING);
        this.lifecycleStateMachine.transition(STARTED);
        this.lifecycleStateMachine.transition(STOPPING_PREP);
        this.lifecycleStateMachine.transition(STOPPING);
        assertTransition(STOPPED);
    }


    private void assertTransition(LifecycleState lifecycleState) throws LifecycleException {
        reset(this.lifecycleSupport);

        this.lifecycleStateMachine.transition(lifecycleState);

        assertEquals(lifecycleState, this.lifecycleStateMachine.getLifecycleState());


        String lifecycleEvent = lifecycleState.getLifecycleEvent();
        if (lifecycleEvent != null) {
            verify(this.lifecycleSupport).notify(lifecycleEvent, null);
        } else {
            verifyZeroInteractions(this.lifecycleSupport);
        }
    }
}
