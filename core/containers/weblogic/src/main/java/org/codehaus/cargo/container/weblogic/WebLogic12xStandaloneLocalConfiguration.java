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
package org.codehaus.cargo.container.weblogic;

/**
 * WebLogic 12.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * WebLogic 12.x is only slightly different to configure then WebLogic 10.3.x.
 * 
 * @version $Id$
 */
public class WebLogic12xStandaloneLocalConfiguration extends
    WebLogic103xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see WebLogic103xStandaloneLocalConfiguration#WebLogic103xStandaloneLocalConfiguration(String)
     */
    public WebLogic12xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "12.1.1.0");
        setProperty(WebLogicPropertySet.DOMAIN_VERSION, "12.1.1.0");
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "WebLogic 12.x Standalone Configuration";
    }

}
