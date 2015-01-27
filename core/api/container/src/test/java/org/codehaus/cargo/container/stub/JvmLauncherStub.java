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
package org.codehaus.cargo.container.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.util.JavaEnvUtils;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherException;

/**
 * A JVM launcher stub.
 * 
 * @version $Id$
 */
public class JvmLauncherStub implements JvmLauncher
{

    /**
     * JVM command.
     */
    private String jvm = JavaEnvUtils.getJreExecutable("java");

    /**
     * Classpath entries.
     */
    private List<String> classpathEntries = new ArrayList<String>();

    /**
     * System properties.
     */
    private Properties systemProperties = new Properties();

    /**
     * Environment variables.
     */
    private final Map<String, String> environmentVariables = new HashMap<String, String>();

    /**
     * {@inheritDoc}
     */
    public void setWorkingDirectory(File workingDirectory)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setJvm(String command)
    {
        this.jvm = command;
    }

    /**
     * @return The configured JVM command.
     */
    public String getJvm()
    {
        return this.jvm;
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArgument(File file)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArguments(String... values)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArgumentLine(String line)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addClasspathEntries(String... paths)
    {
        if (paths != null)
        {
            Collections.addAll(this.classpathEntries, paths);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addClasspathEntries(File... paths)
    {
        if (paths != null)
        {
            for (File path : paths)
            {
                this.classpathEntries.add(path.getAbsolutePath());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getClasspath()
    {
        StringBuilder buffer = new StringBuilder(1024);
        for (String entry : this.classpathEntries)
        {
            if (buffer.length() > 0)
            {
                buffer.append(File.pathSeparatorChar);
            }
            buffer.append(entry);
        }
        return buffer.toString();
    }

    /**
     * @return The configured classpath entries.
     */
    public List<String> getClasspathEntries()
    {
        return this.classpathEntries;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemProperty(String name, String value)
    {
        this.systemProperties.setProperty(name, value);
    }

    /**
     * @return The configured system properties.
     */
    public Properties getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnvironmentVariable(String name, String value)
    {
        if (name != null && name.length() > 0)
        {
            environmentVariables.put(name, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getEnvironmentVariable(String name)
    {
        String value = environmentVariables.get(name);
        if (value == null)
        {
            value = System.getenv(name);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setJarFile(File jarFile)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setMainClass(String mainClass)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArgument(File file)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArguments(String... values)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArgumentLine(String line)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFile(File outputFile)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setAppendOutput(boolean appendOutput)
    {
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandLine()
    {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(this.jvm);
        buffer.append(" -classpath").append(getClasspath());
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void setTimeout(long millis)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setSpawn(boolean spawn)
    {

    }

    /**
     * {@inheritDoc}
     */
    public void start() throws JvmLauncherException
    {
    }

    /**
     * {@inheritDoc}
     */
    public int execute() throws JvmLauncherException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void kill()
    {
    }

}
