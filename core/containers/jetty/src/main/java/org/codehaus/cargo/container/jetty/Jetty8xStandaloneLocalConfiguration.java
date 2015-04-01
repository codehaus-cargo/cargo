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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;

/**
 * Jetty 8.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 */
public class Jetty8xStandaloneLocalConfiguration extends Jetty7xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Jetty7xStandaloneLocalConfiguration#Jetty7xStandaloneLocalConfiguration(String)
     */
    public Jetty8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureDatasource(LocalContainer container, String etcDir) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        createDatasourceDefinitions(sb, container);

        Map<String, String> jettyXmlReplacements = new HashMap<String, String>();
        jettyXmlReplacements.put("</Configure>", sb.toString() + "</Configure>");
        getFileHandler().replaceInFile(
            getFileHandler().append(etcDir, "jetty-plus.xml"), jettyXmlReplacements, "UTF-8");
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 8.x Standalone Configuration";
    }

}
