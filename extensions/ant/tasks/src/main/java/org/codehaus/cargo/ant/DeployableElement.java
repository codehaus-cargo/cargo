/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;

/**
 * Nested Ant element to wrap a {@link Deployable}.
 * 
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
     * Optional ping URL.
     */
    private URL pingURL;

    /**
     * Optional ping timeout.
     */
    private Long pingTimeout;

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
    private List<Property> properties = new ArrayList<Property>();

    /**
     * @param file the deployable file to wrap
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * @param pingURL the deployable ping URL
     */
    public void setPingUrl(URL pingURL)
    {
        this.pingURL = pingURL;
    }

    /**
     * @param pingTimeout the deployable ping timeout
     */
    public void setPingTimeout(Long pingTimeout)
    {
        this.pingTimeout = pingTimeout;
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
        for (Property property : getProperties())
        {
            if ("pingURL".equals(property.getName()))
            {
                try
                {
                    this.setPingUrl(new URL(property.getValue()));
                }
                catch (MalformedURLException e)
                {
                    throw new BuildException("Invalid value [" + property.getValue()
                        + "] for property [" + property.getName() + "]", e);
                }
            }
            else if ("pingTimeout".equals(property.getName()))
            {
                this.setPingTimeout(Long.getLong(property.getValue()));
            }
            else
            {
                try
                {
                    callMethodForProperty(deployable, property);
                }
                catch (Exception e)
                {
                    throw new BuildException("Invalid property [" + property.getName()
                        + "] for deployable type [" + deployable.getType() + "]", e);
                }
            }
        }

        return deployable;
    }

    /**
     * @return deployable monitor, if defined.
     */
    public DeployableMonitor createDeployableMonitor()
    {
        if (pingURL == null)
        {
            return null;
        }
        else
        {
            DeployableMonitor monitor;
            if (pingTimeout == null)
            {
                monitor = new URLDeployableMonitor(pingURL);
            }
            else
            {
                monitor = new URLDeployableMonitor(pingURL, pingTimeout.longValue());
            }
            return monitor;
        }
    }

    /**
     * Call setter methods corresponding to deployable properties.
     * 
     * @param deployable the deployable on which to call the setter method corresponding to the
     * specified property
     * @param property the deployable property used to call the setter method
     * @throws Exception if anything goes wrong.
     */
    private void callMethodForProperty(Deployable deployable, Property property) throws Exception
    {
        String setterMethodName = getSetterMethodName(property.getName());

        Method method;
        Object argument;

        try
        {
            method = deployable.getClass().getMethod(setterMethodName, new Class[] {String.class});
            argument = property.getValue();
        }
        catch (NoSuchMethodException e)
        {
            // If we reach this line, it means there is no String setter for the given property
            // name with a String argument. Check if there is a setter with String[] argument; if
            // there is one split the value at each line and call the setter.
            method = deployable.getClass().getMethod(setterMethodName,
                new Class[] {String[].class});

            List<String> valueList = new ArrayList<String>();
            StringTokenizer commaSeparatedValue = new StringTokenizer(property.getValue(), ",");
            while (commaSeparatedValue.hasMoreTokens())
            {
                String commaSeparatedLine = commaSeparatedValue.nextToken().trim();
                if (commaSeparatedLine.length() > 0)
                {
                    valueList.add(commaSeparatedLine);
                }
            }
            String[] valueArray = new String[valueList.size()];
            valueArray = valueList.toArray(valueArray);
            argument = valueArray;
        }

        method.invoke(deployable, new Object[] {argument});
    }

    /**
     * Transform a property into a method name by transforming the first letter of the property name
     * to uppercase.
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
     * @return the list of deployable properties
     */
    public List<Property> getProperties()
    {
        return this.properties;
    }
}
