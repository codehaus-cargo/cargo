/*
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jboss.internal.AbstractJBossInstalledLocalContainer;

/**
 * JBoss 3.x series container implementation.
 * 
 * @version $Id$
 */
public class JBoss3xInstalledLocalContainer extends AbstractJBossInstalledLocalContainer
{
    /**
     * JBoss 3.x series unique id.
     */
    public static final String ID = "jboss3x";

    /**
     * {@inheritDoc}
     * @see AbstractJBossInstalledLocalContainer#AbstractJBossInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public JBoss3xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "JBoss " + getVersion("3x");
    }
}
