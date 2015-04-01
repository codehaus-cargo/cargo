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
package org.codehaus.cargo.daemon.jvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import org.apache.tools.ant.types.Commandline;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherException;

/**
 * A JVM launcher that launches a new Process, that can be forcibly killed if needed.
 *
 */
class DaemonJvmLauncher implements JvmLauncher
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
     * out.
     */
    private final PrintStream out = System.out;

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
     * Creates a new launcher.
     */
    public DaemonJvmLauncher()
    {
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
    public void setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public void setJvm(String command)
    {
        if ((command == null) || (command.length() == 0))
        {
            return;
        }
        this.executable =
            command.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }

    /**
     * {@inheritDoc}
     */
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
    public String getClasspath()
    {
        return classpath;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemProperty(String name, String value)
    {
        if (name != null && name.length() > 0)
        {
            systemProperties.add("-D" + name + "=" + value);
        }
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
        if (jarFile != null)
        {
            jarPath = jarFile.getAbsolutePath();
        }
    }

    /**
     * {@inheritDoc}
     */
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
    public void setOutputFile(File outputFile)
    {
        this.outputFile = outputFile;
    }

    /**
     * {@inheritDoc}
     */
    public void setAppendOutput(boolean appendOutput)
    {
        this.appendOutput = appendOutput;
    }

    /**
     * {@inheritDoc}
     */
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
    public void kill()
    {
        if (process != null)
        {
            // Call first method to kill the process
            // This is cleanest in code, but no guarantees by the JVM...
            process.destroy();

            // So we call second method to kill the process to be sure
            nativeKill();
            process = null;
        }
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
        try
        {
            ProcessBuilder pb =
                new ProcessBuilder(buildCommandLine()).directory(workingDirectory)
                    .redirectErrorStream(true);
            pb.environment().putAll(environmentVariables);

            this.process = pb.start();

            if (outputFile == null)
            {
                // Close the streams
                process.getErrorStream().close();
                process.getOutputStream().close();
                process.getInputStream().close();
            }
            else
            {
                FileOutputStream outputStream = new FileOutputStream(outputFile, appendOutput);

                Thread outputStreamRedirector =
                    new Thread(new DaemonJvmLauncherStreamRedirector(process.getInputStream(),
                        outputStream));

                outputStreamRedirector.start();
            }
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
                    if (DaemonJvmLauncher.this.process != null)
                    {
                        DaemonJvmLauncher.this.process.destroy();
                        DaemonJvmLauncher.this.process = null;
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
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
 
    /**
     * Forcefully kill the launched process using platform specific methods.
     */
    private void nativeKill()
    {
        if (process == null)
        {
            return;
        }
        if (process.getClass().getName().equals("java.lang.UNIXProcess"))
        {
            try
            {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                int pid = f.getInt(process);
                Runtime.getRuntime().exec("kill -9 " + pid);
            }
            catch (Throwable e)
            {
                // Ignore, we tried our best
            }
        }
        else if (process.getClass().getName().equals("java.lang.Win32Process")
            || process.getClass().getName().equals("java.lang.ProcessImpl"))
        {
            try
            {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handleId = f.getLong(process);

                Kernel32 kernel = Kernel32.INSTANCE;
                HANDLE handle = new HANDLE();
                handle.setPointer(Pointer.createConstant(handleId));
                int pid = kernel.GetProcessId(handle);
                Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
            }
            catch (Throwable e)
            {
                // Ignore, we tried our best
            }
        }
    }

}
