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
package org.codehaus.cargo.container.resin;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.resin.internal.Resin3xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Resin3xStandaloneLocalConfigurationTest extends
    AbstractResinStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Resin3xStandaloneLocalConfiguration}. {@inheritdoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Resin3xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Resin3xInstalledLocalContainer}. {@inheritdoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Resin3xInstalledLocalContainer(configuration);
    }

    @Override
    public void testConfigure() throws Exception
    {
        super.testConfigure();
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/app-default.xml"));
    }

    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = configuration.getHome() + "/conf/resin.conf";
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("resin");
        document.setRootElement(root);
        root.addNamespace("", "http://caucho.com/ns/resin");
        xmlUtil.saveXml(document, file);

        System.gc();
    }

    /**
     * @return {@link Resin3xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Resin3xConfigurationChecker();
    }

}
