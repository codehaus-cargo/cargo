/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.configuration.builder;

/**
 * Marker interface to ensure all methods of <code>ConfigurationBuilder</code> are tested through
 * Configuration.configure(Container).
 * 
 * @see ConfigurationBuilderTests
 * @version $Id$
 */
public interface LocalConfigurationWithConfigurationBuilderTests
{

    /**
     * Test the configuration and creation of a datasource.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesDataSource() throws Exception;

    /**
     * Test the configuration and creation of two datasources.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesTwoDataSources() throws Exception;

    /**
     * Test the configuration and creation of a datasource with driver-configured local transaction
     * support.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception;

    /**
     * Test the configuration and creation of a datasource with driver-configured XA transaction
     * support.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport()
        throws Exception;

    /**
     * Test the configuration and creation of an XA datasource.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesDataSourceForXADataSourceConfiguredDataSource() throws Exception;

    /**
     * Test the configuration and creation of two datasources using properties.
     * @throws Exception If anything goes wrong.
     */
    void testConfigureCreatesTwoResourcesViaProperties() throws Exception;

}
