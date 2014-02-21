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
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public final class StandardLifecycleSupportTest {

    private final Object data = new Object();

    private final LifecycleListener lifecycleListener1 = mock(LifecycleListener.class);

    private final LifecycleListener lifecycleListener2 = mock(LifecycleListener.class);

    private final Lifecycle source = mock(Lifecycle.class);

    private final StandardLifecycleSupport lifecycleSupport = new StandardLifecycleSupport(this.source);

    @Test
    public void findLifecycleListeners() {
        this.lifecycleSupport.add(this.lifecycleListener1);
        this.lifecycleSupport.add(this.lifecycleListener2);

        assertArrayEquals(new LifecycleListener[]{this.lifecycleListener1, this.lifecycleListener2},
                this.lifecycleSupport.getLifecycleListeners());
    }

    @Test
    public void listenerThrowException() {
        doThrow(new RuntimeException()).when(this.lifecycleListener1).lifecycleEvent(any(LifecycleEvent.class));

        this.lifecycleSupport.add(this.lifecycleListener1);
        this.lifecycleSupport.add(this.lifecycleListener2);

        this.lifecycleSupport.notify("test-type", this.data);

        verify(this.lifecycleListener2).lifecycleEvent(any(LifecycleEvent.class));
    }

    @Test
    public void noListeners() {
        this.lifecycleSupport.notify("test-type", this.data);

        verifyZeroInteractions(this.lifecycleListener1, this.lifecycleListener2);
    }

    @Test
    public void withListeners() {
        this.lifecycleSupport.add(this.lifecycleListener1);
        this.lifecycleSupport.add(this.lifecycleListener2);

        this.lifecycleSupport.notify("test-type", this.data);

        ArgumentCaptor<LifecycleEvent> lifecycleEvents = ArgumentCaptor.forClass(LifecycleEvent.class);
        verify(this.lifecycleListener1).lifecycleEvent(lifecycleEvents.capture());
        verify(this.lifecycleListener2).lifecycleEvent(lifecycleEvents.capture());

        for (LifecycleEvent lifecycleEvent : lifecycleEvents.getAllValues()) {
            assertEquals(this.source, lifecycleEvent.getLifecycle());
            assertEquals("test-type", lifecycleEvent.getType());
            assertEquals(this.data, lifecycleEvent.getData());
        }

        this.lifecycleSupport.remove(this.lifecycleListener1);
        this.lifecycleSupport.remove(this.lifecycleListener2);

        this.lifecycleSupport.notify("test-type", this.data);

        verifyNoMoreInteractions(this.lifecycleListener1, this.lifecycleListener2);
    }
}
