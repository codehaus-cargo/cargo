/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.orion;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.orion.internal.AbstractOc4j10xInstalledLocalContainer;

/**
 * Installed local container for the OC4J 10.x application server.
 */
public class Oc4j10xInstalledLocalContainer extends AbstractOc4j10xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "oc4j10x";

    /**
     * {@inheritDoc}
     * @see AbstractOc4j10xInstalledLocalContainer#AbstractOc4j10xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Oc4j10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStartClassname()
    {
        return "oracle.oc4j.loader.boot.BootStrap";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStopClassname()
    {
        return "oracle.oc4j.admin.deploy.cmdline.Oc4jAdminCmdline";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getContainerClasspathIncludes()
    {
        return new String[]
        {
            getFileHandler().append(getHome(), "j2ee/home/oc4j.jar"),
            getFileHandler().append(getHome(), "j2ee/home/lib/pcl.jar")
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "OC4J 10.x";
    }

}
