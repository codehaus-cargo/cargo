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
package org.codehaus.cargo.container.jrun.internal;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilder;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Contains the xml elements used to build a normal or XA compliant DataSource for JRun.
 * 
 * @version $Id: $
 */
public class JRun4xConfigurationBuilder extends AbstractConfigurationBuilder
{

    /**
     * Exception message when trying to configure a Resource.
     */
    public static final String RESOURCE_CONFIGURATION_UNSUPPORTED =
        "JRun does not support configuration of arbitrary resources into the JNDI tree.";

    /**
     * @return  a datasource xml fragment that can be embedded directly into the 
     *          jrun-resources.xml file
     * @param ds the DataSource we are configuring.
     * @param className the implementation class used for this DataSource
     */
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {
        Element datasourceElement = DocumentHelper.createDocument().addElement("data-source");
        
        // settings from the DataSource instance.
        datasourceElement.addElement("dbname").setText(ds.getId());        
        datasourceElement.addElement("jndi-name").setText(ds.getJndiLocation());
        datasourceElement.addElement("driver").setText(ds.getDriverClass());
        datasourceElement.addElement("url").setText(ds.getUrl());
        datasourceElement.addElement("username").setText(ds.getUsername());
        datasourceElement.addElement("password").setText(ds.getPassword());
 
        // some default settings not available from DataSource instance
        datasourceElement.addElement("isolation-level").setText("READ_UNCOMMITTED");
        datasourceElement.addElement("native-results").setText("true");
        datasourceElement.addElement("pool-statements").setText("true");
        datasourceElement.addElement("pool-name").setText("pool");
        datasourceElement.addElement("initial-connections").setText("1");
        datasourceElement.addElement("minimum-size").setText("1");
        datasourceElement.addElement("maximum-size").setText("70");
        datasourceElement.addElement("connection-timeout").setText("60");
        datasourceElement.addElement("user-timeout").setText("120");
        datasourceElement.addElement("skimmer-frequency").setText("1800");
        datasourceElement.addElement("shrink-by").setText("10");
        datasourceElement.addElement("debugging").setText("true");
        datasourceElement.addElement("transaction-timeout").setText("10");
        datasourceElement.addElement("cache-enabled").setText("false");
        datasourceElement.addElement("cache-size").setText("10");
        datasourceElement.addElement("remove-on-exceptions").setText("false");
        
        return datasourceElement.asXML();
    }

    /**
     * {@inheritDoc}
     */
    public String buildEntryForDriverConfiguredDataSourceWithLocalTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc}
     */
    public String buildEntryForDriverConfiguredDataSourceWithNoTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());

    }

    /**
     * {@inheritDoc}
     */
    public String buildEntryForDriverConfiguredDataSourceWithXaTx(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());

    }

    /**
     * {@inheritDoc}
     */
    public String buildConfigurationEntryForXADataSourceConfiguredDataSource(DataSource ds)
    {
        return configureDataSourceWithImplementationClass(ds, ds.getDriverClass());
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in JRun.
     */
    public String toConfigurationEntry(Resource resource)
    {
        throw new UnsupportedOperationException(RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

}
