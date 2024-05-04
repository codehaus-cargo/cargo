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
package org.codehaus.cargo.container.liberty;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.liberty.internal.LibertyInstall;
import org.codehaus.cargo.container.liberty.internal.LibertyStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.liberty.internal.ServerConfigUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * This class configures the WebSphere Liberty install to run cargo.
 */
public class LibertyStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * The configuration capability for WebSphere Liberty
     */
    private static final ConfigurationCapability CAPABILITY =
        new LibertyStandaloneLocalConfigurationCapability();

    /**
     * Configures Liberty at the specified install
     * 
     * @param dir the directory where Liberty is installed.
     */
    public LibertyStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * @return the server configuration capability
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * Configure the WebSphere Liberty server
     * 
     * @param container configure the capability
     * @throws Exception if something goes wrong
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        LibertyInstall install = new LibertyInstall((InstalledLocalContainer) container);
        File serverDir = install.getServerDir(null);

        // CARGO-1468: Websphere Liberty server does not deploy the war, if there were an old
        // version of the war before. Delete the old server environment to avoid such issues.
        if (serverDir.exists())
        {
            getLogger().info("Deleting old WebSphere Liberty configuration in [" + serverDir
                + "]...", this.getClass().getName());

            getFileHandler().delete(serverDir.getAbsolutePath());
        }

        Process p = install.runCommand("create");
        if (p.waitFor(container.getTimeout(), TimeUnit.MILLISECONDS))
        {
            int retVal = p.exitValue();
            if (retVal != 0)
            {
                throw new CargoException(
                    "The WebSphere Liberty configuration could not be created");
            }
        }
        else
        {
            p.destroyForcibly();
            throw new CargoException(
                "The WebSphere Liberty configuration creation command did not complete after "
                    + container.getTimeout() + " milliseconds");
        }

        File configDefaults = new File(serverDir, "configDropins/defaults");
        if (!configDefaults.mkdirs())
        {
            throw new Exception("There is no config dropins defaults dir to write to "
                + configDefaults);
        }

        writeKeystore(configDefaults);

        File configOverrides = new File(serverDir, "configDropins/overrides");
        if (!configOverrides.mkdirs())
        {
            throw new Exception("There is no config dropins overrides dir to write to "
                + configOverrides);
        }

        writeHttpEndpoint(container, configOverrides);
        writeJVMOptions(container, install);
        processUsers(container, configOverrides);
        writeDatasources(configOverrides);
        writeLibrary(container, configOverrides);

        List<Deployable> apps = getDeployables();
        LibertyInstalledLocalDeployer deployer = new LibertyInstalledLocalDeployer(container);
        for (Deployable dep : apps)
        {
            deployer.deploy(dep);
        }

        // Deploy the CPC (Cargo Ping Component) to the dropins directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(serverDir, "dropins/cargocpc.war"));
    }

    /**
     * Write a library for the extra classpath.
     * 
     * @param container the container
     * @param configOverrides the config dir
     * @throws IOException if an error occurs writing.
     */
    private void writeLibrary(LocalContainer container, File configOverrides) throws IOException
    {
        String[] cp = ((InstalledLocalContainer) container).getExtraClasspath();
        if (cp != null)
        {
            File libraryXML = new File(configOverrides, "cargo-library.xml");
            PrintStream out = ServerConfigUtils.open(libraryXML);
            ServerConfigUtils.writeLibrary(out, "cargoLib", cp);
            ServerConfigUtils.close(out);
        }
    }

    /**
     * Write any datasources.
     * 
     * @param configOverrides the directory to write into.
     * @throws IOException if something goes wrong.
     */
    private void writeDatasources(File configOverrides) throws IOException
    {
        List<DataSource> dataSources = getDataSources();
        if (dataSources != null)
        {
            File datasourcesXML = new File(configOverrides, "cargo-datasources.xml");
            PrintStream writer = ServerConfigUtils.open(datasourcesXML);

            for (DataSource ds : dataSources)
            {
                ServerConfigUtils.writeDataSource(writer, ds);
            }

            ServerConfigUtils.close(writer);
        }
    }

    /**
     * Processes the users property and writes the server xml
     * 
     * @param container the container
     * @param configOverrides the directory to write to
     * @throws IOException if an error occurs
     */
    private void processUsers(LocalContainer container, File configOverrides) throws IOException
    {
        List<User> userList = getUsers();
        if (userList != null)
        {
            Map<String, List<String>> groups = new HashMap<String, List<String>>();
            Map<String, String> users = new HashMap<String, String>();
            for (User u : userList)
            {
                users.put(u.getName(), u.getPassword());
                for (String group : u.getRoles())
                {
                    List<String> members = groups.get(group);
                    if (members == null)
                    {
                        members = new ArrayList<String>();
                        groups.put(group, members);
                    }
                    members.add(u.getName());
                }
            }
            writeUserRegistry(container, configOverrides, users, groups);
        }
    }

    /**
     * This method writes a user registry xml
     * 
     * @param container the container
     * @param configDir the config dir to write into
     * @param users the list of users
     * @param groups the groups and the group membership
     * @throws IOException if anything goes wrong.
     */
    private void writeUserRegistry(LocalContainer container, File configDir,
        Map<String, String> users, Map<String, List<String>> groups) throws IOException
    {
        File usersXML = new File(configDir, "cargo-users.xml");
        PrintStream writer = ServerConfigUtils.open(usersXML);

        writer.println("  <basicRegistry id=\"basic\">");
        for (Map.Entry<String, String> user : users.entrySet())
        {
            writer.print("    <user name=\"");
            writer.print(user.getKey());
            writer.print("\" password=\"");
            writer.print(user.getValue());
            writer.println("\"/>");
        }
        for (Map.Entry<String, List<String>> group : groups.entrySet())
        {
            writer.print("    <group name=\"");
            writer.print(group.getKey());
            writer.println("\">");
            for (String member : group.getValue())
            {
                writer.print("      <member name=\"");
                writer.print(member);
                writer.println("\"/>");
            }
            writer.println("    </group>");
        }
        writer.println("  </basicRegistry>");
        ServerConfigUtils.close(writer);
    }

    /**
     * This method writes the <code>jvm.options</code> file for the server
     * 
     * @param container the container
     * @param install the liberty install
     */
    private void writeJVMOptions(LocalContainer container, LibertyInstall install)
    {
        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            Map<String, String> sysProps = installedContainer.getSystemProperties();
            if (sysProps != null && !sysProps.isEmpty())
            {
                File serverDir = install.getServerDir(null);
                File jvmOptions = new File(serverDir, "jvm.options");
                try (PrintStream out = new PrintStream(jvmOptions))
                {
                    for (Map.Entry<String, String> entry
                        : installedContainer.getSystemProperties().entrySet())
                    {
                        out.print("-D");
                        out.print(entry.getKey());
                        out.print('=');
                        out.println(entry.getValue());
                    }
                }
                catch (IOException e)
                {
                    // TODO work out how to deal with
                }
            }
        }
    }

    /**
     * Write the <code>httpEndpoint</code> to set the port
     * 
     * @param container the container to get the port from
     * @param configOverrides the config overrides dir
     * @throws IOException if an error occurs
     */
    private void writeHttpEndpoint(LocalContainer container, File configOverrides)
        throws IOException
    {
        File portXML = new File(configOverrides, "cargo-httpendpoint.xml");
        PrintStream writer = ServerConfigUtils.open(portXML);

        LocalConfiguration config = container.getConfiguration();
        String port = config.getPropertyValue(ServletPropertySet.PORT);
        String protocol = config.getPropertyValue(GeneralPropertySet.PROTOCOL);

        writer.print("  <httpEndpoint id=\"defaultHttpEndpoint\" http");
        if ("https".equals(protocol))
        {
            writer.print('s');
        }
        writer.print("Port=\"");
        writer.print(port);
        String host = container.getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        if (host != null)
        {
            if ("0.0.0.0".equals(host))
            {
                host = "*";
            }
            writer.print("\" host=\"");
            writer.print(host);
        }
        writer.println("\">");
        writer.println("    <tcpOptions waitToAccept=\"true\" acceptThread=\"true\"/>");
        writer.println("  </httpEndpoint>");
        ServerConfigUtils.close(writer);
    }

    /**
     * Write the keystore password so the server will correctly start if the server is configured to
     * enable ssl. This writes to the defaults directory so other definitions of the password
     * override.
     * 
     * @param configDefaults the defaults dir.
     * @throws IOException if an error occurs
     */
    private void writeKeystore(File configDefaults) throws IOException
    {
        File keystoreXML = new File(configDefaults, "cargo-keystore.xml");
        PrintStream writer = ServerConfigUtils.open(keystoreXML);

        // TODO add default keystore id
        writer.print("  <keyStore password=\"");
        writer.print(genPassword());
        writer.println("\"/>");
        ServerConfigUtils.close(writer);
    }

    /**
     * Generates a unique 8 char password
     * 
     * @return the unique password
     */
    private static String genPassword()
    {
        SecureRandom rand = new SecureRandom();

        StringBuilder builder = new StringBuilder();

        while (builder.length() != 12)
        {
            int next = rand.nextInt();
            if (Character.isJavaIdentifierStart(next))
            {
                char c = (char) next;
                if (c == next)
                {
                    builder.append(c);
                }
            }
        }

        return builder.toString();
    }
}
