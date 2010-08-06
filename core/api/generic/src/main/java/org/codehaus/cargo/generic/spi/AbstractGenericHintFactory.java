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

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.ContainerIdentity;
import org.codehaus.cargo.util.log.LoggedObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Factory implementation that registers implementation classes under a given key of type
 * {@link org.codehaus.cargo.generic.internal.util.RegistrationKey}. This abstract factory class
 * is extended by all the different Cargo factories.
 *
 * @version $Id$
 */
public abstract class AbstractGenericHintFactory extends LoggedObject
{
    /**
     * List of name mappings for implementation classes. The keys are instances of
     * {@link org.codehaus.cargo.generic.internal.util.RegistrationKey}.
     */
    private Map mappings;

    /**
     * Generic class to be extended by implementors of {@link AbstractGenericHintFactory} in
     * order to provide possible additional parameters.
     */
    public interface GenericParameters
    {
        // Extension classes should provide custom fields here.
    }
    
    /**
     * Place to register default configurations.
     */
    protected AbstractGenericHintFactory()
    {
        this.mappings = new HashMap();
    }

    /**
     * @param key the key associated with the implementation class
     * @return true if the mapping is already registered or false otherwise
     */
    protected boolean hasMapping(RegistrationKey key)
    {
        return getMappings().containsKey(key);
    }

    /**
     * @param key the key associated with the implementation class to return
     * @return the implementation class
     */
    protected Class getMapping(RegistrationKey key)
    {
        return (Class) getMappings().get(key);
    }

    /**
     * @return the mappings indexed using a {@see RegistrationKey}.
     */
    protected Map getMappings()
    {
        return this.mappings;
    }

    /**
     * Register an implementation class for a given key.
     * 
     * @param key the key under which to register the implementation class
     * @param implementationClass the implementation class to register
     */
    protected void registerImplementation(RegistrationKey key, Class implementationClass)
    {
        getMappings().put(key, implementationClass);
    }

    /**
     * Generic method to create an implementation based on the registered implementation classes.
     * 
     * @param key the key under which the implementation class is registered
     * @param parameters the additional parameters necessary to create the constructor object
     * @param implementationConceptName the name of what the implementation class is representing.
     *        This is used in exception text messages to provide message customization. For
     *        example "container", "configuration", "deployable', etc.
     * @return the created instance
     */
    protected Object createImplementation(RegistrationKey key, GenericParameters parameters,
        String implementationConceptName)
    {
        if (!getMappings().containsKey(key))
        {
            String message = "Cannot create " + implementationConceptName
                +  ". There's no registered " + implementationConceptName + " for the parameters "
                + "(" + key.toString(implementationConceptName) + "). ";

            Iterator hints = getHints(key.getContainerIdentity()).iterator();
            if (hints.hasNext())
            {
                message =
                    message + "Valid types for this " + implementationConceptName + " are: \n";
                while (hints.hasNext())
                {
                    String hint = (String) hints.next();
                    message = message + "  - " + hint;
                    if (hints.hasNext())
                    {
                        message = message + "\n";
                    }
                }
            }
            else
            {
                message = message + "Actually there are no valid types registered for this "
                    + implementationConceptName + ". Maybe you've made a mistake spelling it?";
            }

            throw new ContainerException(message);
        }

        Class implementationClass = (Class) getMappings().get(key);

        Object implementation;
        try
        {
            Constructor constructor = getConstructor(implementationClass, key.getHint(),
                parameters);
            implementation = createInstance(constructor, key.getHint(), parameters);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create " + implementationConceptName
                + " with implementation " + implementationClass + " for the parameters ("
                + key.toString(implementationConceptName) + ").", e);
        }

        return implementation;
    }

    /**
     * Create a constructor.
     * 
     * @param implementationClass implementation class for which to create the constructor
     * @param hint the hint to differentiate this implementation class from others
     * @param parameters additional parameters necessary to create the constructor object
     * @return the constructor to use for creating an instance
     * @throws NoSuchMethodException in case of error
     */
    protected abstract Constructor getConstructor(Class implementationClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException;
    
    /**
     * Create an implementation class instance.
     * 
     * @param constructor the constructor to use for creating the instance
     * @param hint the hint to differentiate this implementation class from others
     * @param parameters additional parameters necessary to create the instance
     * @return the created instance
     * @throws Exception in case of error
     */
    protected abstract Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception;

    /**
     * @param containerIdentity the container for which to look for registered hints
     * @return the hints that have been registered for this container identity
     */
    private List getHints(ContainerIdentity containerIdentity)
    {
        List hints = new ArrayList();
        for (Iterator keys = getMappings().keySet().iterator(); keys.hasNext();)
        {
            RegistrationKey key = (RegistrationKey) keys.next();
            if (key.getContainerIdentity().equals(containerIdentity))
            {
                hints.add(key.getHint());
            }
        }
        return hints;
    }
}
