/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.sample.java;

import org.junit.jupiter.api.extension.ExtensionContext;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;

/**
 * Abstract test case for testing WARs on a container.
 */
public abstract class AbstractStandaloneLocalContainerTestCase extends AbstractCargoTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public AbstractStandaloneLocalContainerTestCase()
    {
        this.addValidator(new HasStandaloneConfigurationValidator());
        this.addValidator(new IsLocalContainerValidator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp(
        CargoTestCase.CargoTestcaseInvocationContext cargoContext, ExtensionContext testContext)
        throws Exception
    {
        super.setUp(cargoContext, testContext);
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }
}
