/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.jrun.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilder;
import org.codehaus.cargo.util.CargoException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * Contains the xml elements used to build a normal or XA compliant DataSource for JRun.
 */
public class JRun4xConfigurationBuilder extends AbstractConfigurationBuilder
{

    /**
     * Exception message when trying to configure a Resource.
     */
    public static final String RESOURCE_CONFIGURATION_UNSUPPORTED =
        "JRun does not support configuration of arbitrary resources into the JNDI tree.";

    /**
     * XML DOM document builder.
     */
    private DocumentBuilder builder;

    /**
     * Initialized the XML DOM document builder.
     */
    public JRun4xConfigurationBuilder()
    {
        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            builder = domFactory.newDocumentBuilder();
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot initialize XML DOM document builder", e);
        }
    }

    /**
     * Create an XML child with a text content.
     * 
     * @param parent Parent XML element.
     * @param tagname XML tag name for the child.
     * @param textContent Text content for the child.
     */
    private void createTextContentChild(Element parent, String tagname, String textContent)
    {
        Element child = parent.getOwnerDocument().createElement(tagname);
        child.setTextContent(textContent);
        parent.appendChild(child);
    }

    /**
     * @return a datasource xml fragment that can be embedded directly into the jrun-resources.xml
     * file
     * @param ds the DataSource we are configuring.
     * @param className the implementation class used for this DataSource
     */
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {
        Document document = builder.newDocument();

        Element datasourceElement = document.createElement("data-source");
        document.appendChild(datasourceElement);

        // settings from the DataSource instance.
        createTextContentChild(datasourceElement, "dbname", ds.getId());
        createTextContentChild(datasourceElement, "jndi-name", ds.getJndiLocation());
        createTextContentChild(datasourceElement, "driver", ds.getDriverClass());
        createTextContentChild(datasourceElement, "url", ds.getUrl());
        createTextContentChild(datasourceElement, "username", ds.getUsername());
        createTextContentChild(datasourceElement, "password", ds.getPassword());

        // some default settings not available from DataSource instance
        createTextContentChild(datasourceElement, "isolation-level", "READ_UNCOMMITTED");
        createTextContentChild(datasourceElement, "native-results", "true");
        createTextContentChild(datasourceElement, "pool-statements", "true");
        createTextContentChild(datasourceElement, "pool-name", "pool");
        createTextContentChild(datasourceElement, "initial-connections", "1");
        createTextContentChild(datasourceElement, "minimum-size", "1");
        createTextContentChild(datasourceElement, "maximum-size", "70");
        createTextContentChild(datasourceElement, "connection-timeout", "60");
        createTextContentChild(datasourceElement, "user-timeout", "120");
        createTextContentChild(datasourceElement, "skimmer-frequency", "1800");
        createTextContentChild(datasourceElement, "shrink-by", "10");
        createTextContentChild(datasourceElement, "debugging", "true");
        createTextContentChild(datasourceElement, "transaction-timeout", "10");
        createTextContentChild(datasourceElement, "cache-enabled", "false");
        createTextContentChild(datasourceElement, "cache-size", "10");
        createTextContentChild(datasourceElement, "remove-on-exceptions", "false");

        DOMImplementationLS implementation = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = implementation.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer.writeToString(datasourceElement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithLocalTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithNoTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithXaTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildConfigurationEntryForXADataSourceConfiguredDataSource(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in JRun.
     */
    @Override
    public String toConfigurationEntry(Resource resource)
    {
        throw new UnsupportedOperationException(RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

}
