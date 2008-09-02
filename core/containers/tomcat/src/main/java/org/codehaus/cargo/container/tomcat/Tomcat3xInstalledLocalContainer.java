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
package org.codehaus.cargo.container.tomcat;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatInstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

import java.io.File;

/**
 * Special container support for the Apache Tomcat 3.x servlet container.
 * 
 * @version $Id$
 */
public class Tomcat3xInstalledLocalContainer extends AbstractTomcatInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomcat3x";

    /**
     * {@inheritDoc}
     * @see AbstractTomcatInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Tomcat3xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "Tomcat 3.x";
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
     * @see AbstractTomcatInstalledLocalContainer#invokeContainer(String, Java)
     */
    protected void invokeContainer(String action, Java java) throws Exception
    {
        java.addSysproperty(getAntUtils().createSysProperty("tomcat.install",
             getConfiguration().getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("tomcat.home",
             getConfiguration().getHome()));

        Path classpath = java.createClasspath();
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName("lib/**/*.jar");
        classpath.addFileset(fileSet);
        addToolsJarToClasspath(classpath);

        java.setClassname("org.apache.tomcat.startup.Main");
        java.createArg().setValue(action);
        AntContainerExecutorThread tomcatRunner = new AntContainerExecutorThread(java);
        tomcatRunner.start();
    }
}
