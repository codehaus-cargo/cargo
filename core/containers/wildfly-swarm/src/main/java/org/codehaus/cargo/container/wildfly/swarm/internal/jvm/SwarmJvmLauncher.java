/*
 * ========================================================================
 *
 *  Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.jvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tools.ant.types.Commandline;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherException;
import org.codehaus.cargo.util.log.Logger;

/**
 * Custom launcher for WildFly Swarm. Swarm cannot guarantee there will be any management interface
 * accepting commands (e.g. stop command), thus this implementation relies on
 * {@link java.lang.Process}.
 */
public class SwarmJvmLauncher implements JvmLauncher
{

    /**
     * The working directory.
     */
    private File workingDirectory;

    /**
     * The executable to run.
     */
    private String executable;

    /**
     * The vm arguments.
     */
    private final List<String> jvmArguments = new ArrayList<String>();

    /**
     * The vm classpath.
     */
    private String classpath;

    /**
     * The vm jar path.
     */
    private String jarPath;

    /**
     * The main class to run.
     */
    private String mainClass;

    /**
     * The vm system properties.
     */
    private final List<String> systemProperties = new ArrayList<String>();

    /**
     * The extra environment variables.
     */
    private final Map<String, String> environmentVariables = new HashMap<String, String>();

    /**
     * The application arguments.
     */
    private final List<String> applicationArguments = new ArrayList<String>();

    /**
     * The running process.
     */
    private Process process;

    /**
     * Output file.
     */
    private File outputFile;

    /**
     * Append output.
     */
    private boolean appendOutput = false;

    /**
     * Logger instance.
     */
    private Logger logger;

    /**
     * Stream redirector.
     */
    private StreamRedirector streamRedirector;

    /**
     * Sets to <code>true</code> when either {@link #kill()} has been called or a shutdown hook
     * took effect.
     */
    private AtomicBoolean killed = new AtomicBoolean(false);

    /**
     * Constructor.
     * @param logger logger instance.
     */
    public SwarmJvmLauncher(final Logger logger)
    {
        this.logger = logger;
    }

    /**
     * Build the complete command line.
     * 
     * @return the array representing the tokens of the command line
     */
    private List<String> buildCommandLine()
    {
        List<String> commandLine = new ArrayList<String>();

        commandLine.add(executable);

        commandLine.addAll(jvmArguments);
        commandLine.addAll(systemProperties);

        if (classpath != null && jarPath == null)
        {
            commandLine.add("-classpath");
            commandLine.add(classpath);
        }

        if (jarPath != null)
        {
            commandLine.add("-jar");
            commandLine.add(jarPath);
        }

        if (jarPath == null)
        {
            commandLine.add(mainClass);
        }
        commandLine.addAll(applicationArguments);

        return commandLine;
    }

    /**
     * Add a path to the classpath.
     * 
     * @param path the path to add to the classpath
     */
    private void addClasspath(String path)
    {
        if (classpath == null)
        {
            classpath = path;
        }
        else
        {
            classpath += File.pathSeparator + path;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;
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
        this.executable =
                command.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addJvmArgument(File file)
    {
        if (file != null)
        {
            jvmArguments.add(file.getAbsolutePath());
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
                jvmArguments.add(value);
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
            String[] args = Commandline.translateCommandline(line);

            if (args != null)
            {
                for (String arg : args)
                {
                    jvmArguments.add(arg);
                }
            }
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
            for (String path : paths)
            {
                addClasspath(path);
            }
        }
    }

    /**
     * Adds additional classpath entries.
     * 
     * @param paths The additional classpath entries.
     */
    public void addClasspathEntries(List<String> paths)
    {
        if (paths != null)
        {
            for (String path : paths)
            {
                addClasspath(path);
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
            for (File path : paths)
            {
                addClasspath(path.getAbsolutePath());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClasspath()
    {
        return classpath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemProperty(String name, String value)
    {
        if (name != null && !name.isEmpty())
        {
            systemProperties.add("-D" + name + "=" + value);
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
            jarPath = jarFile.getAbsolutePath();
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
            this.mainClass = mainClass;
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
            applicationArguments.add(file.getAbsolutePath());
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
                applicationArguments.add(value);
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
            String[] args = Commandline.translateCommandline(line);

            if (args != null)
            {
                for (String arg : args)
                {
                    applicationArguments.add(arg);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputFile(File outputFile)
    {
        this.outputFile = outputFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppendOutput(boolean appendOutput)
    {
        this.appendOutput = appendOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommandLine()
    {
        StringBuilder result = new StringBuilder();
        List<String> commandLine = buildCommandLine();
        if (commandLine != null)
        {
            for (int i = 0; i < commandLine.size(); i++)
            {
                if (i != 0)
                {
                    result.append(' ');
                }

                result.append(commandLine.get(i));
            }
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill()
    {
        terminateProcess();
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
        try
        {
            ProcessBuilder pb =
                    new ProcessBuilder(buildCommandLine()).directory(workingDirectory)
                            .redirectErrorStream(true);
            pb.environment().putAll(environmentVariables);

            this.process = pb.start();

            OutputStream out = System.out;
            if (outputFile != null)
            {
                out = new FileOutputStream(outputFile, appendOutput);
            }

            this.streamRedirector = new StreamRedirector(process.getInputStream(), out);

            new Thread(streamRedirector).start();
        }
        catch (IOException e)
        {
            throw new JvmLauncherException("Failed to launch process " + e);
        }
        finally
        {
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    SwarmJvmLauncher.this.terminateProcess();
                }
            });
        }
    }

    /**
     * Terminates Swarm process execution.
     */
    private void terminateProcess()
    {
        if (!killed.getAndSet(true))
        {
            process.destroy();

            if (streamRedirector != null)
            {
                if (streamRedirector.hasError())
                {
                    String message =
                            StackTraceUtil.getStackTrace(streamRedirector.getError());
                    logger.warn(message, SwarmJvmLauncher.class.getCanonicalName());
                }

                try
                {
                    streamRedirector.close();
                }
                catch (IOException e)
                {
                    logger.warn(
                            StackTraceUtil.getStackTrace(e),
                            SwarmJvmLauncher.class.getCanonicalName()
                    );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int execute() throws JvmLauncherException
    {
        start();
        try
        {
            return this.process.waitFor();
        }
        catch (InterruptedException e)
        {
            throw new JvmLauncherException("Failed waiting for process to end", e);
        }
    }

}
