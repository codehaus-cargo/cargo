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
package org.codehaus.cargo.container.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherException;
import org.codehaus.cargo.util.log.Logger;

/**
 * A JVM launcher stub.
 */
public class JvmLauncherStub implements JvmLauncher
{

    /**
     * JVM command.
     */
    private String jvm;

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
    @Override
    public void setWorkingDirectory(File workingDirectory)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJvm(String command)
    {
        if (command == null || command.isEmpty())
        {
            return;
        }
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
    @Override
    public void addJvmArgument(File file)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addJvmArguments(String... values)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addJvmArgumentLine(String line)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
    public String getClasspath()
    {
        return String.join(String.valueOf(File.pathSeparatorChar), this.classpathEntries);
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
    @Override
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
    @Override
    public void setEnvironmentVariable(String name, String value)
    {
        if (name != null && !name.isEmpty())
        {
            environmentVariables.put(name, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public void setJarFile(File jarFile)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMainClass(String mainClass)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAppArgument(File file)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAppArguments(String... values)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAppArgumentLine(String line)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputFile(File outputFile)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppendOutput(boolean appendOutput)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputLogger(Logger outputLogger, String category)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommandLine()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.jvm);
        sb.append(" -classpath").append(getClasspath());
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(long millis)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawn(boolean spawn)
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws JvmLauncherException
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int execute() throws JvmLauncherException
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill()
    {
        // Nothing
    }

}
