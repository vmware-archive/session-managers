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

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

final class StandardLifecycleSupport implements LifecycleSupport {

    private final List<LifecycleListener> lifecycleListeners = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(StandardLifecycleSupport.class);

    private final Object monitor = new Object();

    private final Lifecycle source;

    StandardLifecycleSupport(Lifecycle source) {
        this.source = source;
    }

    @Override
    public void add(LifecycleListener lifecycleListener) {
        synchronized (this.monitor) {
            this.lifecycleListeners.add(lifecycleListener);
        }
    }

    @Override
    public LifecycleListener[] getLifecycleListeners() {
        synchronized (this.monitor) {
            return this.lifecycleListeners.toArray(new LifecycleListener[this.lifecycleListeners.size()]);
        }
    }

    @Override
    public void notify(String type, Object data) {
        synchronized (this.monitor) {
            notify(new LifecycleEvent(this.source, type, data));
        }
    }

    @Override
    public void remove(LifecycleListener lifecycleListener) {
        synchronized (this.monitor) {
            this.lifecycleListeners.remove(lifecycleListener);
        }
    }

    private void notify(LifecycleEvent lifecycleEvent) {
        for (LifecycleListener lifecycleListener : this.lifecycleListeners) {
            try {
                lifecycleListener.lifecycleEvent(lifecycleEvent);
            } catch (RuntimeException e) {
                this.logger.warn("Exception encountered while notifying listener of lifecycle event", e);
            }
        }
    }
}
