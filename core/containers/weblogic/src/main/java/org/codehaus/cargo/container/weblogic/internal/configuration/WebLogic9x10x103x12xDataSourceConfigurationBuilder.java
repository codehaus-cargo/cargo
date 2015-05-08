/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic.internal.configuration;

import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xConfigurationBuilder;

/**
 * Create WLST script for adding DataSource to Weblogic domain.
 */
public class WebLogic9x10x103x12xDataSourceConfigurationBuilder extends
    WebLogic9x10x103x12xConfigurationBuilder
{
    /**
     * Sets the server name that the resources this creates are bound to.
     *
     * @param configuration containing needed server name.
     */
    public WebLogic9x10x103x12xDataSourceConfigurationBuilder(LocalConfiguration configuration)
    {
        super(configuration.getPropertyValue(WebLogicPropertySet.SERVER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String configureDataSourceWithImplementationClass(DataSource ds, String className)
    {
        String newline = System.getProperty("line.separator");

        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("create('%s','JDBCSystemResource')", ds.getId()));
        buffer.append(newline);
        buffer.append(String.format("cd('JDBCSystemResource/%s/JdbcResource/%s')", ds.getId(),
            ds.getId()));
        buffer.append(newline);
        buffer.append("create('myJdbcDriverParams','JDBCDriverParams')");
        buffer.append(newline);
        buffer.append("cd('JDBCDriverParams/NO_NAME_0')");
        buffer.append(newline);
        buffer.append(String.format("set('DriverName','%s')", className));

        if (ds.getUrl() != null)
        {
            buffer.append(newline);
            buffer.append(String.format("set('URL','%s')", ds.getUrl()));
        }

        if (ds.getPassword() != null)
        {
            buffer.append(newline);
            buffer.append(String.format("set('PasswordEncrypted', '%s')", ds.getPassword()));
        }

        buffer.append(newline);
        buffer.append("create('myProps','Properties')");
        buffer.append(newline);
        buffer.append("cd('Properties/NO_NAME_0')");
        buffer.append(newline);
        buffer.append("create('user', 'Property')");
        buffer.append(newline);
        buffer.append("cd('Property/user')");
        buffer.append(newline);
        buffer.append(String.format("cmo.setValue('%s')", ds.getUsername()));

        Set<Entry<Object, Object>> driverProperties = ds.getConnectionProperties().entrySet();
        for (Entry<Object, Object> driverProperty : driverProperties)
        {
            String name = driverProperty.getKey().toString();
            String value = driverProperty.getValue().toString();
            buffer.append(newline);
            buffer.append("cd('../..')");
            buffer.append(newline);
            buffer.append(String.format("create('%s', 'Property')", name));
            buffer.append(newline);
            buffer.append(String.format("cd('Property/%s')", name));
            buffer.append(newline);
            buffer.append(String.format("cmo.setValue('%s')", value));
        }

        buffer.append(newline);
        buffer.append(String.format("cd('/JDBCSystemResource/%s/JdbcResource/%s')", ds.getId(),
            ds.getId()));
        buffer.append(newline);
        buffer.append("create('myJdbcDataSourceParams','JDBCDataSourceParams')");
        buffer.append(newline);
        buffer.append("cd('JDBCDataSourceParams/NO_NAME_0')");
        buffer.append(newline);
        buffer.append(String.format("set('JNDIName','%s')", ds.getJndiLocation()));

        if (ds.getConnectionType().equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            buffer.append(newline);
            buffer.append("set('GlobalTransactionsProtocol','TwoPhaseCommit')");
        }
        else if (ds.getTransactionSupport().equals(TransactionSupport.XA_TRANSACTION))
        {
            buffer.append(newline);
            buffer.append("set('GlobalTransactionsProtocol','EmulateTwoPhaseCommit')");
        }
        else
        {
            buffer.append(newline);
            buffer.append("set('GlobalTransactionsProtocol','None')");
        }

        buffer.append(newline);
        buffer.append("cd('/')");
        buffer.append(newline);
        buffer.append(String.format("assign('JDBCSystemResource', '%s', 'Target', '%s')",
            ds.getId(), getServerName()));

        return buffer.toString();
    }
}
