/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.RedirectorElement;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.util.CargoException;

/**
 * The default JVM launcher.
 */
public class DefaultJvmLauncher implements JvmLauncher
{

    /**
     * The Ant Java task being used to launch the JVM.
     */
    private final Java java;

    /**
     * {@code true} to launch the JVM in spawn - separate thread independent of the initial thread
     */
    private boolean spawn;

    /**
     * List of extra environment variables. Ant's Java task doesn't offer a getter for the
     * environment variable, forcing us to keep track of them ourselves.
     */
    private final Map<String, String> environmentVariables = new HashMap<String, String>();

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
    @Override
    public void setWorkingDirectory(File workingDirectory)
    {
        this.java.setDir(workingDirectory);
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
        if (!new File(command).isFile())
        {
            throw new JvmLauncherException("JVM executable file [" + command + "] doesn't exist");
        }
        this.java.setJvm(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public String getClasspath()
    {
        Path p = this.java.getCommandLine().getClasspath();
        return (p != null) ? p.toString() : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemProperty(String name, String value)
    {
        if (name != null && !name.isEmpty())
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
    @Override
    public void setEnvironmentVariable(String name, String value)
    {
        if (name != null && !name.isEmpty())
        {
            Environment.Variable var = new Environment.Variable();
            var.setKey(name);
            var.setValue(value);
            java.addEnv(var);

            // separate bookkeeping, to enable getter method
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
        if (jarFile != null)
        {
            this.java.setJar(jarFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void setOutputFile(File outputFile)
    {
        this.java.setOutput(outputFile);
        this.java.setError(outputFile);

        // Don't create empty log files!
        // Output and error streams are redirected to same file. Creating of empty files
        // would overwrite content in log file written by the other output stream!
        // See CARGO-1423.
        RedirectorElement redirector = new RedirectorElement();
        redirector.setCreateEmptyFiles(false);
        this.java.addConfiguredRedirector(redirector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppendOutput(boolean appendOutput)
    {
        this.java.setAppend(appendOutput);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommandLine()
    {
        return this.java.getCommandLine().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill()
    {
        // Not supported by Ant Java Task
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(long millis)
    {
        if (millis > 0)
        {
            this.java.setTimeout(millis);
        }
        else
        {
            this.java.setTimeout(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawn(boolean spawn)
    {
        this.spawn = spawn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws JvmLauncherException
    {
        Thread runner = new AntContainerExecutorThread(this.java, this.spawn);
        runner.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * Turn a string command line to an array of arguments. The logic takes into account the spaces
     * between arguments, as well as single/double quotes for escaping arguments with spaces in
     * them.
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess)
    {
        if (toProcess == null || toProcess.isEmpty())
        {
            return new String[0];
        }

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"' ", true);
        final List<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens())
        {
            String nextTok = tok.nextToken();
            switch (state)
            {
                case inQuote:
                    if ("'".equals(nextTok))
                    {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    }
                    else
                    {
                        current.append(nextTok);
                    }
                    break;

                case inDoubleQuote:
                    if ("\"".equals(nextTok))
                    {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    }
                    else
                    {
                        current.append(nextTok);
                    }
                    break;

                default:
                    if ("'".equals(nextTok))
                    {
                        state = inQuote;
                    }
                    else if ("\"".equals(nextTok))
                    {
                        state = inDoubleQuote;
                    }
                    else if (" ".equals(nextTok))
                    {
                        if (lastTokenHasBeenQuoted || current.length() > 0)
                        {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    }
                    else
                    {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() > 0)
        {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote)
        {
            throw new CargoException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }

}
