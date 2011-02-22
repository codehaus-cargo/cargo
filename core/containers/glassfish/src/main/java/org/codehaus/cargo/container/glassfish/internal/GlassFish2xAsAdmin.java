/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.codehaus.cargo.util.CargoException;

/**
 * Implements an Glassfish 2.x AsAdmin command.
 * 
 * @version $Id$
 */
public class GlassFish2xAsAdmin extends AbstractAsAdmin
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
    public GlassFish2xAsAdmin(String home)
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
    public void invokeAsAdmin(boolean async, Java java, String[] args)
    {
        File home = new File(this.home);
        if (!home.isDirectory())
        {
            throw new CargoException("GlassFish home directory is not valid: " + home);
        }

        // TODO: don't launch the command, launch the JAR instead
        File exec;

        if (File.pathSeparatorChar == ';')
        {
            // on Windows
            exec = new File(home, "bin/asadmin.bat");
        }
        else
        {
            // on other systems
            exec = new File(home, "bin/asadmin");
        }

        if (!exec.exists())
        {
            throw new CargoException("asadmin command not found at " + exec);
        }

        // Make sure the extracted ZIP's executables are set as executable on Unix systems
        if (File.pathSeparatorChar == ':')
        {
            try
            {
                Process p = Runtime.getRuntime().exec("chmod +x " + exec.getAbsolutePath());
                p.waitFor();
            }
            catch (InterruptedException ignored)
            {
                // Ignored
            }
            catch (IOException ignored)
            {
                // Ignored
            }
        }

        List<String> cmds = new ArrayList<String>();
        cmds.add(exec.getAbsolutePath());
        cmds.addAll(Arrays.asList(args));

        try
        {
            Execute exe = new Execute(new PumpStreamHandler(), new ExecuteWatchdog(30 * 1000L));
            exe.setAntRun(new Project());
            String[] arguments = new String[cmds.size()];
            cmds.toArray(arguments);
            exe.setCommandline(arguments);
            if (async)
            {
                exe.spawn();
            }
            else
            {
                int exitCode = exe.execute();
                if (exitCode != 0 && exitCode != 1)
                {
                    // the first token is the command
                    throw new CargoException(cmds + " failed. asadmin exited " + exitCode);
                }
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to invoke asadmin", e);
        }
    }

}
