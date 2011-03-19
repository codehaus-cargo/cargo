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
package org.codehaus.cargo.container.spi.configuration;

/**
 * Container implementations of {@link org.codehaus.cargo.container.configuration.Configuration}
 * must also implement this interface. This is an SPI interface and shouldn't be used by end users.
 * 
 * @version $Id$
 */
public interface ContainerConfiguration
{
    /**
     * Verify that the configuration is valid. The checks to be performed may vay whether this is
     * standalone or existing configuration. This method should also be used to verify that the
     * configuration properties specified by the user are valid and that the required ones are set.
     */
    void verify();
}
