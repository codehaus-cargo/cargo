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
package org.codehaus.cargo.container.weblogic.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilder;
import org.codehaus.cargo.util.CargoException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * Contains the xml elements used to build a normal or XA compliant DataSource for WebLogic.
 */
public class WebLogic8xConfigurationBuilder extends AbstractConfigurationBuilder
{

    /**
     * Exception message when trying to configure a Resource.
     */
    public static final String RESOURCE_CONFIGURATION_UNSUPPORTED =
        "WebLogic does not support configuration of arbitrary resources into the JNDI tree.";

    /**
     * XML DOM document builder.
     */
    protected DocumentBuilder builder;

    /**
     * used for targeting the DataSource resources.
     */
    private String serverName;

    /**
     * Create an instance to add configuration for a particular server.
     * 
     * @param serverName server to target resources to.
     */
    public WebLogic8xConfigurationBuilder(String serverName)
    {
        this.setServerName(serverName);
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
     * @return a datasource xml fragment that can be embedded directly into the config.xml file
     * @param ds the DataSource we are configuring.
     * @param className the implementation class used for this DataSource
     */
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {
        Document document = builder.newDocument();

        Element connectionPool = document.createElement("JDBCConnectionPool");
        connectionPool.setAttribute("Name", ds.getJndiLocation());
        connectionPool.setAttribute("Targets", getServerName());
        if (ds.getUrl() != null)
        {
            connectionPool.setAttribute("URL", ds.getUrl());
        }
        connectionPool.setAttribute("DriverName", className);
        connectionPool.setAttribute("Password", ds.getPassword());
        // there is no native property for user in WebLogic JDBCConnectionPool
        ds.getConnectionProperties().setProperty("user", ds.getUsername());
        connectionPool.setAttribute("Properties", new DataSourceConverter()
            .getConnectionPropertiesAsASemicolonDelimitedString(ds));
        Element dataSource = null;
        if (ds.getTransactionSupport().equals(TransactionSupport.NO_TRANSACTION))
        {
            dataSource = document.createElement("JDBCDataSource");
        }
        else
        {
            dataSource = document.createElement("JDBCTxDataSource");
        }
        if (ds.getTransactionSupport().equals(TransactionSupport.XA_TRANSACTION)
            && ds.getDriverClass() != null)
        {
            dataSource.setAttribute("EnableTwoPhaseCommit", "true");
        }
        dataSource.setAttribute("Name", ds.getJndiLocation());
        dataSource.setAttribute("PoolName", ds.getJndiLocation());
        dataSource.setAttribute("JNDIName", ds.getJndiLocation());
        dataSource.setAttribute("Targets", getServerName());

        StringBuilder out = new StringBuilder();
        DOMImplementationLS implementation = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = implementation.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        out.append(serializer.writeToString(connectionPool));
        out.append("\n");
        out.append(serializer.writeToString(dataSource));
        return out.toString();
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
     * @param serverName the serverName to set
     */
    protected void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * @return the serverName
     */
    protected String getServerName()
    {
        return serverName;
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in Orion.
     */
    @Override
    public String toConfigurationEntry(Resource resource)
    {
        throw new UnsupportedOperationException(RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

}
