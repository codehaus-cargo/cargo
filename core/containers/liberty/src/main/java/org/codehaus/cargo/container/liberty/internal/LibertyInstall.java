/* 
* Copyright 2016 IBM Corp.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
package org.codehaus.cargo.container.liberty.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.internal.util.JdkUtils;

/**
 * This class encapsulate information about a Liberty install
 */
public class LibertyInstall
{
    /** Where Liberty is installed */
    private File installDir;
    /** The liberty usr dir */
    private File usrDir;
    /** JDKUtils */
    private JdkUtils utils = new JdkUtils();

    /**
     * Create the LibertyInstall for this local container
     * 
     * @param container the container to create it for
     */
    public LibertyInstall(InstalledLocalContainer container)
    {
        installDir = new File(container.getHome());
        usrDir = new File(container.getConfiguration().getHome());
    }

    /**
     * Work out the appropriate script to use based on platform
     * 
     * @param name
     *            the name of the script
     * @return the script to invoke as a file
     */
    private File getScript(String name)
    {
        String script = "bin/" + name;
        if (utils.isWindows())
        {
            script += ".bat";
        }

        return new File(installDir, script);
    }

    /**
     * @return The wlp.install.dir
     */
    public File getInstallDir()
    {
        return installDir;
    }

    /**
     * Locates the server.config.dir for the specified Liberty server
     * 
     * @param server
     *            the name of the server. If null 'defaultServer' is used
     * @return The server.config.dir
     */
    public File getServerDir(String server)
    {
        String serverName = server == null ? "defaultServer" : server;

        return new File(usrDir, "servers/" + serverName);
    }

    /**
     * Run the specified server command.
     * 
     * @param command The command to run
     * @return The process object wrapping the invoked process
     * @throws Exception if something goes wrong.
     */
    public Process runCommand(String command) throws Exception
    {
        return runCommand(command, new HashMap<String, String>());
    }
    
    /**
     * Run the specified server command.
     * 
     * @param command The command to run
     * @param inEnv the envrionment to use
     * @return The process object wrapping the invoked process
     * @throws Exception if something goes wrong.
     */
    public Process runCommand(String command, Map<String, String> inEnv) throws Exception
    {
        File scriptFile = getScript("server");
        
        Map<String, String> env = new HashMap<String, String>(inEnv);
        env.put("WLP_USER_DIR", usrDir.getAbsolutePath());

        if (scriptFile.exists())
        {
            ProcessBuilder builder = new ProcessBuilder();
            List<String> cmds = builder.command();
            if (utils.isWindows())
            {
                cmds.add("cmd");
                cmds.add("/c");
            } 
            else 
            {
                cmds.add("sh");
            }
            
            cmds.add(scriptFile.getAbsolutePath());
            cmds.add(command);
            builder.directory(installDir);
            builder.environment().putAll(env);
            return builder.start();
        }
        else
        {
            final StringBuilder builder = new StringBuilder("Liberty is not installed into ");
            builder.append(installDir);
            builder.append("\r\nFile in dir:\r\n");
            installDir.listFiles(new FileFilter()
            {
                
                public boolean accept(File pathname)
                {
                    builder.append(pathname.getName());
                    builder.append("\r\n");
                    return false;
                }
            });
            
            throw new FileNotFoundException(builder.toString());
        }
    }

}
