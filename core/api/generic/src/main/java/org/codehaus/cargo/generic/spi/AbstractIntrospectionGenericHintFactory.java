/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.generic.spi;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;

/**
 * Extension to {@link AbstractGenericHintFactory} that registers and creates instances from
 * implementation classes specified as String, using introspection. If the implementation fails
 * to load the registration fails silently. This is to cover the user case where a specific 
 * container's implementation jar may not be present in the classpath if it's not used by the user.
 * This allows users to only put the container implementation jars the require in the classpath.
 *
 * @version $Id$
 */
public abstract class AbstractIntrospectionGenericHintFactory extends AbstractGenericHintFactory
{
    /**
     * Contains all the default mappings that were not properly registered because the 
     * implementation classes could not be found in the classpath (it probably means the user has
     * forgotten to add the container implementation jar to its classpath). We record them in order 
     * to throw a nice error message if the user tries to use any of them.
     */
    private Map rejectedMappings = new HashMap();

    /**
     * Allow registering container objects using introspection so that at build time and runtime 
     * the user can handpick the container implementation jars that he want to use. If we were not 
     * using introspection the user would have to have all container implementation jars in the 
     * classpath when using this generic API.
     * 
     * @param key the key under which to register the class name
     * @param objectClassName the object to register
     */
    protected void registerImplementation(RegistrationKey key, String objectClassName)
    {
        try
        {
            Class objectClass = this.getClass().getClassLoader().loadClass(objectClassName);
            registerImplementation(key, objectClass);
        }
        catch (Exception e)
        {
            // We record the rejected mapping to provide a nice error message if the user tries
            // to use this mapping later on.
            this.rejectedMappings.put(key, objectClassName);

            // We do not rethrow an exception because we want to allow registering only container
            // implementation classes that are in the classloader.
            getLogger().warn("Not registering class [" + objectClassName
                + "] as there was an error: [" + e.getMessage() + "]", this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractGenericHintFactory#createImplementation
     */
    @Override
    protected Object createImplementation(RegistrationKey key, GenericParameters parameters,
        String implementationConceptName)
    {
        Object object;
        try
        {
            object = super.createImplementation(key, parameters, implementationConceptName);
        }
        catch (ContainerException e)
        {
            if (this.rejectedMappings.containsKey(key))
            {
                String message = "Failed to create a " + implementationConceptName
                    + " for parameters (" + key.toString(implementationConceptName) + ")."
                    + "The container has not been properly registered for that "
                    + implementationConceptName + " type and that's probably because that "
                    + "container implementation class could not been loaded. Are you sure you "
                    + "have added that container's implementation jar to the classpath?";

                throw new ContainerException(message , e);
            }

            throw e;
        }

        return object;
    }
}
