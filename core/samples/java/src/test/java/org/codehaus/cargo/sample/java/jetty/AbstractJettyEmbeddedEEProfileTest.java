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
package org.codehaus.cargo.sample.java.jetty;

import java.util.List;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.sample.java.AbstractWarTestCase;
import org.codehaus.cargo.sample.java.EmbeddedContainerClasspathResolver;
import org.codehaus.cargo.sample.java.validator.IsEmbeddedLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.SupportsPropertyValidator;

/**
 * Test for Jetty Embedded container supporting multiple EE profiles.
 */
public abstract class AbstractJettyEmbeddedEEProfileTest extends AbstractWarTestCase
{
    /**
     * Jetty Embedded specific embedded container classpath resolver.
     */
    private static class JettyEESpecificEmbeddedContainerClasspathResolver
        extends EmbeddedContainerClasspathResolver
    {
        /**
         * Parent test.
         */
        private AbstractJettyEmbeddedEEProfileTest test;

        /**
         * Jetty EE version.
         */
        private String jettyEeVersion;

        /**
         * Saves the parent test and Jetty EE version.
         * @param test Parent test.
         * @param jettyEeVersion Jetty EE version.
         */
        public JettyEESpecificEmbeddedContainerClasspathResolver(
            AbstractJettyEmbeddedEEProfileTest test, String jettyEeVersion)
        {
            this.test = test;
            this.jettyEeVersion = jettyEeVersion;
        }

        /**
         * Replaces the EE-version-specific JARs with the target Jetty EE version. {@inheritDoc}
         */
        @Override
        protected List<String> getDependencies(String containerId)
        {
            List<String> dependencies = super.getDependencies(containerId);
            String[] dependenciesCopy = dependencies.toArray(new String[dependencies.size()]);
            for (int i = 0; i < dependenciesCopy.length; i++)
            {
                if (dependenciesCopy[i].contains("ee10-"))
                {
                    dependencies.set(
                        i, dependenciesCopy[i].replace("ee10-", this.jettyEeVersion + "-"));
                }
            }
            return this.test.filterDependencies(dependencies);
        }
    }

    /**
     * Jetty Embedded specific embedded container classpath resolver.
     */
    private EmbeddedContainerClasspathResolver resolver;

    /**
     * Add the required validators and initializes the Jetty Embedded specific embedded container
     * classpath resolver.
     * @param jettyEeVersion Jetty EE version.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public AbstractJettyEmbeddedEEProfileTest(String jettyEeVersion)
    {
        this.addValidator(new IsEmbeddedLocalContainerValidator());
        this.addValidator(new StartsWithContainerValidator("jetty"));
        this.addValidator(new SupportsPropertyValidator(
            ConfigurationType.STANDALONE, JettyPropertySet.DEPLOYER_EE_VERSION));

        this.resolver =
            new JettyEESpecificEmbeddedContainerClasspathResolver(this, jettyEeVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EmbeddedContainerClasspathResolver getEmbeddedContainerClasspathResolver()
    {
        return this.resolver;
    }

    /**
     * Filters a given list of dependencies for the embedded classpath.
     * @param dependencies Dependencies to filter from.
     * @return Filtered (or updated) dependencies.
     */
    public abstract List<String> filterDependencies(List<String> dependencies);
}
