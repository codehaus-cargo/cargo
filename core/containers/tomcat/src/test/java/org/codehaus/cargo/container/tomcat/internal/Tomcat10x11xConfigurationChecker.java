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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Contains XML logic used to validate the XML output of a Tomcat 10.x onwards Jakarta EE
 * configuration.
 */
public class Tomcat10x11xConfigurationChecker extends Tomcat8x9xConfigurationChecker
{
    /**
     * {@inheritDoc} in Tomcat 10.x, all packages moved from Java EE to Jakarta EE.
     */
    @Override
    protected void checkConfigurationMatchesResource(String configuration, Resource resource)
        throws Exception
    {
        Resource jakartaEeResource = resource;
        for (String jakartaPackage : Tomcat10x11xConfigurationBuilder.JAKARTA_PACKAGES)
        {
            if (resource.getType().startsWith("javax." + jakartaPackage))
            {
                jakartaEeResource =
                    new Resource(resource.getName(),
                        resource.getType().replace("javax.", "jakarta."));
                jakartaEeResource.setClassName(resource.getClassName());
                jakartaEeResource.setId(resource.getId());
                jakartaEeResource.setParameters(resource.getParameters());
                break;
            }
        }
        super.checkConfigurationMatchesResource(configuration, jakartaEeResource);
    }
}
