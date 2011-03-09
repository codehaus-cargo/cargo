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
package org.codehaus.cargo.container.spi.jvm;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;

/**
 * The default JVM launcher.
 * 
 * @version $Id$
 */
class DefaultJvmLauncher implements JvmLauncher
{

    /**
     * The Ant Java task being used to launch the JVM.
     */
    private final Java java;

    /**
     * Creates a new launcher using the specified Ant Java task.
     * 
     * @param java The Ant Java task to use for launching, must not be {@code null}.
     */
    public DefaultJvmLauncher(Java java)
    {
        this.java = java;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorkingDirectory(File workingDirectory)
    {
        this.java.setDir(workingDirectory);
    }

    /**
     * {@inheritDoc}
     */
    public void setJvm(String command)
    {
        this.java.setJvm(command);
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArgument(File file)
    {
        if (file != null)
        {
            this.java.createJvmarg().setFile(file);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArguments(String... values)
    {
        if (values != null)
        {
            for (String value : values)
            {
                this.java.createJvmarg().setValue(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addJvmArgumentLine(String line)
    {
        if (line != null)
        {
            this.java.createJvmarg().setLine(line);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addClasspathEntries(String... paths)
    {
        if (paths != null)
        {
            Path cp = this.java.createClasspath();
            for (String path : paths)
            {
                cp.createPathElement().setPath(path);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addClasspathEntries(File... paths)
    {
        if (paths != null)
        {
            Path cp = this.java.createClasspath();
            for (File path : paths)
            {
                cp.createPathElement().setLocation(path);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getClasspath()
    {
        Path p = this.java.getCommandLine().getClasspath();
        return (p != null) ? p.toString() : "";
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemProperty(String name, String value)
    {
        if (name != null && name.length() > 0)
        {
            Environment.Variable var = new Environment.Variable();
            var.setKey(name);
            var.setValue(value != null ? value : "");
            this.java.addSysproperty(var);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setJarFile(File jarFile)
    {
        if (jarFile != null)
        {
            this.java.setJar(jarFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setMainClass(String mainClass)
    {
        if (mainClass != null)
        {
            this.java.setClassname(mainClass);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArgument(File file)
    {
        if (file != null)
        {
            this.java.createArg().setFile(file);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArguments(String... values)
    {
        if (values != null)
        {
            for (String value : values)
            {
                this.java.createArg().setValue(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAppArgumentLine(String line)
    {
        if (line != null)
        {
            this.java.createArg().setLine(line);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputFile(File outputFile)
    {
        this.java.setOutput(outputFile);
    }

    /**
     * {@inheritDoc}
     */
    public void setAppendOutput(boolean appendOutput)
    {
        this.java.setAppend(appendOutput);
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandLine()
    {
        return this.java.getCommandLine().toString();
    }

    /**
     * {@inheritDoc}
     */
    public void setTimeout(long millis)
    {
        if (millis > 0)
        {
            this.java.setTimeout(Long.valueOf(millis));
        }
        else
        {
            this.java.setTimeout(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws JvmLauncherException
    {
        Thread catalinaRunner = new AntContainerExecutorThread(this.java);
        catalinaRunner.start();
    }

    /**
     * {@inheritDoc}
     */
    public int execute() throws JvmLauncherException
    {
        try
        {
            return this.java.executeJava();
        }
        catch (BuildException e)
        {
            throw new JvmLauncherException(e.getMessage(), e);
        }
    }

}
