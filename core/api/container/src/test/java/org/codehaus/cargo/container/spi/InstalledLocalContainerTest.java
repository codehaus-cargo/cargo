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
package org.codehaus.cargo.container.spi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.DefaultJvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.stub.JvmLauncherStub;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Provides base level of testing for subclasses of AbstractInstalledLocalContainer.
 */
public class InstalledLocalContainerTest
{
    /**
     * Previous file.
     */
    private static final String PREVIOUS_FILE = "ram:/Install/test1";

    /**
     * Test file.
     */
    private static final String TEST_FILE = "ram:/Install/test2";

    /**
     * Container configuration.
     */
    private AbstractStandaloneLocalConfiguration configuration;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container configuration.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        this.configuration = new AbstractStandaloneLocalConfiguration("/some/path")
        {
            /**
             * Doesn't do anything. {@inheritDoc}
             * @param container Ignored.
             * @throws Exception If anything goes wrong.
             */
            @Override
            protected void doConfigure(LocalContainer container) throws Exception
            {
            }

            /**
             * {@inheritDoc}
             * @return <code>null</code>.
             */
            @Override
            public ConfigurationCapability getCapability()
            {
                return null;
            }

            /**
             * Doesn't do anything. {@inheritDoc}
             * @param resource Ignored.
             */
            @Override
            public void addResource(Resource resource)
            {
                // TODO Auto-generated method stub
            }
        };

        fsManager = new StandardFileSystemManager();
        fsManager.init();
        fileHandler = new VFSFileHandler(fsManager);
        this.configuration.setFileHandler(fileHandler);
        this.fileHandler.createFile(TEST_FILE);
        this.fileHandler.createFile(PREVIOUS_FILE);
    }

    /**
     * {@link AbstractInstalledLocalContainer} for testing. {@inheritDoc}
     */
    public class AbstractInstalledLocalContainerStub extends AbstractInstalledLocalContainer
    {
        /**
         * Java executable.
         */
        private JvmLauncher java;

        /**
         * {@inheritDoc}
         * @param configuration Container configuration.
         */
        public AbstractInstalledLocalContainerStub(LocalConfiguration configuration)
        {
            super(configuration);
        }

        /**
         * Saves the {@link JvmLauncher} instance. {@inheritDoc}
         * @param java Java instance.
         * @throws Exception If anything goes wrong.
         */
        @Override
        protected void doStart(JvmLauncher java) throws Exception
        {
            this.java = java;
            java.setMainClass("test.Dummy");
        }

        /**
         * Doesn't do anything. {@inheritDoc}
         * @param java Ignored.
         * @throws Exception If anything goes wrong.
         */
        @Override
        protected void doStop(JvmLauncher java) throws Exception
        {
        }

        /**
         * {@inheritDoc}
         * @return <code>null</code>.
         */
        @Override
        public ContainerCapability getCapability()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         * @return <code>null</code>.
         */
        @Override
        public String getId()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         * @return <code>null</code>.
         */
        @Override
        public String getName()
        {
            return null;
        }

        /**
         * Method for testing to retrieve the java command created.
         * @return Passed {@link JvmLauncher} instance.
         */
        public JvmLauncher getJava()
        {
            return this.java;
        }

    }

    /**
     * Tests that <code>tools.jar</code> is not set on MacOS X.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoesntSetToolsJarWhenOsX() throws Exception
    {
        System.setProperty("mrj.version", "is.OsX");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        JvmLauncherStub java = new JvmLauncherStub();
        container.addToolsJarToClasspath(java);
        Assertions.assertFalse(java.getClasspath().contains("myTestPath"));
    }

    /**
     * Tests that <code>tools.jar</code> is set on other platforms than MacOS X and Java versions
     * equal or higher than 9.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSetsToolsJarWhenNotOsXOrJava9() throws Exception
    {
        System.getProperties().remove("mrj.version");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        JvmLauncherStub java = new JvmLauncherStub();
        container.addToolsJarToClasspath(java);
        if (JdkUtils.getMajorJavaVersion() < 9)
        {
            Assertions.assertTrue(java.getClasspath().contains("myTestPath"));
        }
    }

    /**
     * Tests that <code>JAVA_HOME</code> getter and setter work properly.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSetsDefaultJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, null);
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        JvmLauncherStub java = new JvmLauncherStub();
        container.setJvmToLaunchContainerIn(java);
        // wipe out anything that would break on windows
        String binDir = container.getFileHandler().append(System.getProperty("java.home"), "bin");
        String expected =
            container.getFileHandler().append(binDir, "java").replaceAll("\\\\", "/")
                .toLowerCase();
        String vmCmd = java.getJvm().replaceAll("\\\\", "/").toLowerCase();
        // vmCmd may be wrapped in double quotes on windows when JAVA_HOME has spaces in it
        vmCmd = vmCmd.replaceAll("\"", "");
        // in windows, it may be .exe, so we'll ignore the extension
        Assertions.assertTrue(vmCmd.startsWith(expected));
    }

    /**
     * Tests that <code>JAVA_HOME</code> can be changed.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSetsAlternateJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "/my/java");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        JvmLauncherStub java = new JvmLauncherStub();
        container.setJvmToLaunchContainerIn(java);
        // wipe out anything that would break on windows
        String vmCmd = java.getJvm().replaceAll("\\\\", "/").toLowerCase();
        Assertions.assertTrue(vmCmd.startsWith("/my/java/bin/java"));
    }

    /**
     * Tests that the shared classpath is not <code>null</code> even when empty.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSharedClasspathNotNull() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Assertions.assertNotNull(container.getSharedClasspath());
        Assertions.assertEquals(0, container.getSharedClasspath().length);
    }

    /**
     * Tests adding a JAR to the shared classpath when no path was set.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testAddSharedClasspathWorksWithNoPreviousPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.addSharedClasspath(TEST_FILE);
        Assertions.assertEquals(1, container.getSharedClasspath().length);
        Assertions.assertEquals(TEST_FILE, container.getSharedClasspath()[0]);
    }

    /**
     * Tests adding a JAR to the shared classpath when another path was already set.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testAddSharedClasspathWorksWithAnotherPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.setSharedClasspath(new String[] {PREVIOUS_FILE});
        Assertions.assertEquals(1, container.getSharedClasspath().length);
        Assertions.assertEquals(PREVIOUS_FILE, container.getSharedClasspath()[0]);

        container.addSharedClasspath(TEST_FILE);
        Assertions.assertEquals(2, container.getSharedClasspath().length);
        Assertions.assertEquals(PREVIOUS_FILE, container.getSharedClasspath()[0]);
        Assertions.assertEquals(TEST_FILE, container.getSharedClasspath()[1]);
    }

    /**
     * Tests that the extra classpath is not <code>null</code> even when empty.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testExtraClasspathNotNull() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Assertions.assertNotNull(container.getExtraClasspath());
        Assertions.assertEquals(0, container.getExtraClasspath().length);
    }

    /**
     * Tests adding a JAR to the extra classpath when no path was set.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testAddExtraClasspathWorksWithNoPreviousPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.addExtraClasspath(TEST_FILE);
        Assertions.assertEquals(1, container.getExtraClasspath().length);
        Assertions.assertEquals(TEST_FILE, container.getExtraClasspath()[0]);
    }

    /**
     * Tests adding a JAR to the extra classpath when another path was already set.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testAddExtraClasspathWorksWithAnotherPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.setExtraClasspath(new String[] {PREVIOUS_FILE});
        Assertions.assertEquals(1, container.getExtraClasspath().length);
        Assertions.assertEquals(PREVIOUS_FILE, container.getExtraClasspath()[0]);

        container.addExtraClasspath(TEST_FILE);
        Assertions.assertEquals(2, container.getExtraClasspath().length);
        Assertions.assertEquals(PREVIOUS_FILE, container.getExtraClasspath()[0]);
        Assertions.assertEquals(TEST_FILE, container.getExtraClasspath()[1]);
    }

    /**
     * Test that system properties are never null.
     */
    @Test
    public void testSystemPropertiesNeverNull()
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Assertions.assertNotNull(container.getSystemProperties());
        Assertions.assertEquals(0, container.getSystemProperties().size());
    }

    /**
     * Test that system properties can be set.
     */
    @Test
    public void testCanSetSystemProperty()
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.getSystemProperties().put("1", "2");
        Assertions.assertEquals(1, container.getSystemProperties().size());
        Assertions.assertEquals("2", container.getSystemProperties().get("1"));
    }

    /**
     * Test the runtime arguments.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testRuntimeArgs() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.RUNTIME_ARGS, "hello -world");
        container.startInternal();
        JvmLauncher java = container.getJava();
        Assertions.assertTrue(
            java.getCommandLine().contains("hello -world"),
                "Expected runtime arguments not contained in the java commandline.");
    }

    /**
     * Test the JVM arguments.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testJvmArgs() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS,
            "-Dx.y=z\n\r\t\t-Du.v=w");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Dx.y=z ");
        checkString(commandLine, "-Du.v=w");
        Assertions.assertFalse(commandLine.contains("\n"), "check new lines");
        Assertions.assertFalse(commandLine.contains("\r"), "check new lines");
        Assertions.assertFalse(commandLine.contains("\t"), "check tabs");
    }

    /**
     * Test the default memory arguments.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDefaultMemoryArguments() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
    }

    /**
     * Test memory arguments override.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testXmsMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-Xms256m");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms256m");
        checkString(commandLine, "-Xmx512m");
    }

    /**
     * Test memory arguments override.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testXmxMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-Xmx256m");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx256m");
    }

    /**
     * Test memory arguments override.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testXXPermSizeMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-XX:PermSize=256m");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:PermSize=256m");
    }

    /**
     * Test memory arguments override.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testXXMaxPermSizeMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration()
            .setProperty(GeneralPropertySet.JVMARGS, "-XX:MaxPermSize=256m");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:MaxPermSize=256m");
    }

    /**
     * Test memory arguments override.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testAllMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS,
                "-Xms256m -Xmx256m -XX:PermSize=256m -XX:MaxPermSize=256m");

        container.startInternal();
        JvmLauncher java = container.getJava();
        String commandLine = java.getCommandLine();
        checkString(commandLine, "-Xms256m");
        checkString(commandLine, "-Xmx256m");
        checkString(commandLine, "-XX:PermSize=256m");
        checkString(commandLine, "-XX:MaxPermSize=256m");
    }

    /**
     * Check if <code>haystack</code> contains <code>needle</code>. A JUnit assertion will fail
     * otherwise.
     * @param haystack String in which to search.
     * @param needle String to search.
     */
    private void checkString(String haystack, String needle)
    {
        Assertions.assertTrue(
            haystack.contains(needle),
                "Expected argument \"" + needle + "\", got \"" + haystack + "\"");
    }

    /**
     * Test the case when the JVM version is slow to execute.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testJvmVersionSlowToExecute() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
                new AbstractInstalledLocalContainerStub(configuration);

        DefaultJvmLauncher delayingJvmLauncher = new DefaultJvmLauncher()
        {
            @Override
            public void setOutputFile(File outputFile)
            {
                try (PrintWriter writer = new PrintWriter(outputFile))
                {
                    // Write a first line of text directly to pinpoint CARGO-1595 issue:
                    writer.println("Picked up JAVA_TOOL_OPTIONS: -Dtest=foo");
                    writer.flush();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                super.setOutputFile(outputFile);
            }

        };
        container.setJvmLauncherFactory(request -> delayingJvmLauncher);
        container.startInternal();
    }

}
