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
package org.codehaus.cargo.container.property;

/**
 * Defines all general container properties valid for JSR88-compliant containers.
 * 
 * @see org.codehaus.cargo.container.jsr88.GenericJSR88Container
 * 
 * @version $Id$
 */
public interface JSR88PropertySet
{

    /**
     * Username to use when acquiring a {@link javax.enterprise.deploy.spi.DeploymentManager}.
     */
    String USERNAME = "cargo.jsr88.user";

    /**
     * Password to use when acquiring a {@link javax.enterprise.deploy.spi.DeploymentManager}.
     */
    String PASSWORD = "cargo.jsr88.password";

    /**
     * JAR file to load the deployment tool from.
     */
    String DEPLOYTOOL_JAR = "cargo.jsr88.deploytooljar";

    /**
     * Extra classpath necessary for the deployment tool (not the container itself!) to function.
     * Semicolon-separated.
     */
    String DEPLOYTOOL_CLASSPATH = "cargo.jsr88.deploytoolclasspath";
}
