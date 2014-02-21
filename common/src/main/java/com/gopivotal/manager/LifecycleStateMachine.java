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

interface LifecycleStateMachine {

    /**
     * Returns the current {@link LifecycleState}
     *
     * @return the current {@link LifecycleState}
     */
    LifecycleState getLifecycleState();

    /**
     * Whether a transition from the current {@link LifecycleState} to a new {@link LifecycleState} is meaningful.
     * That is, should it have any effect.
     *
     * @param lifecycleState the new {@link LifecycleState}
     * @return {@code true} if the transition is meaningful, {@code false} otherwise
     */
    boolean isMeaningfulTransition(LifecycleState lifecycleState);

    /**
     * Transition from the current {@link LifecycleState} to a new {@link LifecycleState}.
     *
     * @param lifecycleState the new {@link LifecycleState}
     * @throws LifecycleException if the transition is not legal
     */
    void transition(LifecycleState lifecycleState) throws LifecycleException;

}
