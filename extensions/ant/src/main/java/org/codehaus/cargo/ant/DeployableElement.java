/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.ant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;

/**
 * Nested Ant element to wrap a {@link Deployable}.
 *
 * @version $Id$
 */
public class DeployableElement
{
    /**
     * Deployable type ("war", "ear", "ejb", etc).
     */
    private DeployableType type;
    
    /**
     * The deployable file.
     */
    private String file;

    /**
     * Optional implementation class for custom deployable types. 
     */
    private Class deployableClass;

    /**
     * Deployable factory to use to create deployables.
     */
    private DeployableFactory factory = new DefaultDeployableFactory(); 

    /**
     * Deployable properties.
     */
    private List properties = new ArrayList();
    
    /**
     * @param file the deployable file to wrap
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * @param type the deployable type ("war", "ejb", "ear", etc) 
     */
    public void setType(String type)
    {
        this.type = DeployableType.toType(type);
    }

    /**
     * @param deployableClass a custom deployable class to register against the specified type
     */
    public void setClass(Class deployableClass)
    {
        this.deployableClass = deployableClass;
    }

    /**
     * Add a deployable property.
     * 
     * @param property the deployable property to add 
     */
    public void addConfiguredProperty(Property property)
    {
        this.properties.add(property);
    }
    
    /**
     * @param containerId the container id to which this deployable will be deployed
     * @return a {@link Deployable} representing this nested Ant element
     */
    public Deployable createDeployable(String containerId)
    {
        if (getFile() == null)
        {
            throw new BuildException("The [file] attribute is mandatory");
        }

        if (getType() == null)
        {
            throw new BuildException("The [type] attribute is mandatory");
        }
        
        // If a custom implementation class is defined register it against the deployable factory.
        if (getDeployableClass() != null)
        {
            this.factory.registerDeployable(containerId, getType(), getDeployableClass()); 
        }

        Deployable deployable = this.factory.createDeployable(containerId, getFile(), getType());
        
        // Set user-defined properties on the created deployable.
        Iterator props = getProperties().iterator();
        while (props.hasNext())
        {
            callMethodForProperty(deployable, (Property) props.next());
        }
        
        return deployable; 
    }

    /**
     * Call setter methods corresponding to deployable properties.
     * 
     * @param deployable the deployable on which to call the setter method corresponding to the
     *        specified property
     * @param property the deployable property used to call the setter method 
     */
    private void callMethodForProperty(Deployable deployable, Property property)
    {
        try
        {
            Method method = deployable.getClass().getMethod(getSetterMethodName(property.getName()),
                new Class[] {String.class});
            method.invoke(deployable, new Object[] {property.getValue()});
        }
        catch (Exception e)
        {
            throw new BuildException("Invalid property [" + property.getName() 
                + "] for deployable type [" + deployable.getType() + "]", e);
        }
    }

    /**
     * Transform a property into a method name by transforming the first letter of the property
     * name to uppercase.
     * 
     * @param propertyName the property name to transform into a setter method
     * @return the setter method's name
     */
    protected String getSetterMethodName(String propertyName)
    {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }
    
    /**
     * @return the deployable type
     */
    public DeployableType getType()
    {
        return this.type;
    }

    /**
     * @return the location of the deployable file to wrap
     */
    public String getFile()
    {
        return this.file;
    }

    /**
     * @return the custom deployable implementation to use (if defined by the user)
     */
    public Class getDeployableClass()
    {
        return this.deployableClass;
    }
    
    /**
     * {@inheritDoc}
     * @see #addConfiguredProperty(Property)
     */
    public final List getProperties()
    {
        return this.properties;
    }
}
