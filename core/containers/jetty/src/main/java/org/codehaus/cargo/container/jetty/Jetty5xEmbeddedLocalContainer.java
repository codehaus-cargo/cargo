/*
 * ========================================================================
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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.AbstractJetty4x5xEmbeddedLocalContainer;

/**
 * A Jetty 5.x instance running embedded.
 * 
 * @version $Id$
 */
public class Jetty5xEmbeddedLocalContainer extends AbstractJetty4x5xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty5x";

    /**
     * A default security realm. If ServletPropertySet.USERS has been specified, then we create a
     * default realm containing those users and then force that realm to be associated with every
     * webapp (see TODO comment on setSecurityRealm()).
     */
    private Object defaultRealm;

    /**
     * {@inheritDoc}
     * @see AbstractJetty4x5xEmbeddedLocalContainer#AbstractJetty4x5xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty5xEmbeddedLocalContainer(LocalConfiguration configuration)
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
        return "Jetty 5.x Embedded";
    }

    /**
     * @return the default security realm that is set
     */
    public Object getDefaultRealm()
    {
        return this.defaultRealm;
    }

    /**
     * @param webapp the webapp to set the default security realm on
     * @throws Exception invokation error
     */
    public void setDefaultRealm(Object webapp) throws Exception
    {
        if (this.defaultRealm != null)
        {
            webapp.getClass().getMethod("setRealm",
                new Class[] {this.defaultRealm.getClass()}).invoke(webapp,
                    new Object[] {this.defaultRealm});
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractJetty4x5xEmbeddedLocalContainer#performExtraSetupOnDeployable(Object)
     */
    @Override
    protected void performExtraSetupOnDeployable(Object webapp) throws Exception
    {
        setDefaultRealm(webapp);
    }
}
