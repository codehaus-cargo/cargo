/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Copyright 2004-2006 Vincent Massol.
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

import java.util.HashSet;
import java.util.Set;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.orion.internal.AbstractOc4j10xInstalledLocalContainer;

/**
 * Installed local container for the OC4J 10.x application server.
 *
 * @version $Id: $
 */
public class Oc4j10xInstalledLocalContainer extends AbstractOc4j10xInstalledLocalContainer
{
    /**
     * {@inheritDoc}
     */
    public Oc4j10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Unique container id.
     */
    public static final String ID = "oc4j10x";

    /**
     * {@inheritDoc}
     */
    protected String getStartClassname()
    {
        return "oracle.oc4j.loader.boot.BootStrap";
    }

    /**
     * {@inheritDoc}
     */
    protected String getStopClassname()
    {
        return "oracle.oc4j.admin.deploy.cmdline.Oc4jAdminCmdline";
    }

    /**
     * {@inheritDoc}
     */
    protected Set getContainerClasspathIncludes()
    {
        Set classpath = new HashSet();
        classpath.add("j2ee/home/oc4j.jar");
        classpath.add("j2ee/home/lib/pcl.jar");

        return classpath;
    }

    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "OC4J 10.x";
    }

}
