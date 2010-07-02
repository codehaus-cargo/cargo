/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaInstalledLocalContainer;

/**
 * Special container support for the Apache Tomcat 5.x servlet container.
 * 
 * @version $Id$
 */
public class Tomcat5xInstalledLocalContainer extends AbstractCatalinaInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomcat5x";

    /**
     * Perform Tomcat 5.x-specific initializations.
     *
     * {@inheritDoc}
     * @see AbstractCatalinaInstalledLocalContainer#AbstractCatalinaInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration) 
     */
    public Tomcat5xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "Tomcat " + getVersion("5.x");
    }
}
