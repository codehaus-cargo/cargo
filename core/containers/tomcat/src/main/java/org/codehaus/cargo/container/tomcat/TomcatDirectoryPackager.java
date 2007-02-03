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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Package a Tomcat distribution.
 *  
 * @version $Id: Container.java 886 2006-02-28 12:40:47Z vmassol $
 */
public class TomcatDirectoryPackager extends AbstractDirectoryPackager
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager#AbstractDirectoryPackager(String) 
     */
    public TomcatDirectoryPackager(String targetDirectory)
    {
        super(targetDirectory);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager#getConfigurationExclusions()
     */
    protected List getConfigurationExclusions()
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager#getDistributionExclusions()
     */
    protected List getDistributionExclusions()
    {
        List excludes = new ArrayList();
        excludes.add("conf/**");
        excludes.add("logs/**");
        excludes.add("webapps/**");
        excludes.add("work/**");

        return excludes;
    }
}
