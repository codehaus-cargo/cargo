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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * provides base level of testing for subclasses of AbstractInstalledLocalContainer.
 */
public class InstalledLocalContainerTest extends TestCase
{
    private LocalConfiguration configuration = null;

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
        };
    }

    public class AbstractInstalledLocalContainerStub extends AbstractInstalledLocalContainer{

        public AbstractInstalledLocalContainerStub(LocalConfiguration configuration)
        {
            super(configuration);
        }

        protected void doStart(Java java) throws Exception
        {
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
        
    }
    
    public void testDoesntSetToolsJarWhenOsX() throws Exception{
        System.setProperty("mrj.version","is.OsX");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container = new AbstractInstalledLocalContainerStub(configuration);
        Path path = new Path(new Project());
        container.addToolsJarToClasspath(path);
        assertFalse(path.toString().indexOf("myTestPath") >=0);
        
    }

    public void testSetsToolsJarWhenNotOsX() throws Exception{
        System.getProperties().remove("mrj.version");
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "myTestPath");
        AbstractInstalledLocalContainer container = new AbstractInstalledLocalContainerStub(configuration);
        Path path = new Path(new Project());
        container.addToolsJarToClasspath(path);
        assertTrue(path.toString().indexOf("myTestPath") >=0);
    }
    
    
    public void testSetsDefaultJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, null);
        AbstractInstalledLocalContainer container = new AbstractInstalledLocalContainerStub(configuration);
        Java java = new Java();
        container.setJvmToLaunchContainerIn(java);
        //wipe out anything that would break on windows
        String binDir = container.getFileHandler().append(System.getProperty("java.home"),"bin");
        String expected = container.getFileHandler().append(binDir,"java").replaceAll("\\\\","/").toLowerCase();
        String vmCmd = java.getCommandLine().getVmCommand().toString().replaceAll("\\\\","/").toLowerCase();
        // in windows, it may be .exe, so we'll ignore the extension
        assertTrue(vmCmd.startsWith(expected));
    }

    public void testSetsAlternateJavaHome() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.JAVA_HOME, "/my/java");
        AbstractInstalledLocalContainer container = new AbstractInstalledLocalContainerStub(configuration);
        Java java = new Java();
        container.setJvmToLaunchContainerIn(java);
        //wipe out anything that would break on windows
        String vmCmd = java.getCommandLine().getVmCommand().toString().replaceAll("\\\\","/").toLowerCase();
        assertTrue(vmCmd.startsWith("/my/java/bin/java"));
    }

}
