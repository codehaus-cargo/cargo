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
package org.codehaus.cargo.daemon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Selfcontained main bootstrap class to launch the
 * webcontainer tjws and run the daemon web application.
 */
public final class Main
{
    /**
     * The logger for errors during startup.
     * Cannot use cargo classes because they are not loaded yet.
     */
    private static final PrintStream LOGGER = System.err;

    /**
     * Constructor not allowed for utility classes.
     */
    private Main()
    {
        return;
    }

    /**
     * @return the daemon home directory
     */
    private static File getHomeDirectory()
    {
        boolean gotHomeViaProperty = true;
        String home = System.getProperty("daemon.home");
        if (home == null)
        {
            gotHomeViaProperty = false;
            home = System.getProperty("user.home");
        }
        File homeDir = null;

        if (home == null)
        {
            homeDir = new File(System.getProperty("java.io.tmpdir"), "cargo");
        }
        else if (!gotHomeViaProperty)
        {
            homeDir = new File(home, ".cargo");
        }
        else
        {
            homeDir = new File(home);
        }

        return homeDir;
    }

    /**
     * Gets the daemon destination webapp directory.
     * 
     * @param homeDirectory The daemon home directory.
     * @return the webapp directory.
     */
    private static File getWebAppDirectory(File homeDirectory)
    {
        return new File(homeDirectory, "daemon");
    }

    /**
     * Gets the daemon destination war directory.
     * @param webAppDirectory The webapp directory
     * @return the war directory
     */
    private static File getWarDirectory(File webAppDirectory)
    {
        return new File(new File(webAppDirectory, ".web-apps-target"), "daemon");
    }

    /**
     * Gets the server directory.
     * @param warDirectory The war directory.
     * @return the server directory.
     */
    private static File getServerDirectory(File warDirectory)
    {
        return new File(new File(warDirectory, "WEB-INF"), "tjws");
    }

    /**
     * Deletes a directory recursively.
     * 
     * @param file Path to directory to delete.
     * @throws IOException If io exception happens.
     */
    private static void deleteDirectory(File file) throws IOException
    {
        if (file.isDirectory())
        {
            File[] children = file.listFiles();
            if (children != null)
            {
                for (File child : children)
                {
                    deleteDirectory(child);
                }
            }
        }
        file.delete();
    }

    /**
     * Unpacks a jar.
     * 
     * @param jarFile Jar file path.
     * @param destDir Destination directory.
     * @throws IOException If io exception happens.
     */
    @SuppressWarnings("rawtypes")
    private static void unpack(String jarFile, File destDir) throws IOException
    {
        byte[] buffer = new byte[1024 * 1024];
        int length;
        destDir.mkdirs();

        try (JarFile jar = new JarFile(jarFile))
        {
            Enumeration enumeration = jar.entries();
            while (enumeration.hasMoreElements())
            {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                // Security note: Zip Slip cannot happen here, this is our own ZIP
                File destFile = new File(destDir, jarEntry.getName());
                if (jarEntry.isDirectory())
                {
                    destFile.mkdirs();
                    continue;
                }
                else if (!destFile.getParentFile().isDirectory())
                {
                    destFile.getParentFile().mkdirs();
                }

                try (InputStream is = jar.getInputStream(jarEntry);
                    FileOutputStream os = new FileOutputStream(destFile))
                {
                    while ((length = is.read(buffer)) >= 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
            }
        }
    }

    /**
     * Updates the last-modified timestamp of the webapp directory to that of the daemon war.
     * 
     * @param destDir The webapp directory.
     * @param warFilePath The war filepath.
     */
    private static void updateLastModified(File destDir, String warFilePath)
    {
        File warFile = new File(warFilePath);

        destDir.setLastModified(warFile.lastModified());
    }

    /**
     * Checks if unpacking is needed.
     * 
     * @param webAppDirectory The webapp directory.
     * @param webServerDirectory The server directory.
     * @param warFilePath The daemon war filepath.
     * @return {@code true} if unpacking is needed, {@code false} otherwise.
     */
    private static boolean checkUnpack(File webAppDirectory, File webServerDirectory,
        String warFilePath)
    {
        if (!webAppDirectory.isDirectory() || !webServerDirectory.isDirectory())
        {
            return true;
        }

        File warFile = new File(warFilePath);
        return webAppDirectory.lastModified() != warFile.lastModified();
    }

    /**
     * Main entrypoint.
     * 
     * @param bootstrapArguments The bootstrap arguments.
     * @throws Exception If exception happens.
     */
    public static void main(String[] bootstrapArguments) throws Exception
    {
        List<String> serverArguments = new ArrayList<String>(bootstrapArguments.length);
        boolean logging = true;

        for (String argument : bootstrapArguments)
        {
            if (argument.equals("-nologging"))
            {
                logging = false;
            }
            else
            {
                serverArguments.add(argument);
            }
        }

        String warFilePath = new File(Main.class.getProtectionDomain()
            .getCodeSource().getLocation().toURI()).getAbsolutePath();

        File homeDirectory = getHomeDirectory();
        File webAppDirectory = getWebAppDirectory(homeDirectory);
        File warDirectory = getWarDirectory(webAppDirectory);
        File webServerDirectory = getServerDirectory(warDirectory);

        warDirectory.mkdirs();

        if (checkUnpack(webAppDirectory, webServerDirectory, warFilePath))
        {
            deleteDirectory(webAppDirectory);
            unpack(warFilePath, warDirectory);
            updateLastModified(webAppDirectory, warFilePath);
        }

        System.setProperty("tjws.webappdir", webAppDirectory.getAbsolutePath());
        System.setProperty("tjws.wardeploy.as-root", "daemon");
        serverArguments.add("-d");
        serverArguments.add(homeDirectory.getAbsolutePath());
        serverArguments.add("-nohup");

        if (!serverArguments.contains("-p"))
        {
            serverArguments.add("-p");
            serverArguments.add("18000");
        }

        if (!logging)
        {
            serverArguments.add("-out");
            serverArguments.add("Acme.Utils$DummyPrintStream");
            serverArguments.add("-err");
            serverArguments.add("Acme.Utils$DummyPrintStream");
        }

        if (System.getProperty("cargo.home") == null)
        {
            System.setProperty("cargo.home", homeDirectory.getAbsolutePath());
        }

        try
        {
            List<URL> classpathURLs = new ArrayList<URL>();

            classpathURLs.add(warDirectory.toURI().toURL());

            if (!webServerDirectory.isDirectory())
            {
                throw new FileNotFoundException(
                    "Directory " + webServerDirectory + " doesn't exist");
            }
            for (File file : webServerDirectory.listFiles())
            {
                if (file.getName().endsWith("jar"))
                {
                    classpathURLs.add(file.toURI().toURL());
                }
            }

            URLClassLoader serverClassloader = new URLClassLoader(
                classpathURLs.toArray(new URL[0]), Main.class.getClassLoader());
            // The WarRoller requires newer Java versions; hence check that one
            serverClassloader.loadClass("rogatkin.web.WarRoller");
            Class<?> mainClass = serverClassloader.loadClass("Acme.Serve.Main");
            Method main = mainClass.getMethod("main", new String[0].getClass());
            main.invoke(null, new Object[] {serverArguments.toArray(new String[0])});
        }
        catch (Throwable t)
        {
            LOGGER.println("Failed starting the Codehaus Cargo Daemon server: " + t.toString());
            for (StackTraceElement ste : t.getStackTrace())
            {
                LOGGER.println("\t" + ste.toString());
            }
        }
    }
}
