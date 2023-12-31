/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.jboss.JBossPropertySet;

/**
 * Gathers all WildFly properties.
 */
public interface WildFlyPropertySet extends JBossPropertySet
{
    /**
     * External CLI script file paths.<br>
     * Used for custom configuration of WildFly container in offline mode.<br>
     * <br>
     * Example usage:<br>
     * setProperty("cargo.wildfly.script.cli.offline.journal",
     *              "target/test-classes/wildfly/wildfly10/jms-journal.cli")<br>
     */
    String CLI_OFFLINE_SCRIPT = "cargo.wildfly.script.cli.offline";

    /**
     * External CLI script file paths.<br>
     * Used for custom configuration of WildFly container in embedded mode.<br>
     * <br>
     * Example usage:<br>
     * setProperty("cargo.wildfly.script.cli.embedded.journal",
     *              "target/test-classes/wildfly/wildfly10/jms-journal.cli")<br>
     */
    String CLI_EMBEDDED_SCRIPT = "cargo.wildfly.script.cli.embedded";
}
