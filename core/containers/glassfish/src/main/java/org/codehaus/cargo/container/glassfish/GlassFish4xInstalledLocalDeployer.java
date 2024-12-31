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
package org.codehaus.cargo.container.glassfish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.internal.AbstractAsAdmin;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.User;

/**
 * GlassFish 4.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 */
public class GlassFish4xInstalledLocalDeployer extends GlassFish3xInstalledLocalDeployer
{
    /**
     * Calls parent constructor, which saves the container.
     * 
     * @param localContainer Container.
     */
    public GlassFish4xInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFileUser(final User user)
    {
        try
        {
            List<String> args = new ArrayList<String>();
            args.add("--interactive=false");
            args.add("--host");
            args.add(getLocalContainer().getConfiguration().getPropertyValue(
                    GeneralPropertySet.HOSTNAME));
            args.add("--port");
            args.add(getLocalContainer().getConfiguration().getPropertyValue(
                    GlassFishPropertySet.ADMIN_PORT));
            args.add("--user");
            args.add(getLocalContainer().getConfiguration().getPropertyValue(
                    RemotePropertySet.USERNAME));

            File tempPasswordFile = createPasswordProperties(user.getPassword());
            args.add("--passwordfile");
            args.add(tempPasswordFile.getCanonicalPath());

            args.add("create-file-user");

            StringBuilder groups = new StringBuilder();
            for (String role : user.getRoles())
            {
                if (groups.length() > 0)
                {
                    groups.append(':');
                }
                groups.append(role.trim());
            }
            args.add("--groups");
            args.add(groups.toString());
            args.add(user.getName().trim());
            new PrintWriter(System.out).println(args);
            this.getLocalContainer().invokeAsAdmin(false, args);

            tempPasswordFile.delete();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to create password file", e);
        }

    }

    /**
     * Creates a temporary file containing the existing AS_ADMIN_PASSWORD and
     * adds AS_ADMIN_USERPASSWORD property to contain the password of the user
     * in <code>code.servlet.users</code>.
     * 
     * @param password
     *            password
     * @return temporary file
     * @throws IOException
     *             I/O exception
     */
    private File createPasswordProperties(String password) throws IOException
    {
        Properties passwordProperties = new Properties();
        try (InputStream existing = new FileInputStream(
            AbstractAsAdmin.getPasswordFile(getLocalContainer().getConfiguration())))
        {
            passwordProperties.load(existing);
        }
        passwordProperties.setProperty("AS_ADMIN_USERPASSWORD", password);

        File tempFile = File.createTempFile("password", ".properties");
        try (OutputStream tempFileStream = new FileOutputStream(tempFile))
        {
            passwordProperties.store(tempFileStream, null);
        }
        return tempFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateDefaultPrincipalToRoleMapping()
    {
        List<String> args = new ArrayList<String>();
        this.addConnectOptions(args);
        args.add("set");
        args.add("configs.config.server-config.security-service."
                + "activate-default-principal-to-role-mapping=true");
        this.getLocalContainer().invokeAsAdmin(false, args);
    }

}
