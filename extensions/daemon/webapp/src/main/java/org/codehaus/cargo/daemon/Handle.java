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
package org.codehaus.cargo.daemon;

import java.util.Map.Entry;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.daemon.properties.PropertyTable;

/**
 * A handle keeps track of deployed containers.
 *
 * @version $Id$
 */
public class Handle
{
    /**
     * The key representing the autostart flag.
     */
    private static final String KEY_AUTOSTART = "autostart";

    /**
     * The key representing the log file path.
     */
    private static final String KEY_LOGPATH = "logpath";

    /**
     * The unique handle identifier of a container.
     */
    private String id;

    /**
     * The installed container.
     */
    private InstalledLocalContainer container;

    /**
     * The configuration.
     */
    private LocalConfiguration configuration;
    
    
    /**
     * Tells if the container was forcibly stopped.
     */
    private boolean forceStop = false;

    /**
     * Properties of the handle.
     */
    private PropertyTable properties = new PropertyTable();
    
    
    /**
     * @return the handle identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the handle identifier.
     *
     * @param id The handle identifier.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the container
     */
    public InstalledLocalContainer getContainer()
    {
        return container;
    }

    /**
     * Sets the container.
     *
     * @param container The container.
     */
    public void setContainer(InstalledLocalContainer container)
    {
        this.container = container;
    }

    /**
     * @return the configuration.
     */
    public LocalConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration
     */
    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }
    
    /**
     * @return the properties.
     */
    public PropertyTable getProperties()
    {
        return properties;
    }

    /**
     * Sets the properties.
     *
     * @param properties The properties
     */
    public void setProperties(PropertyTable properties)
    {
        this.properties = properties;
    }
    

    @Override
    public String toString()
    {
        return id;
    }

    /**
     * @return if the container associated with this handle should autostart.
     */
    public boolean isAutostart()
    {
        return properties.getBoolean(KEY_AUTOSTART);
    }

    /**
     * Sets if the container should autostart
     *
     * @param autostart The autostart flag
     */
    public void setAutostart(boolean autostart)
    {
        this.properties.put(KEY_AUTOSTART, String.valueOf(autostart));
    }
    
    /**
     * @return the log file path of the container.
     */
    public String getLogPath()
    {
        return this.properties.get(KEY_LOGPATH);
    }

    /**
     * Set the log file path of the container
     *
     * @param logpath The log file path
     */
    public void setLogPath(String logpath)
    {
        this.properties.put(KEY_LOGPATH, logpath);
    }
    
    
    /**
     * @return true if the container was forcibly stopped.
     */
    public boolean isForceStop()
    {
        return forceStop;
    }

    /**
     * @param forceStop True if the container was forcibly stopped.
     */
    public void setForceStop(boolean forceStop)
    {
        this.forceStop = forceStop;
    }    

    /**
     * @return the status of the container.
     */
    public State getContainerStatus()
    {
        if (container == null)
        {
            return State.STOPPED;
        }
        else
        {
            return container.getState();
        }
    }

    /**
     * Add all the properties to this handle.
     * 
     * @param properties The list of properties
     */
    public void addProperties(PropertyTable properties)
    {
        if (properties == null)
        {
            return;
        }
        
        for (Entry<String, String> property : properties.entrySet())
        {
            String key = property.getKey();
            String value = property.getValue();
            
            this.properties.put(key, value);
        }
    }
}
