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
package org.codehaus.cargo.container.configuration.builder;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;

/**
 * <h2>Overview</h2> Implementors of <code>DataSourceConfigurationChecker</code> ensure basic
 * coverage of {@link ConfigurationBuilder} in a way that can be automated. It does this
 * by providing methods for every known configuration of a {@link DataSource}<br>
 * <h2>Usage</h2> Implementors will be called by tests who implement
 * {@link ConfigurationBuilderTests} or
 * {@link LocalConfigurationWithConfigurationBuilderTests}.<br>
 * <code>DataSourceConfigurationChecker</code> will be called from both of these contexts to help
 * developers isolate where configuration problems arise.
 * <ul>
 * <li> <code>ConfigurationBuilderTests</code> test the ability of a
 * ConfigurationBuilder to produce the correct configuration string based on the type of
 * <code>DataSource</code>.</li>
 * <li> <code>LocalConfigurationWithDataSourceSupportTests</code> test the ability of a
 * <code>LocalConfiguration</code> to use <code>ConfigurationBuilder</code> appropriately
 * such that those entries are written to the correct place in the correct file.</li>
 * </ul>
 * The methods mean what they say and all take two parameters:
 * <ol>
 * <li>configuration - this is what your implementation of {@link ConfigurationBuilder}
 * created for the context of this test.</li>
 * <li>dataSourceFixture - this is the inputs that should relate directly to the configuration
 * built.</li>
 * </ol>
 * <br>
 * Implementation Guidance:<br>
 * To implement one of these methods, you will need to scrub <code>configuration</code> for evidence
 * of being created correctly.<br>
 * For example, most implementations write XML. If dataSourceFixture.url was null for a check, you
 * should verify that the configuration doesn't include the word 'null', as such a thing would
 * certainly break a container. <br>
 * 
 * @version $Id$
 */
public interface ConfigurationChecker
{

    void checkConfigurationForDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception;

    void checkConfigurationForDriverConfiguredDataSourceWithLocalTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception;

    void checkConfigurationForDriverConfiguredDataSourceWithXaTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception;

    void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception;

    void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(String configuration,
        ResourceFixture resourceFixture) throws Exception;
    
    void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(String configuration,
        ResourceFixture resourceFixture) throws Exception;
    
    String insertConfigurationEntryIntoContext(String dataSourceEntry);
}
