/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * Constructs xml elements needed to configure a normal or XA compliant DataSource for WebLogic
 * 9-10.3.
 */
public class WebLogic9x10x103x12xConfigurationBuilder extends
    WebLogic8xConfigurationBuilder
{
    /**
     * Sets the server name that the resources this creates are bound to.
     * 
     * @param serverName which server the resources configured here will be bound to.
     */
    public WebLogic9x10x103x12xConfigurationBuilder(String serverName)
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
    @Override
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {
        Document document = builder.newDocument();

        Element nameElement = document.createElement("name");
        nameElement.setTextContent(ds.getId());

        Element driverElement = document.createElement("jdbc-driver-params");
        if (ds.getUrl() != null)
        {
            Element urlElement = document.createElement("url");
            driverElement.appendChild(urlElement);
            urlElement.setTextContent(ds.getUrl());
        }
        Element driverNameElement = document.createElement("driver-name");
        driverElement.appendChild(driverNameElement);
        driverNameElement.setTextContent(className);
        Element properties = document.createElement("properties");
        driverElement.appendChild(properties);
        ds.getConnectionProperties().setProperty("user", ds.getUsername());
        Iterator<Object> driverProperties = ds.getConnectionProperties().keySet().iterator();
        while (driverProperties.hasNext())
        {
            String name = driverProperties.next().toString();
            String value = ds.getConnectionProperties().getProperty(name);
            Element property = document.createElement("property");
            properties.appendChild(property);
            Element propertyNameElement = document.createElement("name");
            property.appendChild(propertyNameElement);
            propertyNameElement.setTextContent(name);
            Element propertyValueElement = document.createElement("value");
            property.appendChild(propertyValueElement);
            propertyValueElement.setTextContent(value);
        }
        if (ds.getPassword() != null)
        {
            Element passwordElement = document.createElement("password-encrypted");
            driverElement.appendChild(passwordElement);
            passwordElement.setTextContent(ds.getPassword());
        }

        Element dataSourceElement = document.createElement("jdbc-data-source-params");
        Element jndiNameElement = document.createElement("jndi-name");
        dataSourceElement.appendChild(jndiNameElement);
        jndiNameElement.setTextContent(ds.getJndiLocation());
        Element transactionsElement = document.createElement("global-transactions-protocol");
        dataSourceElement.appendChild(transactionsElement);
        if (ds.getConnectionType().equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            transactionsElement.setTextContent("TwoPhaseCommit");
        }
        else if (ds.getTransactionSupport().equals(TransactionSupport.XA_TRANSACTION))
        {
            transactionsElement.setTextContent("EmulateTwoPhaseCommit");
        }
        else
        {
            transactionsElement.setTextContent("None");
        }

        StringBuilder out = new StringBuilder();
        DOMImplementationLS implementation = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = implementation.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        out.append(serializer.writeToString(nameElement));
        out.append("\n");
        out.append(serializer.writeToString(driverElement));
        out.append("\n");
        out.append(serializer.writeToString(dataSourceElement));
        return out.toString();
    }

}
