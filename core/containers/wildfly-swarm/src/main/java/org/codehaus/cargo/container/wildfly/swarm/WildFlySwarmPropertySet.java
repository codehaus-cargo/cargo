/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm;

/**
 * WildFly Swarm property set.
 */
public interface WildFlySwarmPropertySet
{
    /**
     * The project name used for a project descriptor file.
     */
    String SWARM_PROJECT_NAME = "cargo.swarm.project.name";

    /**
     * URL of an application deployed with WildFly Swarm.
     */
    String SWARM_APPLICATION_URL = "cargo.swarm.ping.url";

    /**
     * Property telling if Hollow Swarm mode is enabled. In this mode WildFly Swarm allows
     * deployments.
     */
    String SWARM_HOLLOW_ENABLED = "cargo.swarm.hollowswarm";
}
