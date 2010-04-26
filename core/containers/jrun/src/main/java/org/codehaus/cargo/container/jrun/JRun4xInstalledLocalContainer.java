/* 
 * ========================================================================
 * 
 * Copyright 2003-2008 The Apache Software Foundation. Code from this file 
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
package org.codehaus.cargo.container.jrun;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jrun.internal.AbstractJRunInstalledLocalContainer;

/**
 * Special container support for the Adobe JRun4.x servlet container.
 * 
 * @version $Id: JRun4xInstalledLocalContainer.java rconnolly $
 */
public class JRun4xInstalledLocalContainer extends AbstractJRunInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jrun4x";
    
    /**
     * {@inheritDoc}
     * @see AbstractJRunInstalledLocalContainer#AbstractJRunInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public JRun4xInstalledLocalContainer(LocalConfiguration configuration)
    {
       super(configuration);
    }
    
    /**
     * {@inheritDoc}
     * @see AbstractJRunInstalledLocalContainer#startUpAdditions(Java, Path)
     */
    protected void startUpAdditions(Java java, Path classpath) throws FileNotFoundException
    {
        java.addSysproperty(getAntUtils().createSysProperty("sun.io.useCanonCaches", "false"));
        java.addSysproperty(getAntUtils().createSysProperty("jmx.invoke.getters", "true"));

        // If getHome() contains spaces a hot fix is required in order for jrun to be able to 
        // stop itself. The following property is needed along with the hot fix.
        // see: http://kb.adobe.com/selfservice/viewContent.do?externalId=4c7d1c1        
        File hotFixJar = new File(getHome() + "/servers/lib/54101.jar");
        if (hotFixJar.exists())
        {
            java.addSysproperty(getAntUtils().createSysProperty(
                "-Djava.rmi.server.RMIClassLoaderSpi", "jrunx.util.JRunRMIClassLoaderSpi"));
        }
        
        java.addSysproperty(getAntUtils()
            .createSysProperty("java.home", System.getProperty("java.home")));
        
        // Add the tools.jar to the classpath.
        addToolsJarToClasspath(classpath);
    }    
    
    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "JRun " + getVersion("4.x");
    }
}
