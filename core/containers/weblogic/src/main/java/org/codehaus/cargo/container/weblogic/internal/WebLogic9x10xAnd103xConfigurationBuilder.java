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
package org.codehaus.cargo.container.weblogic.internal;

import java.util.Iterator;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Constructs xml elements needed to configure a normal or XA compliant DataSource for WebLogic
 * 9-10.3.
 * 
 * @version $Id: $
 */
public class WebLogic9x10xAnd103xConfigurationBuilder extends
    WebLogic8xConfigurationBuilder
{
    /**
     * Sets the server name that the resources this creates are bound to.
     * 
     * @param serverName which server the resources configured here will be bound to.
     */
    public WebLogic9x10xAnd103xConfigurationBuilder(String serverName)
    {
        super(serverName);
    }

    /**
     * {@inheritDoc}
     * 
     * In WebLogic 9.x DataSource definitions are located in separate files linked to config.xml.
     * This method creates the definition of the datasource. This file must be linked to the
     * config.xml to become useful.
     */
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {

        Element nameElement = DocumentHelper.createDocument().addElement("name");
        nameElement.setText(ds.getId());

        Element driverElement = DocumentHelper.createDocument().addElement("jdbc-driver-params");
        if (ds.getUrl() != null)
        {
            driverElement.addElement("url").setText(ds.getUrl());
        }
        driverElement.addElement("driver-name").setText(className);
        Element properties = driverElement.addElement("properties");

        ds.getConnectionProperties().setProperty("user", ds.getUsername());
        Iterator driverProperties = ds.getConnectionProperties().keySet().iterator();
        while (driverProperties.hasNext())
        {
            String name = driverProperties.next().toString();
            String value = ds.getConnectionProperties().getProperty(name);
            Element property = properties.addElement("property");
            property.addElement("name").setText(name);
            property.addElement("value").setText(value);

        }
        Element dataSourceElement =
            DocumentHelper.createDocument().addElement("jdbc-data-source-params");
        dataSourceElement.addElement("jndi-name").setText(ds.getJndiLocation());
        if (ds.getConnectionType().equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            dataSourceElement.addElement("global-transactions-protocol")
                .setText("TwoPhaseCommit");
        }
        else if (ds.getTransactionSupport().equals(TransactionSupport.XA_TRANSACTION))
        {
            dataSourceElement.addElement("global-transactions-protocol").setText(
                "EmulateTwoPhaseCommit");
        }
        else
        {
            dataSourceElement.addElement("global-transactions-protocol").setText("None");
        }
        if (ds.getPassword() != null)
        {
            driverElement.addElement("password-encrypted").setText(ds.getPassword());
        }
        StringBuffer out = new StringBuffer();
        out.append(nameElement.asXML());
        out.append("\n");
        out.append(driverElement.asXML());
        out.append("\n");
        out.append(dataSourceElement.asXML());
        return out.toString();
    }

}
