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

/**
 * A key used to register Cargo object implementation classes (configurations, deployables,
 * deployers, etc) against containers.
 *
 * @version $Id: HintKey.java 971 2006-03-27 13:37:28Z vmassol $
 */
public class RegistrationKey
{
    /**
     * @see #RegistrationKey(ContainerIdentity, String) 
     */
    private ContainerIdentity containerIdentity;

    /**
     * @see #RegistrationKey(ContainerIdentity, String)
     */
    private String hint;

    /**
     * @param containerIdentity the container to which the Cargo object implementation class will be
     *        associated with.
     * @param hint A general purpose string. This is used to differentiate different Cargo object
     *             implementation classes when they are registered against the same container.
     *             For example for configurations we're using "standalone", "existing" or "runtime".

     */
    public RegistrationKey(ContainerIdentity containerIdentity, String hint)
    {
        this.containerIdentity = containerIdentity;
        this.hint = hint;
    }

    /**
     * @return the container identity
     * @see #RegistrationKey(ContainerIdentity, String)
     */
    public ContainerIdentity getContainerIdentity()
    {
        return this.containerIdentity;
    }

    /**
     * @return the hint associated with the registration of the Cargo object implementation classes
     * @see #RegistrationKey(ContainerIdentity, String) 
     */
    public String getHint()
    {
        return this.hint;
    }

    /**
     * Differentiate two keys. Needed as we're using this class as an index in a Map.
     * {@inheritDoc}
     * @see Object#equals(java.lang.Object)
     */
    public boolean equals(Object registrationKey)
    {
        boolean result = false;

        if ((registrationKey != null) && (registrationKey instanceof RegistrationKey))
        {
            RegistrationKey key = (RegistrationKey) registrationKey;
            if (key.getContainerIdentity().equals(getContainerIdentity())
                && key.getHint().equals(getHint()))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * Allows quick verification to check is two keys are different. Needed as we're using
     * this class as an index in a Map.
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return getContainerIdentity().hashCode() + getHint().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString(String implementationConceptName)
    {
        return "container [" + getContainerIdentity().toString() + "], "
            + implementationConceptName + " type [" + getHint() + "]";
    }
}
