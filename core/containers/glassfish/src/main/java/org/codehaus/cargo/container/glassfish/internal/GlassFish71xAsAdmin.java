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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Implements an GlassFish 7.1.x AsAdmin command.
 */
public class GlassFish71xAsAdmin extends GlassFish3xAsAdmin
{

    /**
     * AsAdmin JARs, as listed in the <code>config/asenv.conf</code> file.
     */
    private static final String[] GLASSFISH_ASADMIN_JARS = new String[]
    {
        "appserver-cli.jar",
        "modules/admin-util.jar",
        "modules/backup.jar",
        "modules/cluster-common.jar",
        "modules/cluster-ssh.jar",
        "modules/config-api.jar",
        "modules/config-types.jar",
        "modules/common-util.jar",
        "modules/glassfish-api.jar",
        "modules/hk2.jar",
        "modules/hk2-config-generator.jar",
        "modules/internal-api.jar",
        "modules/jackson-core.jar",
        "modules/jakarta.activation-api.jar",
        "modules/jakarta.validation-api.jar",
        "modules/jakarta.xml.bind-api.jar",
        "modules/jaxb-osgi.jar",
        "modules/jettison.jar",
        "modules/jsch.jar",
        "modules/launcher.jar",
        "modules/mimepull.jar"
    };

    /**
     * Saves the GlassFish home directory.
     * 
     * @param home GlassFish home directory.
     */
    public GlassFish71xAsAdmin(String home)
    {
        super(home);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeAsAdminJavaCall(File home, JvmLauncher java)
    {
        File glassfish = new File(home, "glassfish");
        File adminCli = new File(glassfish, "modules/admin-cli.jar");
        if (!adminCli.isFile())
        {
            adminCli = new File(glassfish, "admin-cli.jar");
        }
        if (!adminCli.isFile())
        {
            throw new CargoException(
                "Cannot find the GlassFish admin CLI JAR: " + adminCli);
        }

        boolean hasMain;
        try (JarFile asadmin = new JarFile(adminCli))
        {
            if (asadmin.getManifest().getMainAttributes().getValue("Main-Class") != null)
            {
                hasMain = true;
            }
            else
            {
                hasMain = false;
            }
        }
        catch (IOException e)
        {
            throw new CargoException(
                "Cannot read the GlassFish admin CLI JAR manifest: " + adminCli);
        }
        if (hasMain)
        {
            java.setJarFile(adminCli);
        }
        else
        {
            java.addJvmArguments(
                "--module-path", new File(glassfish, "lib/bootstrap").getAbsolutePath(),
                "--add-modules", "ALL-MODULE-PATH");

            java.addClasspathEntries(adminCli);
            File asadmin = new File(glassfish, "lib/asadmin");
            if (!asadmin.isDirectory())
            {
                throw new CargoException(
                    "Cannot find the GlassFish admin library directory: " + asadmin);
            }
            for (File glassfishAsAdminJar : asadmin.listFiles())
            {
                if (glassfishAsAdminJar.getName().endsWith(".jar"))
                {
                    java.addClasspathEntries(glassfishAsAdminJar);
                }
            }
            for (String glassfishAsAdminJar : GlassFish71xAsAdmin.GLASSFISH_ASADMIN_JARS)
            {
                java.addClasspathEntries(new File(glassfish, glassfishAsAdminJar));
            }

            java.setMainClass("org.glassfish.admin.cli.AsadminMain");
        }
    }
}
