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
package org.codehaus.cargo.container.orion.internal;

import java.util.Iterator;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractConfigurationBuilder;

/**
 * Contains the xml elements used to build a normal or XA compliant DataSource for Orion. Note that
 * this implementation returns multiple xml elements for DataSources which specify dataSourceclass.
 * 
 * @version $Id$
 */
public class OrionConfigurationBuilder extends AbstractConfigurationBuilder
{

    /**
     * Exception message when trying to configure a Resource.
     */
    public static final String RESOURCE_CONFIGURATION_UNSUPPORTED =
        "Orion does not support configuration of arbitrary resources into the JNDI tree.";
    
    /**
     * the DataSource we are generating Orion configuration for.
     */
    private DataSource ds;

    /**
     * internal buffer used. recreated for each public method.
     */
    private StringBuffer buffer;

    /**
     * create the default instance by passing control to the superclass.
     * 
     * @see AbstractConfigurationBuilder#AbstractConfigurationBuilder()
     */
    public OrionConfigurationBuilder()
    {
        super();
    }

    /**
     * adds xml element representing a provided DataSource, wrapped by a container managed one. The
     * provided DataSource will have the suffix: "Provided" in its xml element and jndi name.
     * 
     * @param className - the class implementing DataSource, XADataSource, etc.
     */
    private void addBackingDataSourceWithImplementationClass(String className)
    {
        startDataSourceTag();
        setImplementationClassAndJndiContext(className, "Provided");
        addVendorConnectionDataAndCloseTag();
    }

    /**
     * adds vendor connection details, such as url, then stops the DataSource tag.
     */
    private void addVendorConnectionDataAndCloseTag()
    {
        addCommonConnectionAttributes();
        endDataSourceTag();
        addNestedDriverPropertyTags();
        closeDataSourceTag();
    }

    /**
     * adds xml element representing a Container Managed Transaction DataSource.
     */
    private void addCMTDataSourceTag()
    {
        startDataSourceTag();
        addCMTDataSourceAndLinkToXa();
        addIdAndJndiLocationAttributes();
        endDataSourceTag();
        closeDataSourceTag();
    }

    /**
     * terminates the data-source tag (ex. </data-source> ).
     */
    private void closeDataSourceTag()
    {
        buffer.append("</data-source>\n");
    }

    /**
     * adds details necessary to create the vendor's DataSource or XADataSource object.
     * 
     * @param className - the class implementing DataSource, XADataSource, etc.
     * @param suffix - what to append to the id and jndi location
     */
    private void setImplementationClassAndJndiContext(String className, String suffix)
    {
        buffer.append("    name='").append(ds.getId()).append(suffix).append("' \n");
        buffer.append("    connection-driver='" + className + "'\n");
        buffer.append("    class='" + className + "'\n");
        buffer.append("    location='").append(ds.getJndiLocation()).append(suffix)
            .append("' \n");
    }

    /**
     * ends the data-source tag in progress (ex. adds a greater than ).
     */
    private void endDataSourceTag()
    {
        buffer.append(" >\n");
    }

    /**
     * opens the data-source tag.
     */
    private void startDataSourceTag()
    {
        buffer.append("<data-source \n");
    }

    /**
     * adds the DataSource the application will use to make connections to an XADataSource.
     */
    private void addCMTDataSourceAndLinkToXa()
    {
        buffer.append("    class='com.evermind.sql.OrionCMTDataSource' \n ");
        buffer.append("    xa-source-location='").append(ds.getJndiLocation()).append("Provided")
            .append("' \n");
    }

    /**
     * adds the driver details necessary for normal DataSource objects.
     */
    private void addDriverAttributes()
    {
        buffer.append("    class='com.evermind.sql.DriverManagerDataSource' \n ");
        buffer.append("    connection-driver='" + ds.getDriverClass() + "'\n");
    }

    /**
     * sets the name and location of the datasource.
     */
    private void addIdAndJndiLocationAttributes()
    {
        buffer.append("    name='").append(ds.getId()).append("' \n");
        buffer.append("    location='").append(ds.getJndiLocation()).append("' \n");
    }

    /**
     * sets the name and jndi locations of the datasource.
     * 
     * @param location - jndi context to bind a non-transactional reference to this datasource.
     * @param ejbLocation - jndi context to bind a local transactional reference to this datasource.
     * @param xaLocation - jndi context to bind an xml supporting reference to this datasource.
     */
    private void addIdAndExtraLocationAttributes(String location, String ejbLocation,
        String xaLocation)
    {
        buffer.append("    name='").append(ds.getId()).append("' \n");
        buffer.append("    location='").append(location).append("' \n");
        buffer.append("    ejb-location='").append(ejbLocation).append("' \n");
        buffer.append("    xa-location='").append(xaLocation).append("' \n");
    }

    /**
     * adds common details such as username, password, and url. used by both XA and non-XA
     * connections.
     */
    private void addCommonConnectionAttributes()
    {
        buffer.append("    username='").append(ds.getUsername()).append("' \n");
        buffer.append("    password='").append(ds.getPassword()).append("' \n");
        if (ds.getUrl() != null)
        {
            buffer.append("    url='" + ds.getUrl()).append("' \n");
        }
        buffer.append("    inactivity-timeout='30'");
    }

    /**
     * adds the driver-specific properties to the data-source tag.
     */
    private void addNestedDriverPropertyTags()
    {
        if (ds.getConnectionProperties() != null && ds.getConnectionProperties().size() != 0)
        {
            Iterator<Object> i = ds.getConnectionProperties().keySet().iterator();
            while (i.hasNext())
            {
                String key = i.next().toString();
                buffer.append("      <property name=\"").append(key);
                buffer.append("\" value=\"")
                    .append(ds.getConnectionProperties().getProperty(key)).append("\" />\n");
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithNoTx(DataSource ds)
    {
        this.ds = ds;
        this.buffer = new StringBuffer();
        startDataSourceTag();
        addDriverAttributes();
        addIdAndExtraLocationAttributes(ds.getJndiLocation(), ds.getJndiLocation() + "local", ds
            .getJndiLocation()
            + "xa");
        addVendorConnectionDataAndCloseTag();
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithLocalTx(DataSource ds)
    {
        this.ds = ds;
        this.buffer = new StringBuffer();
        startDataSourceTag();
        addDriverAttributes();
        addIdAndExtraLocationAttributes(ds.getJndiLocation() + "notx", ds.getJndiLocation(), ds
            .getJndiLocation()
            + "xa");
        addVendorConnectionDataAndCloseTag();
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildEntryForDriverConfiguredDataSourceWithXaTx(DataSource ds)
    {
        this.ds = ds;
        this.buffer = new StringBuffer();
        startDataSourceTag();
        addDriverAttributes();
        addIdAndExtraLocationAttributes(ds.getJndiLocation() + "notx", ds.getJndiLocation()
            + "local", ds.getJndiLocation());
        addVendorConnectionDataAndCloseTag();
        return buffer.toString();
    }

    /**
     * {@inheritDoc} This implementation will create two elements, one for the XA DataSource, and
     * another that proxies that. The jndi location users will use will be to the proxy.
     */
    @Override
    public String buildConfigurationEntryForXADataSourceConfiguredDataSource(DataSource ds)
    {
        this.ds = ds;
        this.buffer = new StringBuffer();
        addBackingDataSourceWithImplementationClass(ds.getDriverClass());
        addCMTDataSourceTag();
        return buffer.toString();
    }

    /**
     * {@inheritDoc} This implementation throws an UnsupportedOperationException as Resource
     * configuration is not supported in Orion.
     */
    public String toConfigurationEntry(Resource resource)
    {
        throw new UnsupportedOperationException(RESOURCE_CONFIGURATION_UNSUPPORTED);
    }

}
