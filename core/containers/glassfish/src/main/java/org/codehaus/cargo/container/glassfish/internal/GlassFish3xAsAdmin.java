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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.File;

import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Implements an Glassfish 3.x AsAdmin command.
 * 
 * @version $Id$
 */
public class GlassFish3xAsAdmin extends AbstractAsAdmin
{

    /**
     * GlassFish home, where the AsAdmin executable can be found.
     */
    private String home;

    /**
     * Saves the GlassFish home directory.
     * 
     * @param home GlassFish home directory.
     */
    public GlassFish3xAsAdmin(String home)
    {
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

        File adminCli = new File(home, "glassfish/modules/admin-cli.jar");
        if (!adminCli.isFile())
        {
            throw new CargoException("Cannot find the GlassFish admin CLI JAR: "
                + adminCli.getName());
        }

        java.setJarFile(adminCli);
        java.addAppArguments(args);

        int exitCode = 0;
        if (async)
        {
            java.start();
        }
        else
        {
            exitCode = java.execute();

            if (exitCode != 0 && exitCode != 1)
            {
                throw new CargoException("Command " + args[0] + " failed: asadmin exited "
                    + exitCode);
            }
        }
        return exitCode;
    }

}
