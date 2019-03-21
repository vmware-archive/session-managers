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

import java.beans.PropertyChangeListener;

/**
 * An API that encapsulates the functionality required to support {@link PropertyChangeListener}s
 */
public interface PropertyChangeSupport {

    /**
     * Add a {@link PropertyChangeListener} to the collection to be notified
     *
     * @param propertyChangeListener the {@link PropertyChangeListener} to add
     */
    void add(PropertyChangeListener propertyChangeListener);

    /**
     * Notify the collection of {@link PropertyChangeListener}s of a change
     *
     * @param propertyName the name of the property that has changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void notify(String propertyName, Object oldValue, Object newValue);

    /**
     * Remove a {@link PropertyChangeListener} from the collection to be notified
     *
     * @param propertyChangeListener the {@link PropertyChangeListener} to remove
     */
    void remove(PropertyChangeListener propertyChangeListener);

}
