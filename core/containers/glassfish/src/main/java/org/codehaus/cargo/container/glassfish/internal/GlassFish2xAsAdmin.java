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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Implements an GlassFish 2.x AsAdmin command.
 */
public class GlassFish2xAsAdmin extends AbstractAsAdmin
{

    /**
     * GlassFish home, where the AsAdmin executable can be found.
     */
    private String home;

    /**
     * Saves the GlassFish home directory and the timeout.
     * 
     * @param home GlassFish home directory.
     */
    public GlassFish2xAsAdmin(String home)
    {
        super();

        if (home == null)
        {
            throw new CargoException("GlassFish home directory is not set");
        }

        this.home = home;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int invokeAsAdmin(boolean async, JvmLauncher java, String[] args)
    {
        File home = new File(this.home);
        if (!home.isDirectory())
        {
            throw new CargoException("GlassFish home directory is not valid: " + home);
        }

        java.setSystemProperty("derby.storage.fileSyncTransactionLog", "true");
        java.setSystemProperty("com.sun.aas.instanceName", "server");
        java.setSystemProperty("java.library.path", new File(home, "lib").getAbsolutePath());
        java.setSystemProperty("com.sun.aas.configRoot",
            new File(home, "config").getAbsolutePath());
        java.setSystemProperty("java.endorsed.dirs", new File(home, "endorsed").getAbsolutePath());
        java.setSystemProperty("com.sun.aas.processLauncher", "SE");
        java.setSystemProperty("com.sun.appserv.admin.pluggable.features",
            "com.sun.enterprise.ee.admin.pluggable.EEClientPluggableFeatureImpl");

        java.addClasspathEntries(
            new File(home, "javadb/lib/derby.jar"),
            new File(home, "jbi/lib/jbi-admin-cli.jar"),
            new File(home, "jbi/lib/jbi-admin-common.jar"),
            new File(home, "lib"),
            new File(home, "lib/comms-appserv-rt.jar"),
            new File(home, "lib/comms-appserv-api.jar"),
            new File(home, "lib/appserv-rt.jar"),
            new File(home, "lib/appserv-ext.jar"),
            new File(home, "lib/javaee.jar"),
            new File(home, "lib/appserv-se.jar"),
            new File(home, "lib/comms-appserv-admin-cli.jar"),
            new File(home, "lib/admin-cli.jar"),
            new File(home, "lib/appserv-admin.jar"),
            new File(home, "lib/commons-launcher.jar"),
            new File(home, "lib/install/applications/jmsra/imqjmsra.jar"));

        java.setMainClass("com.sun.enterprise.cli.framework.CLIMain");

        java.addAppArguments(args);

        if (async)
        {
            java.start();
            return 0;
        }
        else
        {
            File asAdminOutput;
            try
            {
                asAdminOutput = File.createTempFile("cargo-glassfish-asadmin-", ".txt");
            }
            catch (IOException e)
            {
                throw new CargoException("Cannot create asadmin output file", e);
            }
            java.setOutputFile(asAdminOutput);
            int exitCode = java.execute();
            if (exitCode != 0 && exitCode != 1)
            {
                final StringBuilder arguments = new StringBuilder();
                for (final String arg : args)
                {
                    arguments.append(arg);
                    arguments.append(' ');
                }
                if (args.length > 0)
                {
                    arguments.deleteCharAt(arguments.length() - 1);
                }
                FileHandler fh = new DefaultFileHandler();
                String output = fh.readTextFile(asAdminOutput.getPath(), StandardCharsets.UTF_8);
                asAdminOutput.delete();
                throw new CargoException("GlassFish admin command with args (" + arguments
                    + ") failed: asadmin exited " + exitCode + ": " + output);
            }
            asAdminOutput.delete();
            return exitCode;
        }
    }

}
