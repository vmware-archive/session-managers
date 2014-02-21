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

import org.apache.catalina.LifecycleListener;

interface LifecycleSupport {

    /**
     * Add a {@link LifecycleListener} to the collection to be notified
     *
     * @param lifecycleListener the {@link LifecycleListener} to add
     */
    void add(LifecycleListener lifecycleListener);

    /**
     * Returns the collection of {@link LifecycleListener}s
     *
     * @return the collection of {@link LifecycleListener}s
     */
    LifecycleListener[] getLifecycleListeners();

    /**
     * Notify the collection of {@link LifecycleListener}s of an event
     *
     * @param type the type of event
     * @param data the data of the event
     */
    void notify(String type, Object data);

    /**
     * Remove a {@link LifecycleListener} from the collection to be notified
     *
     * @param lifecycleListener the {@link LifecycleListener} to remove
     */
    void remove(LifecycleListener lifecycleListener);

}
