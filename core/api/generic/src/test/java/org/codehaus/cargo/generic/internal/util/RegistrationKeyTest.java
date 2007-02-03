/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.generic.internal.util;

import junit.framework.TestCase;
import org.codehaus.cargo.container.ContainerType;

/**
 * Unit tests for {@link org.codehaus.cargo.generic.internal.util.RegistrationKey}.
 *
 * @version $Id: $
 */
public class RegistrationKeyTest extends TestCase
{
    public void testEquality()
    {
        RegistrationKey key1 = new RegistrationKey(new SimpleContainerIdentity("container"),
            "hint");
        RegistrationKey key2 = new RegistrationKey(new SimpleContainerIdentity("container"),
            "hint");
        RegistrationKey key3 = new RegistrationKey(new SimpleContainerIdentity("container"),
            "otherHint");
        RegistrationKey key4 = new RegistrationKey(new SimpleContainerIdentity("otherContainer"),
            "hint");
        RegistrationKey key5 = new RegistrationKey(new SimpleContainerIdentity("otherContainer"),
            "otherHint");
        RegistrationKey key6 = new RegistrationKey(new FullContainerIdentity("container",
            ContainerType.INSTALLED), "hint");
        RegistrationKey key7 = new RegistrationKey(new FullContainerIdentity("container",
            ContainerType.INSTALLED), "hint");
        RegistrationKey key8 = new RegistrationKey(new FullContainerIdentity("container",
            ContainerType.EMBEDDED), "hint");
        RegistrationKey key9 = new RegistrationKey(new FullContainerIdentity("otherContainer",
            ContainerType.INSTALLED), "hint");
        RegistrationKey key10 = new RegistrationKey(new FullContainerIdentity("container",
            ContainerType.INSTALLED), "otherHint");

        assertTrue(key1.equals(key2));
        assertTrue(key1.hashCode() == key2.hashCode());
        
        assertFalse(key1.equals(key3));
        assertFalse(key1.equals(key4));
        assertFalse(key1.equals(key5));
        assertFalse(key1.equals(key6));

        assertTrue(key6.equals(key7));
        assertTrue(key6.hashCode() == key7.hashCode());

        assertFalse(key6.equals(key8));
        assertFalse(key6.equals(key9));
        assertFalse(key6.equals(key10));
    }
}
