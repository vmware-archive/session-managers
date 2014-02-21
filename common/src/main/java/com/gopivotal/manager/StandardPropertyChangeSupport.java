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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * The standard implementation of the {@link PropertyChangeSupport} interface
 */
public final class StandardPropertyChangeSupport implements PropertyChangeSupport {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Object monitor = new Object();

    private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    private final Object source;

    /**
     * Create a new instance
     *
     * @param source the source that notifications will be sent from
     */
    public StandardPropertyChangeSupport(Object source) {
        this.source = source;
    }

    @Override
    public void add(PropertyChangeListener propertyChangeListener) {
        synchronized (this.monitor) {
            this.propertyChangeListeners.add(propertyChangeListener);
        }
    }

    @Override
    public void notify(String propertyName, Object oldValue, Object newValue) {
        synchronized (this.monitor) {
            if (!isEqual(oldValue, newValue)) {
                notify(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
            }
        }
    }

    @Override
    public void remove(PropertyChangeListener propertyChangeListener) {
        synchronized (this.monitor) {
            this.propertyChangeListeners.remove(propertyChangeListener);
        }
    }

    private boolean isEqual(Object oldValue, Object newValue) {
        return oldValue == null ? newValue == null : oldValue.equals(newValue);
    }

    private void notify(PropertyChangeEvent propertyChangeEvent) {
        for (PropertyChangeListener propertyChangeListener : this.propertyChangeListeners) {
            try {
                propertyChangeListener.propertyChange(propertyChangeEvent);
            } catch (RuntimeException e) {
                this.logger.warning(String.format(
                        "Exception encountered while notifying listener of property change: %s", e.getMessage()));
            }
        }
    }

}
