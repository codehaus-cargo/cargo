/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerType;

/**
 * Unit tests for {@link org.codehaus.cargo.generic.internal.util.RegistrationKey}.
 */
public class RegistrationKeyTest
{
    /**
     * Test equality.
     */
    @Test
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

        Assertions.assertTrue(key1.equals(key2));
        Assertions.assertTrue(key1.hashCode() == key2.hashCode());

        Assertions.assertFalse(key1.equals(key3));
        Assertions.assertFalse(key1.equals(key4));
        Assertions.assertFalse(key1.equals(key5));
        Assertions.assertFalse(key1.equals(key6));

        Assertions.assertTrue(key6.equals(key7));
        Assertions.assertTrue(key6.hashCode() == key7.hashCode());

        Assertions.assertFalse(key6.equals(key8));
        Assertions.assertFalse(key6.equals(key9));
        Assertions.assertFalse(key6.equals(key10));
    }
}
