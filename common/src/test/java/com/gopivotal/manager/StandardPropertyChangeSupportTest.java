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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class StandardPropertyChangeSupportTest {

    private final PropertyChangeListener propertyChangeListener1 = mock(PropertyChangeListener.class);

    private final PropertyChangeListener propertyChangeListener2 = mock(PropertyChangeListener.class);

    private final Object source = new Object();

    private final StandardPropertyChangeSupport propertyChangeSupport = new StandardPropertyChangeSupport(this.source);

    @Test
    public void listenerThrowsException() {
        doThrow(new RuntimeException()).when(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));

        this.propertyChangeSupport.add(this.propertyChangeListener1);
        this.propertyChangeSupport.add(this.propertyChangeListener2);

        this.propertyChangeSupport.notify("test-property", true, false);

        verify(this.propertyChangeListener2).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void noChangeNoNotification() {
        this.propertyChangeSupport.add(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", true, true);

        verifyZeroInteractions(this.propertyChangeListener1);
    }

    @Test
    public void noListeners() {
        this.propertyChangeSupport.notify("test-property", true, false);

        verifyZeroInteractions(this.propertyChangeListener1, this.propertyChangeListener2);
    }

    @Test
    public void nullNotifications() {
        this.propertyChangeSupport.add(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", null, null);
        verifyZeroInteractions(this.propertyChangeListener1);
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", null, true);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", null, false);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", true, null);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", true, true);
        verifyZeroInteractions(this.propertyChangeListener1);
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", true, false);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", false, null);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", false, true);
        verify(this.propertyChangeListener1).propertyChange(any(PropertyChangeEvent.class));
        reset(this.propertyChangeListener1);

        this.propertyChangeSupport.notify("test-property", false, false);
        verifyZeroInteractions(this.propertyChangeListener1);
        reset(this.propertyChangeListener1);
    }

    @Test
    public void withListeners() {
        this.propertyChangeSupport.add(this.propertyChangeListener1);
        this.propertyChangeSupport.add(this.propertyChangeListener2);

        this.propertyChangeSupport.notify("test-property", true, false);

        ArgumentCaptor<PropertyChangeEvent> propertyChangeEvents = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(this.propertyChangeListener1).propertyChange(propertyChangeEvents.capture());
        verify(this.propertyChangeListener2).propertyChange(propertyChangeEvents.capture());

        for (PropertyChangeEvent propertyChangeEvent : propertyChangeEvents.getAllValues()) {
            assertEquals(this.source, propertyChangeEvent.getSource());
            assertEquals("test-property", propertyChangeEvent.getPropertyName());
            assertEquals(true, propertyChangeEvent.getOldValue());
            assertEquals(false, propertyChangeEvent.getNewValue());
        }

        this.propertyChangeSupport.remove(this.propertyChangeListener1);
        this.propertyChangeSupport.remove(this.propertyChangeListener2);

        this.propertyChangeSupport.notify("test-property", true, false);

        verifyNoMoreInteractions(this.propertyChangeListener1, this.propertyChangeListener2);
    }
}
