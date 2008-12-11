/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004 Vincent Massol.
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
 * 
 * @version $Id$
 */
public class JdkUtils
{
    /**
     * Returns the file containing the JDK tools (such as the compiler). This
     * method must not be called on Mac OSX as there is no tools.jar file on
     * that platform (everything is included in classes.jar).
     * 
     * @return The tools.jar file
     * @throws FileNotFoundException If the tools.jar file could not be found
     */
    public final File getToolsJar() throws FileNotFoundException
    {
        String javaHome = System.getProperty("java.home");
        if (javaHome.indexOf("jre") > 0)
        {
            javaHome = new File(javaHome).getParent();
        }
        File libDir = new File(javaHome, "lib");
        File toolsJar = new File(libDir, "tools.jar");
        if (!toolsJar.isFile())
        {
            throw new FileNotFoundException(toolsJar.getAbsolutePath());
        }
        return toolsJar;
    }   

    /**
     * Is the user running on a Macintosh OS X system?  Heuristic derived from
     * <a href="http://developer.apple.com/technotes/tn/tn2042.html#Section0_1">
     * Apple Tech Note 2042</a>.
     *
     * @return true if the user's system is determined to be Mac OS X.
     */
    public final boolean isOSX()
    {
        return (System.getProperty("mrj.version") != null);
    }    

    /**
     * Pauses the current thread for the specified amount.
     *
     * @param ms The time to sleep in milliseconds
     */
    public void sleep(long ms)
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
