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
package org.codehaus.cargo.container.weblogic;

/**
 * WebLogic 10x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * WebLogic 10x is only slightly different to configure then WebLogic 9x.
 * 
 * @version $Id$
 */
public class WebLogic10xStandaloneLocalConfiguration extends
    WebLogic9xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see WebLogic9xStandaloneLocalConfiguration#WebLogic9xStandaloneLocalConfiguration(String)
     */
    public WebLogic10xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "10.0.1.0");
        setProperty(WebLogicPropertySet.DOMAIN_VERSION, "10.0.1.0");
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "WebLogic 10x Standalone Configuration";
    }

}
