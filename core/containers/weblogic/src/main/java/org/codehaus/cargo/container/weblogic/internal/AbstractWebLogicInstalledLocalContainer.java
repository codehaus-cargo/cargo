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
package org.codehaus.cargo.container.weblogic.internal;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogicLocalContainer;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic support for the WebLogic application server.
 *
 * @version $Id$
 */
public abstract class AbstractWebLogicInstalledLocalContainer extends
        AbstractInstalledLocalContainer implements WebLogicLocalContainer
{
    /**
     * The Bea home directory.
     */
    private String beaHome;

    /**
     * Capability of the WebLogic container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractWebLogicInstalledLocalContainer(
            final LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Sets the Bea home directory.
     *
     * @param beaHome The BEA home directory
     */
    public final void setBeaHome(final String beaHome)
    {
        this.beaHome = beaHome;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public final ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#verify()
     */
    protected final void verify()
    {
        super.verify();
        initBeaHome();
        verifyBeaHome();
        verifyWeblogicHome();
    }

    /**
     * @return a list of files that indicate a properly installed BEA_HOME
     */
    protected List getBeaHomeFiles()
    {
        List requiredFiles = new ArrayList();
        requiredFiles
                .add(getFileHandler().append(getBeaHome(), "registry.xml"));
        return requiredFiles;
    }

    /**
     * @return a list of directories that indicate a properly installed BEA_HOME
     */
    protected List getBeaHomeDirs()
    {

        return new ArrayList();
    }

    /**
     * @return a list of files that indicate a properly installed WL_HOME
     */
    protected List getWeblogicHomeFiles()
    {
        List requiredFiles = new ArrayList();
        requiredFiles.add(getFileHandler().append(getWeblogicHome(),
                "server/lib/weblogic.jar"));
        return requiredFiles;
    }

    /**
     * @return a list of directories that indicate a properly installed WL_HOME
     */
    protected List getWeblogicHomeDirs()
    {
        List requiredDirs = new ArrayList();
        requiredDirs.add(getFileHandler().append(getWeblogicHome(),
                "server/lib"));
        return requiredDirs;
    }

    /**
     * Verify that the Weblogic home directory structure is valid and throw a
     * ContainerException if not.
     * 
     * @throws ContainerException
     *                 if any
     */
    protected void verifyWeblogicHome()
    {
        List requiredDirs = this.getWeblogicHomeDirs();
        List requiredFiles = this.getWeblogicHomeFiles();
        String errorPrefix = "Invalid Weblogic installation. ";
        String errorSuffix = "Make sure the WL_HOME directory you have specified "
                + "points to the right location (It's currently pointing to ["
                + getWeblogicHome() + "])";
        verify(errorPrefix, errorSuffix, requiredDirs, requiredFiles);
    }

    /**
     * Verify that the Bea home directory structure is valid and throw a
     * ContainerException if not.
     * 
     * @throws ContainerException
     *                 if any
     */
    protected void verifyBeaHome()
    {
        List requiredDirs = this.getBeaHomeDirs();
        List requiredFiles = this.getBeaHomeFiles();
        String errorPrefix = "Invalid Weblogic installation. ";
        String errorSuffix = "Make sure the BEA_HOME directory you have specified "
                + "points to the right location (It's currently pointing to ["
                + getBeaHome() + "])";
        verify(errorPrefix, errorSuffix, requiredDirs, requiredFiles);
    }

    /**
     * run through a list of expected files and directories that indicate a
     * properly installed product.
     * 
     * @param errorPrefix -
     *                Prefix to the ContainerException, if a file or directory
     *                is missing
     * @param errorSuffix -
     *                Suffix o the above
     * @param requiredDirs -
     *                Directories that are required to exist
     * @param requiredFiles -
     *                Files that are required to exist
     * 
     * @throws ContainerException -
     *                 if a file or directory isn't present as expected
     */
    protected void verify(String errorPrefix, String errorSuffix,
            List requiredDirs, List requiredFiles)
    {
        for (Iterator it = requiredDirs.iterator(); it.hasNext();)
        {
            String dir = (String) it.next();

            if (!getFileHandler().exists(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                        + "] directory doesn't exist. " + errorSuffix);
            }
            if (!getFileHandler().isDirectory(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                        + "] path should be a directory. " + errorSuffix);
            }
            if (getFileHandler().isDirectoryEmpty(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                        + "] directory is empty and it shouldn't be. "
                        + errorSuffix);
            }
        }

        for (Iterator it = requiredFiles.iterator(); it.hasNext();)
        {
            String file = (String) it.next();
            if (!getFileHandler().exists(file))
            {
                throw new ContainerException(errorPrefix + "The [" + file
                        + "] file doesn't exist. " + errorSuffix);
            }
        }
    }

    /**
     * Check the WLS installation directory setting and if the beaHome attribute
     * is not set, guess it.
     */
    public final void initBeaHome()
    {

        if (this.getHome() == null)
        {
            throw new ContainerException("Please set container home to WL_HOME");
        }

        if (this.getBeaHome() == null)
        {
            this.setBeaHome(this.getConfiguration().getPropertyValue(
                    WebLogicPropertySet.BEA_HOME));
        }

        // If the beaHome attribute is not set, guess the bea home
        // directory using the parent directory of the weblogic home
        if (this.getBeaHome() == null)
        {
            this.setBeaHome(new File(this.getHome()).getParent());
        }

    }
    
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    public final void doStart(final Java java) throws Exception
    {
        initBeaHome();      
        
        // Weblogic looks for files relative to the domain home, which is not
        // necessarily relative
        // to the Bea home
        java.setDir(new File(getConfiguration().getHome()));

        File serverDir = new File(this.getHome(), "server");

        if (getConfiguration().getPropertyValue(ServletPropertySet.PORT) != null)
        {
            java.addSysproperty(getAntUtils().createSysProperty(
                    "weblogic.ListenPort",
                    getConfiguration()
                            .getPropertyValue(ServletPropertySet.PORT)));
        }
        if (getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) != null)
        {
            java.addSysproperty(getAntUtils().createSysProperty(
                    "weblogic.ListenAddress",
                    getConfiguration()
                            .getPropertyValue(GeneralPropertySet.HOSTNAME)));
        }
        java.addSysproperty(getAntUtils()
                .createSysProperty(
                        "weblogic.name",
                        getConfiguration().getPropertyValue(
                                WebLogicPropertySet.SERVER)));
        java.addSysproperty(getAntUtils().createSysProperty("bea.home",
                this.getBeaHome()));
        java.addSysproperty(getAntUtils().createSysProperty(
                "weblogic.management.username",
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_USER)));
        java.addSysproperty(getAntUtils().createSysProperty(
                "weblogic.management.password",
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_PWD)));

        // Note: The "=" in the call below is on purpose. It is left so that
        // we end up with:
        // -Djava.security.policy==./server/lib/weblogic.policy
        // (otherwise, we would end up with:
        // -Djava.security.policy=./server/lib/weblogic.policy, which
        // will not add to the security policy but instead replace it).
        java.addSysproperty(getAntUtils().createSysProperty(
                "java.security.policy",
                "=" + serverDir + "/lib/weblogic.policy"));

        Path classpath = java.getCommandLine().getClasspath();
        classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic_sp.jar"));
        classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic.jar"));

        // The WebLogic startup scripts automatically includes the domain root folder in the
        // classpath. This folder is a common place to include configuration files, property files,
        // log4j configurations, etc. This is why we're adding it here.
        classpath.createPathElement().setLocation(
                new File(getConfiguration().getHome()));


        // Add the tools jar to the classpath so deployment will succeed due to appc compiles
        addToolsJarToClasspath(classpath);
       
        java.setClassname("weblogic.Server");

        AntContainerExecutorThread webLogicRunner = new AntContainerExecutorThread(
                java);
        webLogicRunner.start();
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(Java)
     */
    public final void doStop(final Java java) throws Exception
    {
        File serverDir = new File(this.getHome(), "server");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic_sp.jar"));
        classpath.createPathElement().setLocation(
                new File(serverDir, "lib/weblogic.jar"));

        java.setClassname("weblogic.Admin");
        java.createArg().setValue("-url");
        java.createArg().setValue(
                "t3://" + getConfiguration().getPropertyValue(
                GeneralPropertySet.HOSTNAME)
                        + ":"
                        + getConfiguration().getPropertyValue(
                                ServletPropertySet.PORT));
        java.createArg().setValue("-username");
        java.createArg().setValue(
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_USER));
        java.createArg().setValue("-password");
        java.createArg().setValue(
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_PWD));

        // Forcing WebLogic shutdown to speed up the shutdown process
        java.createArg().setValue("FORCESHUTDOWN");

        AntContainerExecutorThread webLogicRunner = new AntContainerExecutorThread(
                java);
        webLogicRunner.start();
    }

    /**
     * @return the BEA_HOME
     * 
     * @see org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer#getDomainHome()
     */
    public String getBeaHome()
    {
        return beaHome;
    }

    /**
     * @return the DOMAIN_HOME
     * 
     * @see org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer#getDomainHome()
     */
    public String getDomainHome()
    {
        return getConfiguration().getHome();
    }

    /**
     * @return the WL_HOME
     * 
     * @see org.codehaus.cargo.container.weblogic.internal.WebLogicLocalContainer#getWeblogicHome()
     */
    public String getWeblogicHome()
    {
        return getHome();
    }
}
