/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.resin;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Special container support for the Caucho Resin 3.1.x servlet container.
 */
public class Resin31xInstalledLocalContainer extends Resin3xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "resin31x";

    /**
     * {@inheritDoc}
     * @see Resin3xInstalledLocalContainer#Resin3xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Resin31xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
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
        return "Resin " + getVersion("3.1.x");
    }
}
