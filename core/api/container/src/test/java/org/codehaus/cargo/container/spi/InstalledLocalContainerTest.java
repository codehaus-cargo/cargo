/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * provides base level of testing for subclasses of AbstractInstalledLocalContainer.
 */
public class InstalledLocalContainerTest extends TestCase
{
    private AbstractStandaloneLocalConfiguration configuration = null;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    private String previousFile = "ram:/Install/test1";

    private String testFile = "ram:/Install/test2";

    protected void setUp() throws Exception
    {
        this.configuration = new AbstractStandaloneLocalConfiguration("/some/path")
        {
            protected void doConfigure(LocalContainer container) throws Exception
            {
            }

            public ConfigurationCapability getCapability()
            {
                return null;
            }

            public void addResource(Resource resource)
            {
                // TODO Auto-generated method stub

            }
        };
        fsManager = new StandardFileSystemManager();
        fsManager.init();
        fileHandler = new VFSFileHandler(fsManager);
        this.configuration.setFileHandler(fileHandler);
        this.fileHandler.createFile(testFile);
        this.fileHandler.createFile(previousFile);

    }

    public class AbstractInstalledLocalContainerStub extends AbstractInstalledLocalContainer
    {

    	Java java;
    	
        public AbstractInstalledLocalContainerStub(LocalConfiguration configuration)
        {
            super(configuration);
        }

        protected void doStart(Java java) throws Exception
        {
        	this.java = java;
        }

        protected void doStop(Java java) throws Exception
        {
        }

        public ContainerCapability getCapability()
        {
            return null;
        }

        public String getId()
        {
            return null;
        }

        public String getName()
        {
            return null;
        }

        // method for testing to retrieve the java command created
        public Java getJava()
        {
        	return this.java;
        }
        
    }

    public void testDoesntSetToolsJarWhenOsX() throws Exception
    {
        System.setProperty("mrj.version", "is.OsX");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Path path = new Path(new Project());
        container.addToolsJarToClasspath(path);
        assertFalse(path.toString().indexOf("myTestPath") >= 0);

    }

    public void testSetsToolsJarWhenNotOsX() throws Exception
    {
        System.getProperties().remove("mrj.version");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Path path = new Path(new Project());
        container.addToolsJarToClasspath(path);
        assertTrue(path.toString().indexOf("myTestPath") >= 0);
    }

    public void testSetsDefaultJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, null);
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Java java = new Java();
        container.setJvmToLaunchContainerIn(java);
        // wipe out anything that would break on windows
        String binDir = container.getFileHandler().append(System.getProperty("java.home"), "bin");
        String expected =
            container.getFileHandler().append(binDir, "java").replaceAll("\\\\", "/")
                .toLowerCase();
        String vmCmd =
            java.getCommandLine().getVmCommand().toString().replaceAll("\\\\", "/").toLowerCase();
        // vmCmd may be wrapped in double quotes on windows when JAVA_HOME has spaces in it
        vmCmd = vmCmd.replaceAll("\"", "");
        // in windows, it may be .exe, so we'll ignore the extension
        assertTrue(vmCmd.startsWith(expected));
    }

    public void testSetsAlternateJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "/my/java");
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        Java java = new Java();
        container.setJvmToLaunchContainerIn(java);
        // wipe out anything that would break on windows
        String vmCmd =
            java.getCommandLine().getVmCommand().toString().replaceAll("\\\\", "/").toLowerCase();
        assertTrue(vmCmd.startsWith("/my/java/bin/java"));
    }

    public void testSharedClasspathNotNull() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        assertNotNull(container.getSharedClasspath());
        assertEquals(0, container.getSharedClasspath().length);
    }

    public void testAddSharedClasspathWorksWithNoPreviousPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.addSharedClasspath(testFile);
        assertEquals(1, container.getSharedClasspath().length);
        assertEquals(testFile, container.getSharedClasspath()[0]);
    }

    public void testAddSharedClasspathWorksWithAnotherPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.setSharedClasspath(new String[] {previousFile});
        assertEquals(1, container.getSharedClasspath().length);
        assertEquals(previousFile, container.getSharedClasspath()[0]);

        container.addSharedClasspath(testFile);
        assertEquals(2, container.getSharedClasspath().length);
        assertEquals(previousFile, container.getSharedClasspath()[0]);
        assertEquals(testFile, container.getSharedClasspath()[1]);
    }

    public void testExtraClasspathNotNull() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        assertNotNull(container.getExtraClasspath());
        assertEquals(0, container.getExtraClasspath().length);
    }

    public void testAddExtraClasspathWorksWithNoPreviousPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.addExtraClasspath(testFile);
        assertEquals(1, container.getExtraClasspath().length);
        assertEquals(testFile, container.getExtraClasspath()[0]);
    }

    public void testAddExtraClasspathWorksWithAnotherPath() throws Exception
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.setFileHandler(fileHandler);

        container.setExtraClasspath(new String[] {previousFile});
        assertEquals(1, container.getExtraClasspath().length);
        assertEquals(previousFile, container.getExtraClasspath()[0]);

        container.addExtraClasspath(testFile);
        assertEquals(2, container.getExtraClasspath().length);
        assertEquals(previousFile, container.getExtraClasspath()[0]);
        assertEquals(testFile, container.getExtraClasspath()[1]);
    }

    public void testSystemPropertiesNeverNull()
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        assertNotNull(container.getSystemProperties());
        assertEquals(0, container.getSystemProperties().size());
    }

    public void testCanSetSystemProperty()
    {
        AbstractInstalledLocalContainer container =
            new AbstractInstalledLocalContainerStub(configuration);
        container.getSystemProperties().put("1", "2");
        assertEquals(1, container.getSystemProperties().size());
        assertEquals("2", container.getSystemProperties().get("1"));
    }
    
    public void testRuntimeArgs()
    {
    	AbstractInstalledLocalContainerStub container =
    		new AbstractInstalledLocalContainerStub(configuration);
    	try
    	{
    		container.getConfiguration().setProperty("cargo.runtime.args", "hello -world");
    		container.startInternal();
    		Java java = container.getJava();
    		assertTrue("Expected runtime arguments not contained in the java commandline.", java.getCommandLine().toString().contains("hello -world"));
    	} 
    	catch (Exception e)
    	{
    		assertFalse("An exception occured while getting the java object", true);
    	}
    }
    
    public void testJvmArgs()
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

  	    container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-Dx.y=z\n\r\t\t-Du.v=w");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Dx.y=z ");
        checkString(commandLine, "-Du.v=w");
        assertFalse("check new lines", commandLine.contains("\n"));
        assertFalse("check new lines", commandLine.contains("\r"));
        assertFalse("check tabs", commandLine.contains("\t"));
    }

    public void testDefaultMemoryArguments() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:PermSize=48m");
        checkString(commandLine, "-XX:MaxPermSize=128m");
    }

    public void testXmsMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-Xms256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms256m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:PermSize=48m");
        checkString(commandLine, "-XX:MaxPermSize=128m");
    }

    public void testXmxMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-Xmx256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx256m");
        checkString(commandLine, "-XX:PermSize=48m");
        checkString(commandLine, "-XX:MaxPermSize=128m");
    }

    public void testXXPermSizeMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-XX:PermSize=256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:PermSize=256m");
        checkString(commandLine, "-XX:MaxPermSize=128m");
    }

    public void testXXMaxPermSizeMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS, "-XX:MaxPermSize=256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms128m");
        checkString(commandLine, "-Xmx512m");
        checkString(commandLine, "-XX:PermSize=48m");
        checkString(commandLine, "-XX:MaxPermSize=256m");
    }

    public void testAllMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS,
                "-Xms256m -Xmx256m -XX:PermSize=256m -XX:MaxPermSize=256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms256m");
        checkString(commandLine, "-Xmx256m");
        checkString(commandLine, "-XX:PermSize=256m");
        checkString(commandLine, "-XX:MaxPermSize=256m");
    }

    public void testAllMemoryArgumentOverride() throws Exception
    {
        AbstractInstalledLocalContainerStub container =
            new AbstractInstalledLocalContainerStub(configuration);

        container.getConfiguration().setProperty(GeneralPropertySet.JVMARGS,
                "-Xms256m -Xmx256m -XX:PermSize=256m -XX:MaxPermSize=256m");

        container.startInternal();
        Java java = container.getJava();
        String commandLine = java.getCommandLine().toString();
        checkString(commandLine, "-Xms256m");
        checkString(commandLine, "-Xmx256m");
        checkString(commandLine, "-XX:PermSize=256m");
        checkString(commandLine, "-XX:MaxPermSize=256m");
    }

    private void checkString(String haystack, String needle)
    {
        assertTrue("Expected argument \"" + needle + "\", got \"" + haystack + "\"", haystack.contains(needle));
    }
}
