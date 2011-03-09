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

/**
 * A JVM launcher is responsible to fork a JVM.
 * 
 * @version $Id$
 */
public interface JvmLauncher
{

    /**
     * Sets the working directory for the forked JVM.
     * 
     * @param workingDirectory The working directory for the forked JVM, may be {@code null}.
     */
    void setWorkingDirectory(File workingDirectory);

    /**
     * Sets the command to launch the JVM.
     * 
     * @param command The command to lauch the JVM, may be {@code null}.
     */
    void setJvm(String command);

    /**
     * Adds the specified pathname as an argument to the JVM.
     * 
     * @param file The pathname to add, may be {@code null}.
     */
    void addJvmArgument(File file);

    /**
     * Adds the specified values as arguments to the JVM.
     * 
     * @param values The values to add, may be {@code null}.
     */
    void addJvmArguments(String... values);

    /**
     * Adds the specified comma separated argument line as arguments to the JVM.
     * 
     * @param line The arguments to add, may be be {@code null}.
     */
    void addJvmArgumentLine(String line);

    /**
     * Adds the specified paths to the system classpath of the JVM.
     * 
     * @param paths The classpath entries, may be {@code null}.
     */
    void addClasspathEntries(String... paths);

    /**
     * Adds the specified paths to the system classpath of the JVM.
     * 
     * @param paths The classpath entries, may be {@code null}.
     */
    void addClasspathEntries(File... paths);

    /**
     * Gets the currently configured system classpath.
     * 
     * @return The currently configured system classpath, never {@code null}.
     */
    String getClasspath();

    /**
     * Sets a system property for the JVM.
     * 
     * @param name The property name, may be {@code null}.
     * @param value The property value, may be {@code null}.
     */
    void setSystemProperty(String name, String value);

    /**
     * Sets the JAR file containing the main class to execute. Only one of {@link #setJarFile(File)}
     * and {@link #setMainClass(String)} may be invoked.
     * 
     * @param jarFile The JAR file to execute, may be {@code null}.
     */
    void setJarFile(File jarFile);

    /**
     * Sets the qualified name of the Java class to execute. Only one of {@link #setJarFile(File)}
     * and {@link #setMainClass(String)} may be invoked.
     * 
     * @param mainClass The qualified name of the Java class to execute, may be {@code null}.
     */
    void setMainClass(String mainClass);

    /**
     * Adds the specified pathname as an argument to the application.
     * 
     * @param file The pathname to add, may be {@code null}.
     */
    void addAppArgument(File file);

    /**
     * Adds the specified values as arguments to the application.
     * 
     * @param values The values to add, may be {@code null}.
     */
    void addAppArguments(String... values);

    /**
     * Adds the specified comma separated argument line as arguments to the application.
     * 
     * @param line The arguments to add, may be {@code null}.
     */
    void addAppArgumentLine(String line);

    /**
     * Sets the file to which the output of the JVM is redirected.
     * 
     * @param outputFile The file to which the output of the JVM is redirected, may be {@code null}.
     */
    void setOutputFile(File outputFile);

    /**
     * Controls whether the redirected output should be appended to an existing output file (if
     * any).
     * 
     * @param appendOutput {@code true} to append the output, {@code false} to overwrite the file.
     * @see #setOutputFile(File)
     */
    void setAppendOutput(boolean appendOutput);

    /**
     * Gets a string representation of the currently configured command line.
     * 
     * @return The currently configured command line, never {@code null}.
     */
    String getCommandLine();

    /**
     * Sets the timeout in milliseconds after which the process will be killed if still running.
     * 
     * @param millis The timeout, may be non-positive to disable process watching.
     */
    void setTimeout(long millis);

    /**
     * Launches a JVM according to the configuration of this launcher. The method returns as soon as
     * the JVM got launched and does not wait for its termination.
     * 
     * @throws JvmLauncherException If the JVM could not be launched.
     */
    void start() throws JvmLauncherException;

    /**
     * Launches a JVM according to the configuration of this launcher and waits for its termination.
     * 
     * @return The exit code of the JVM.
     * @throws JvmLauncherException If the JVM could not be launched.
     */
    int execute() throws JvmLauncherException;

}
