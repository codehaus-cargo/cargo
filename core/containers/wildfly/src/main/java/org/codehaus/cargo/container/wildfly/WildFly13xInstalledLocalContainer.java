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
package org.codehaus.cargo.container.wildfly;


import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * WildFly 13.x series container implementation.
 */
public class WildFly13xInstalledLocalContainer extends WildFly12xInstalledLocalContainer
{
    /**
     * WildFly 13.x series unique id.
     */
    public static final String ID = "wildfly13x";

    /**
     * {@inheritDoc}
     * @see WildFly12xInstalledLocalContainer#WildFly12xInstalledLocalContainer(LocalConfiguration)
     */
    public WildFly13xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultName()
    {
        return "WildFly 13.x (JBoss EAP 7.2)";
    }

    @Override
    protected void setProperties(JvmLauncher java)
    {
        super.setProperties(java);
        // CARGO-1473: For modular JVMs (from Java 11) there is additional configuration required.
        if (JdkUtils.getMajorJavaVersion() > 10)
        {
            java.addJvmArguments("--add-exports=java.base/sun.nio.ch=ALL-UNNAMED");
            java.addJvmArguments("--add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED");
            java.addJvmArguments("--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED");
            java.addJvmArguments("--add-modules=java.se");
        }
    }
}
