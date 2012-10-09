/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
 * @version $Id: $
 */
public class DaemonStart
{
    /**
     * The unique identifier of the container to start
     */
    private String handleId;
    
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
    
}
