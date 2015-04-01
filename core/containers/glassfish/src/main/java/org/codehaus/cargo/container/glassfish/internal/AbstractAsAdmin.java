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
import java.io.FileWriter;
import java.io.IOException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Implements an Glassfish AsAdmin command.
 * 
 */
public abstract class AbstractAsAdmin
{

    /**
     * Invokes asadmin using a Java container.
     * 
     * @param async Asynchronous invoke?
     * @param java JVM launcher.
     * @param args Invoke arguments.
     * @return The exit code from asadmin, always {@code 0} when using asynchronous invocation.
     * @throws CargoException If anything wrong happens.
     */
    public abstract int invokeAsAdmin(boolean async, JvmLauncher java, String[] args)
        throws CargoException;

    /**
     * Creates and returns the password file that contains admin's password.
     * 
     * @param configuration local configuration.
     * @return The password file that contains admin's password.
     */
    public static File getPasswordFile(LocalConfiguration configuration)
    {
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        if (password == null)
        {
            password = "";
        }

        try
        {
            File f = new File(configuration.getHome(), "password.properties");
            if (!f.exists())
            {
                configuration.getFileHandler().mkdirs(configuration.getHome());
                FileWriter w = new FileWriter(f);
                w.write("AS_ADMIN_PASSWORD=" + password + "\n");
                w.close();
            }
            return f;
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to create a password file", e);
        }
    }

}
