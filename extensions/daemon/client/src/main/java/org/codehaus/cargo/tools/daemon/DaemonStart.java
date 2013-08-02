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
package org.codehaus.cargo.tools.daemon;

import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;

/**
 * Represents a daemon start request
 *
 * @version $Id$
 */
public class DaemonStart
{
    /**
     * The unique identifier of the container to start
     */
    private String handleId;
    
    /**
     * Tells the daemon if the container should auto start.
     */
    private boolean autostart;    
    
    /**
     * Additional classpath entries.
     */
    private List<String> additionalClasspathEntries;

    /**
     * The container to start
     */
    private InstalledLocalContainer container;

    /**
     * The deployables to deploy
     */
    private List<Deployable> deployables;

    /**
     * The zip file to install
     */
    private String installerZipFile;

    /**
     * The log file where the Cargo log should be saved to.
     */
    private String logFile;

    
    /**
     * @return the unique identifier of the container to start.
     */
    public String getHandleId()
    {
        return handleId;
    }

    /**
     * @param handleId The unique identifier of the container to start.
     */
    public void setHandleId(String handleId)
    {
        this.handleId = handleId;
    }
    
    /**
     * @return the additional classpath entries.
     */
    public List<String> getAdditionalClasspathEntries()
    {
        return additionalClasspathEntries;
    }

    /**
     * @param additionalClasspathEntries The additional classpath entries to set for the container.
     */
    public void setAdditionalClasspathEntries(List<String> additionalClasspathEntries)
    {
        this.additionalClasspathEntries = additionalClasspathEntries;
    }
    
    
    /**
     * @return true if the container should auto start.
     */
    public boolean isAutostart()
    {
        return autostart;
    }

    /**
     * @param autostart True if the container should auto start.
     */
    public void setAutostart(boolean autostart)
    {
        this.autostart = autostart;
    }
    

    /**
     * @return the container to start
     */
    public InstalledLocalContainer getContainer()
    {
        return container;
    }

    /**
     * @param container The container to start
     */
    public void setContainer(InstalledLocalContainer container)
    {
        this.container = container;
    }

    /**
     * @return the deployables to deploy
     */
    public List<Deployable> getDeployables()
    {
        return deployables;
    }

    /**
     * @param deployables The deployables to deploy
     */
    public void setDeployables(List<Deployable> deployables)
    {
        this.deployables = deployables;
    }

    /**
     * @return the zip file to install
     */
    public String getInstallerZipFile()
    {
        return installerZipFile;
    }

    /**
     * @param installerZipFile The zip file to install
     */
    public void setInstallerZipFile(String installerZipFile)
    {
        this.installerZipFile = installerZipFile;
    }

    /**
     * @return the log file where to save the Cargo log
     */
    public String getLogFile()
    {
        return logFile;
    }

    /**
     * @param logFile The log file where to save the Cargo log
     */
    public void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }

}
