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
package org.codehaus.cargo.container.tomcat.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilder;

/**
 * Constructs xml elements needed to configure a DataSource for Tomcat. Note that this
 * implementation converts DataSources into Resources and then uses an appropriate
 * {@link org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder} to create the
 * configuration.
 * 
 * @version $Id$
 */
public abstract class AbstractTomcatConfigurationBuilder extends AbstractConfigurationBuilder
{
    /**
     * contains a mapping of resource types to the factory they use. There should always be a key
     * "default" which can be used for unknown objects.
     */
    protected Map<String, String> typeToFactory;

    /**
     * generates {@link #typeToFactory}
     */
    public AbstractTomcatConfigurationBuilder()
    {
        typeToFactory = new HashMap<String, String>();
        typeToFactory.put("default", "org.apache.naming.factory.BeanFactory");
        typeToFactory.put(ConfigurationEntryType.MIMETYPE_DATASOURCE,
            "org.apache.naming.factory.SendMailFactory");
        typeToFactory.put(ConfigurationEntryType.MAIL_SESSION,
            "org.apache.naming.factory.MailSessionFactory");
    }

    /**
     * {@inheritDoc} this implementation first converts the DataSource to a Resource before
     * returning XML.
     * 
     * @see #convertDataSourceToResourceAndGetXMLEntry(DataSource)
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithNoTx(DataSource ds)
    {
        return convertDataSourceToResourceAndGetXMLEntry(ds);

    }

    /**
     * This method converts the DataSource to a Resource and then builds the xml entry based on
     * that.
     * 
     * @return a datasource xml fragment that can be embedded directly into the server.xml file
     * @param ds the DataSource we are configuring.
     */
    protected String convertDataSourceToResourceAndGetXMLEntry(DataSource ds)
    {

        Resource dataSourceResource = convertToResource(ds);
        return toConfigurationEntry(dataSourceResource);
    }

    /**
     * This method converts the DataSource to a Resource used in Tomcat.
     * 
     * @return a Resource that can be used in Tomcat.
     * @param ds the DataSource we are configuring.
     */
    protected Resource convertToResource(DataSource ds)
    {
        Properties parameters = new Properties();
        PropertyUtils.setPropertyIfNotNull(parameters, "url", ds.getUrl());
        PropertyUtils.setPropertyIfNotNull(parameters, "username", ds.getUsername());
        PropertyUtils.setPropertyIfNotNull(parameters, "password", ds.getPassword());

        parameters.putAll(ds.getConnectionProperties());

        Resource resource = new Resource(ds.getJndiLocation(), ConfigurationEntryType.DATASOURCE);
        PropertyUtils.setPropertyIfNotNull(parameters, "driverClassName", ds.getDriverClass());
        resource.setParameters(PropertyUtils.toMap(parameters));
        return resource;
    }

    /**
     * @return the <code>factory</code> responsible for creating the objects.
     * @param type the type of object we are creating
     */
    protected String getFactoryClassFor(String type)
    {
        String returnVal = typeToFactory.get(type);
        if (returnVal == null)
        {
            returnVal = typeToFactory.get("default");
        }
        return returnVal;
    }

    /**
     * {@inheritDoc} This throws an UnsupportedOperationException as Tomcat is not transactional.
     */
    @Override
    public String buildConfigurationEntryForXADataSourceConfiguredDataSource(DataSource ds)
    {
        throw new UnsupportedOperationException("Tomcat does not support "
            + "XADataSource configured DataSource implementations.");
    }

    /**
     * {@inheritDoc} This throws an UnsupportedOperationException as Tomcat is not transactional.
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithLocalTx(DataSource ds)
    {
        throw new UnsupportedOperationException("Tomcat does not support "
            + ds.getTransactionSupport() + " for DataSource implementations.");
    }

    /**
     * {@inheritDoc} This throws an UnsupportedOperationException as Tomcat is not transactional.
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithXaTx(DataSource ds)
    {
        throw new UnsupportedOperationException("Tomcat does not support "
            + ds.getTransactionSupport() + " for DataSource implementations.");
    }

}
