/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.internal.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.codehaus.cargo.container.ContainerException;

/**
 * Set of common JDK utility methods.
 */
public final class JdkUtils
{
    /**
     * Ensures that this utility class cannot be instantiated.
     */
    private JdkUtils()
    {
    }

    /**
     * Returns the file containing the JDK tools (such as the compiler). This method must not be
     * called on Mac OSX as there is no tools.jar file on that platform (everything is included in
     * classes.jar).
     * 
     * @return The tools.jar file
     * @throws FileNotFoundException If the tools.jar file could not be found
     */
    public static File getToolsJar() throws FileNotFoundException
    {
        String javaHome = System.getProperty("java.home");
        File toolsJar = getToolsJar(javaHome);
        if (!toolsJar.isFile())
        {
            throw new FileNotFoundException(toolsJar.getAbsolutePath());
        }
        return toolsJar;
    }

    /**
     * Returns the file containing the JDK tools (such as the compiler) for the specified Java
     * installation. This method must not be called on Mac OSX as there is no {@code tools.jar} file
     * on that platform (everything is included in {@code classes.jar}).
     * 
     * @param javaHome The installation directory of the JRE/JDK for which to locate the JDK tools,
     *            must not be {@code null}.
     * @return The absolute (and possibly non-existent) path to the {@code tools.jar} file, never
     *         {@code null}.
     */
    public static File getToolsJar(String javaHome)
    {
        File jdkHome = new File(javaHome).getAbsoluteFile();
        if (jdkHome.getName().equals("jre"))
        {
            jdkHome = jdkHome.getParentFile();
        }
        File libDir = new File(jdkHome, "lib");
        File toolsJar = new File(libDir, "tools.jar");
        return toolsJar;
    }

    /**
     * Is the user running on a Macintosh OS X system? Heuristic derived from <a
     * href="http://developer.apple.com/technotes/tn/tn2042.html#Section0_1">Apple Tech Note
     * 2042</a>.
     * 
     * @return true if the user's system is determined to be Mac OS X.
     */
    public static boolean isOSX()
    {
        return System.getProperty("mrj.version") != null;
    }

    /**
     * Is the user running on a Windows system?
     * 
     * @return true if the user's system is determined to be Windows.
     */
    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Get the major Java version.
     * 
     * @return Major Java version.
     */
    public static int getMajorJavaVersion()
    {
        return parseMajorJavaVersion(System.getProperty("java.version"));
    }

    /**
     * Parse major Java version from a Java version string.
     * 
     * @param version Java version string.
     * @return Major Java version.
     */
    public static int parseMajorJavaVersion(String version)
    {
        String jvmVersion = version.replaceAll("^\"?(1\\.)?([0-9]+).*", "$2");
        return Integer.parseInt(jvmVersion);
    }

    /**
     * Pauses the current thread for the specified amount.
     * 
     * @param ms The time to sleep in milliseconds
     */
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            throw new ContainerException("Interruption during sleep", e);
        }
    }
}
