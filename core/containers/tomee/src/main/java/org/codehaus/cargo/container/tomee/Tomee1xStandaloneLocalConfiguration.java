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
package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.tomcat.Tomcat7xStandaloneLocalConfiguration;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class Tomee1xStandaloneLocalConfiguration extends Tomcat7xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat7xStandaloneLocalConfiguration#Tomcat7xStandaloneLocalConfiguration(String)
     */
    public Tomee1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "TomEE 1.x Standalone Configuration";
    }

    /**
     * {@inheritDoc} Tomee provide it's own transaction factory with openejb, so we don't add
     * org.objectweb.jotm.UserTransactionFactory unlike Tomcat
     */
    @Override
    protected void setupTransactionManager()
    {
    }

}
