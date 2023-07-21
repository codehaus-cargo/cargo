/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.log.Logger;

/**
 * A JVM launcher that launches a new Process, that can be forcibly killed if needed.
 */
public class DefaultJvmLauncher implements JvmLauncher
{
    /**
     * Whether a JVM shutdown is in progress.
     */
    public static boolean shutdownInProgress = false;

    /**
     * Information message when changing the process attribute visibility fails.
     */
    private static final String PROCESS_ATTRIBUTE_CHANGE_MESSAGE =
        "Failed changing the visibility of internal JDK process classes, required for force "
            + "killing of the Java process Codehaus Cargo has created. You could add --add-opens "
                + "to your JVM arguments to allow this. ";

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
     * Timeout while waiting for {@link #execute()} command to complete.
     */
    private long timeout = 60 * 1000;

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
     * The logger to which the output of the JVM is redirected
     */
    private Logger outputLogger;

    /**
     * The log category to use when logging the JVM's outputs
     */
    private String category;

    /**
     * Creates a new launcher.
     */
    public DefaultJvmLauncher()
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

        if (executable == null)
        {
            throw new CargoException("Java executable not set");
        }
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
        if (!new File(command).isFile())
        {
            throw new JvmLauncherException("JVM executable file [" + command + "] doesn't exist");
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
            String[] args = DefaultJvmLauncher.translateCommandline(line);

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
            String[] args = DefaultJvmLauncher.translateCommandline(line);

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
     * {@inheritDoc}. This takes precendence over {@link #setOutputLogger(Logger, String)}.
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
     * {@inheritDoc}. If {@link #setOutputFile(File)} is set, that will take precedence.
     */
    @Override
    public void setOutputLogger(Logger outputLogger, String category)
    {
        this.outputLogger = outputLogger;
        if (category == null)
        {
            throw new IllegalArgumentException("Logger category should not be null");
        }
        this.category = category;
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
        if (process != null)
        {
            // Call first method to kill the process
            // This is cleanest in code, but no guarantees by the JVM...
            process.destroy();

            // So we call second method to kill the process to be sure
            try
            {
                nativeKill();
            }
            catch (NoClassDefFoundError e)
            {
                if (outputLogger != null)
                {
                    outputLogger.debug(
                        PROCESS_ATTRIBUTE_CHANGE_MESSAGE + e.getMessage(),
                            this.getClass().getName());
                }
                // This happens when the JVM is shutting down, ignore
            }
            process = null;
            System.gc();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(long millis)
    {
        this.timeout = millis;
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
            if (outputFile != null)
            {
                pb.redirectOutput(
                    appendOutput ? Redirect.appendTo(outputFile) : Redirect.to(outputFile));
            }
            pb.environment().putAll(environmentVariables);

            this.process = pb.start();
            process.getOutputStream().close();

            if (outputFile == null)
            {
                if (outputLogger != null)
                {
                    Thread outputStreamRedirector =
                        new Thread(new DefaultJvmLauncherLoggerRedirector(
                            process.getInputStream(), outputLogger, category));
                    outputStreamRedirector.start();
                }
                else
                {
                    process.getErrorStream().close();
                    process.getInputStream().close();
                }
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
                    DefaultJvmLauncher.shutdownInProgress = true;
                    DefaultJvmLauncher.this.kill();
                }
            });
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
            if (this.process.waitFor(DefaultJvmLauncher.this.timeout, TimeUnit.MILLISECONDS))
            {
                return this.process.exitValue();
            }
            else
            {
                try
                {
                    this.kill();
                }
                catch (Throwable e)
                {
                    // Ignore, we tried our best
                }

                throw new JvmLauncherException("Java command [" + this.getCommandLine()
                    + "] did not complete after " + DefaultJvmLauncher.this.timeout
                        + " milliseconds");
            }
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
        for (Field f : process.getClass().getDeclaredFields())
        {
            if ("pid".equals(f.getName()))
            {
                if (makeFieldAccessible(f))
                {
                    try
                    {
                        int pid = f.getInt(process);
                        Runtime.getRuntime().exec(
                            new String[] {"kill", "-9", Integer.toString(pid)});
                    }
                    catch (Throwable e)
                    {
                        // Ignore, we tried our best
                    }
                }
                break;
            }
            else if ("handle".equals(f.getName()))
            {
                if (makeFieldAccessible(f))
                {
                    try
                    {
                        long handleId = f.getLong(process);

                        Kernel32 kernel = Kernel32.INSTANCE;
                        HANDLE handle = new HANDLE();
                        handle.setPointer(Pointer.createConstant(handleId));
                        int pid = kernel.GetProcessId(handle);
                        Runtime.getRuntime().exec(
                            new String[] {"taskkill", "/PID", Integer.toString(pid), "/F"});
                    }
                    catch (Throwable e)
                    {
                        // Ignore, we tried our best
                    }
                }
                break;
            }
        }
    }

    /**
     * Sets a given (private) field accessible, while remaining compatible with Java 8 and avoiding
     * the <code>Illegal reflective access</code> messages in Java 9 onwards.
     * @param f Field to make accessible
     * @return Whether changing field accessibility fails.
     */
    private boolean makeFieldAccessible(Field f)
    {
        // See: https://stackoverflow.com/questions/46454995/#58834966
        Method getModule;
        try
        {
            getModule = Class.class.getMethod("getModule");
        }
        catch (NoSuchMethodException e)
        {
            // We are on Java 8
            getModule = null;
        }
        if (getModule != null)
        {
            try
            {
                Object thisModule = getModule.invoke(this.getClass());
                Method isNamed = thisModule.getClass().getMethod("isNamed");
                if (!(boolean) isNamed.invoke(thisModule))
                {
                    Class fieldClass = f.getDeclaringClass().getClass();
                    Object fieldModule = getModule.invoke(fieldClass);
                    Method addOpens = fieldModule.getClass().getMethod(
                        "addOpens", String.class, thisModule.getClass());
                    Method getPackageName = fieldClass.getMethod("getPackageName");
                    addOpens.invoke(fieldModule, getPackageName.invoke(fieldClass), thisModule);
                }
            }
            catch (Throwable t)
            {
                if (t instanceof InvocationTargetException)
                {
                    InvocationTargetException e = (InvocationTargetException) t;
                    if (e.getCause() != null
                        && e.getCause().getClass().getName().endsWith("IllegalCallerException"))
                    {
                        if (outputLogger != null)
                        {
                            outputLogger.debug(
                                PROCESS_ATTRIBUTE_CHANGE_MESSAGE + e.getCause().getMessage(),
                                    this.getClass().getName());
                        }
                        t = null;
                    }
                }
                if (t != null)
                {
                    throw new CargoException("Cannot set field accessibility for [" + f + "]", t);
                }
                return false;
            }
        }
        try
        {
            f.setAccessible(true);
            return true;
        }
        catch (Throwable t)
        {
            if (t.getClass().getName().endsWith("InaccessibleObjectException"))
            {
                if (outputLogger != null)
                {
                    outputLogger.debug(
                        PROCESS_ATTRIBUTE_CHANGE_MESSAGE + t.getMessage(),
                            this.getClass().getName());
                }
                t = null;
            }
            if (t != null)
            {
                throw new CargoException("Cannot set field accessibility for [" + f + "]", t);
            }
            return false;
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
