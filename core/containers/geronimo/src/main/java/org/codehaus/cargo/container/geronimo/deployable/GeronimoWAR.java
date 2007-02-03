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
package org.codehaus.cargo.container.geronimo.deployable;

import org.codehaus.cargo.container.deployable.WAR;

/**
 * Geronimo WAR deployable.
 *
 * @version $Id$
 */
public class GeronimoWAR extends WAR implements GeronimoDeployable
{
    /**
     * The path to the location of the EAR being wrapped.
     */
    private String plan;

    /**
     * @param war the location of the WAR being wrapped. This must point to either a WAR file or an
     *        expanded WAR directory.
     */
    public GeronimoWAR(String war)
    {
        super(war);
    }
    
    /**
     * {@inheritDoc}
     * @see GeronimoDeployable#setPlan(String)
     */
    public void setPlan(String plan)
    {
        this.plan = plan;
    }

    /**
     * {@inheritDoc}
     * @see GeronimoDeployable#getPlan()
     */
    public String getPlan()
    {
        return this.plan;
    }
}
