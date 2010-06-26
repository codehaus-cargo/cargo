/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.IOException;
import java.util.Iterator;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.xml.sax.SAXException;

/**
 * Contains XML logic used to validate the XML output of a Tomcat 5 and 6.x DataSource
 * configuration.
 * 
 * @version $Id$
 */
public class Tomcat5And6xConfigurationChecker extends Tomcat4xConfigurationChecker
{
    @Override
    protected String getDataSourceFactory()
    {
        return "org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory";
    }

    @Override
    protected void notExists(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        String pathToResource =
            "//Context/Resource[@name='" + dataSourceFixture.jndiLocation + "']";

        XMLAssert.assertXpathNotExists(pathToResource, configuration);

    }

    @Override
    protected void checkConfigurationMatchesResource(String configuration, Resource resource)
        throws Exception
    {

        String pathToResource = "//Context/Resource[@name='" + resource.getName() + "']";

        XMLAssert.assertXpathEvaluatesTo("Container", pathToResource + "/@auth", configuration);
        if (resource.getClassName() != null)
        {
            XMLAssert.assertXpathEvaluatesTo(resource.getClassName(), pathToResource + "/@type",
                configuration);
        }
        else
        {
            XMLAssert.assertXpathEvaluatesTo(resource.getType(), pathToResource + "/@type",
                configuration);
        }

        Iterator i = resource.getParameterNames().iterator();
        while (i.hasNext())
        {
            String propertyName = i.next().toString();

            String propertyNameInTomcatXML = propertyName;
            if ("user".equals(propertyName))
            {
                // see: http://jira.codehaus.org/browse/CARGO-705
                propertyNameInTomcatXML = "username";
            }

            XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName), pathToResource
                + "/@" + propertyNameInTomcatXML, configuration);
        }
    }

    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Context>" + dataSourceEntry + "</Context>";
    }

    public void checkTransactionManagerToken(String xml) throws SAXException, IOException,
        XpathException
    {
        XMLAssert
            .assertXpathEvaluatesTo(
                "60",
                "//Context/Transaction[@factory='org.objectweb.jotm.UserTransactionFactory']/@jotm.timeout",
                xml);
    }
}
