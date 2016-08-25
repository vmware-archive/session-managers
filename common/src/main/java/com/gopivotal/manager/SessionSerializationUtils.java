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

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Loader;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utilities for serializing and deserializing {@link Session}s
 */
public final class SessionSerializationUtils {

    private final Manager manager;

    /**
     * Creates a new instance
     *
     * @param manager the manager to use when recreating sessions
     */
    public SessionSerializationUtils(Manager manager) {
        this.manager = manager;
    }

    /**
     * Deserialize a {@link Session}
     *
     * @param session a {@code byte[]} representing the serialized {@link Session}
     * @return the deserialized {@link Session} or {@code null} if the session data is {@code null}
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Session deserialize(byte[] session) throws ClassNotFoundException, IOException {
        if (session == null) {
            return null;
        }

        ByteArrayInputStream bytes = null;
        ObjectInputStream in = null;

        ClassLoader oldThreadContextCL = null;
        try {
            bytes = new ByteArrayInputStream(session);
            Loader loader = null;
            ClassLoader classLoader = null;
            oldThreadContextCL = Thread.currentThread().getContextClassLoader();
            Container container = manager.getContainer();
            if (container != null) {
              loader = container.getLoader();
            }
            if (loader != null) {
              classLoader = loader.getClassLoader();
            }
            if (classLoader != null) {
              Thread.currentThread().setContextClassLoader(classLoader);
              in = new CustomObjectInputStream(bytes, classLoader);
            } else {
              in = new ObjectInputStream(bytes);
            }

            StandardSession standardSession = (StandardSession) this.manager.createEmptySession();
            standardSession.readObjectData(in);

            return standardSession;
        } finally {
            closeQuietly(in, bytes);
            if (oldThreadContextCL != null) {
              Thread.currentThread().setContextClassLoader(oldThreadContextCL);
            }
        }
    }

    /**
     * Serialize a {@link Session}
     *
     * @param session the {@link Session} to serialize
     * @return a {@code byte[]} representing the serialized {@link Session}
     * @throws IOException
     */
    public byte[] serialize(Session session) throws IOException {
        ByteArrayOutputStream bytes = null;
        ObjectOutputStream out = null;

        try {
            bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);

            StandardSession standardSession = (StandardSession) session;
            standardSession.writeObjectData(out);

            out.flush();
            bytes.flush();

            return bytes.toByteArray();
        } finally {
            closeQuietly(out, bytes);
        }

    }

    private void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

}
