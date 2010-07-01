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
package org.codehaus.cargo.container.spi.configuration.builder;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * {@inheritDoc}
 * 
 * @version $Id$
 */
public abstract class AbstractConfigurationBuilder implements ConfigurationBuilder
{
    /**
     * {@inheritDoc} Detects the type of <code>DataSource</code> to configure based on transactional
     * properties and implementation class.
     */
    public final String toConfigurationEntry(DataSource ds)
    {
        String returnVal = " ";
        if (ds.getConnectionType().equals(ConfigurationEntryType.JDBC_DRIVER))
        {
            returnVal = buildEntryForDriverConfiguredDataSource(ds);
        }
        else if (ds.getConnectionType().equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            returnVal = buildConfigurationEntryForXADataSourceConfiguredDataSource(ds);
        }
        else
        {
            throw new IllegalArgumentException("Connection Type: " + ds.getConnectionType()
                + " not supported");
        }

        return returnVal;
    }

    /**
     * This method looks at the transactional support for the DataSource and builds an appropriate
     * configuration entry.
     * 
     * @param ds the {@link DataSource} with the following state:
     *            <ul>
     *            <li>{@link DataSource#getJndiLocation()} is set to a unique path for the
     *            container.</li>
     *            <li>{@link DataSource#getDriverClass()} is an implementation of
     *            <code>java.sql.Driver</code></li>
     * @return configuration binding a container provided implementation of type
     *         <code>javax.sql.DataSource</code> to the JNDI path specified at
     *         {@link DataSource#getJndiLocation()}.
     */
    private String buildEntryForDriverConfiguredDataSource(DataSource ds)
    {
        String returnVal = null;

        if (ds.getTransactionSupport().equals(TransactionSupport.NO_TRANSACTION))
        {
            returnVal = buildEntryForDriverConfiguredDataSourceWithNoTx(ds);
        }
        else if (ds.getTransactionSupport().equals(TransactionSupport.LOCAL_TRANSACTION))
        {
            returnVal = buildEntryForDriverConfiguredDataSourceWithLocalTx(ds);
        }
        else if (ds.getTransactionSupport().equals(TransactionSupport.XA_TRANSACTION))
        {
            returnVal = buildEntryForDriverConfiguredDataSourceWithXaTx(ds);
        }
        else
        {
            throw new IllegalArgumentException("Transaction support: "
                + ds.getTransactionSupport() + " not supported");
        }
        return returnVal;
    }

    /**
     * @param ds the {@link DataSource} with the following state:
     *            <ul>
     *            <li>{@link DataSource#getJndiLocation()} is set to a unique path for the
     *            container.</li>
     *            <li>{@link DataSource#getDriverClass()} is an implementation of
     *            <code>java.sql.Driver</code></li>
     *            <li>{@link DataSource#getTransactionSupport()} is
     *            <code>TransactionSupport.XA_TRANSACTION</code></li>
     *            </ul>
     * @return configuration binding a container provided implementation of type
     *         <code>javax.sql.DataSource</code> to the JNDI path specified at
     *         {@link DataSource#getJndiLocation()}.
     */
    public abstract String buildEntryForDriverConfiguredDataSourceWithXaTx(DataSource ds);

    /**
     * @param ds the {@link DataSource} with the following state:
     *            <ul>
     *            <li>{@link DataSource#getJndiLocation()} is set to a unique path for the
     *            container.</li>
     *            <li>{@link DataSource#getDriverClass()} is an implementation of
     *            <code>java.sql.Driver</code></li>
     *            <li>{@link DataSource#getTransactionSupport()} is
     *            <code>TransactionSupport.LOCAL_TRANSACTION</code></li>
     *            </ul>
     * @return configuration binding a container provided implementation of type
     *         <code>javax.sql.DataSource</code> to the JNDI path specified at
     *         {@link DataSource#getJndiLocation()}.
     */
    public abstract String buildEntryForDriverConfiguredDataSourceWithLocalTx(DataSource ds);

    /**
     * @param ds the {@link DataSource} with the following state:
     *            <ul>
     *            <li>{@link DataSource#getJndiLocation()} is set to a unique path for the
     *            container.</li>
     *            <li>{@link DataSource#getDriverClass()} is an implementation of
     *            <code>java.sql.Driver</code></li>
     *            <li>{@link DataSource#getTransactionSupport()} is
     *            <code>TransactionSupport.NO_TRANSACTION</code></li>
     *            </ul>
     * @return configuration binding a container provided implementation of type
     *         <code>javax.sql.DataSource</code> to the JNDI path specified at
     *         {@link DataSource#getJndiLocation()}.
     */
    public abstract String buildEntryForDriverConfiguredDataSourceWithNoTx(DataSource ds);

    /**
     * @param ds the {@link DataSource} with the following state:
     *            <ul>
     *            <li>{@link DataSource#getJndiLocation()} is set to a unique path for the
     *            container.</li>
     *            <li>{@link DataSource#getConnectionType()} is <code>javax.sql.XADataSource</code>
     *            </li>
     *            <li>{@link DataSource#getDriverClass()} is an implementation of
     *            <code>javax.sql.XADataSource</code></li>
     *            </ul>
     * @return configuration binding a container provided implementation of type
     *         <code>javax.sql.DataSource</code> to the JNDI path specified at
     *         {@link DataSource#getJndiLocation()}. This container will provide XA support through
     *         the third party implementation specified at {@link DataSource#getDriverClass()}.
     */
    public abstract String buildConfigurationEntryForXADataSourceConfiguredDataSource(
        DataSource ds);
}
