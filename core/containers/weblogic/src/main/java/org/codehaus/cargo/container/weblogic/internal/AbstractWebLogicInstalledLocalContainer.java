/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.weblogic.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

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
    @Override
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
    protected List<String> getBeaHomeFiles()
    {
        List<String> requiredFiles = new ArrayList<String>();
        requiredFiles.add(getFileHandler().append(getBeaHome(), "registry.xml"));
        return requiredFiles;
    }

    /**
     * @return a list of directories that indicate a properly installed BEA_HOME
     */
    protected List<String> getBeaHomeDirs()
    {

        return new ArrayList<String>();
    }

    /**
     * @return a list of files that indicate a properly installed WL_HOME
     */
    protected List<String> getWeblogicHomeFiles()
    {
        List<String> requiredFiles = new ArrayList<String>();
        requiredFiles.add(getFileHandler().append(getWeblogicHome(), "server/lib/weblogic.jar"));
        return requiredFiles;
    }

    /**
     * @return a list of directories that indicate a properly installed WL_HOME
     */
    protected List<String> getWeblogicHomeDirs()
    {
        List<String> requiredDirs = new ArrayList<String>();
        requiredDirs.add(getFileHandler().append(getWeblogicHome(), "server/lib"));
        return requiredDirs;
    }

    /**
     * Verify that the Weblogic home directory structure is valid and throw a ContainerException if
     * not.
     */
    protected void verifyWeblogicHome()
    {
        List<String> requiredDirs = this.getWeblogicHomeDirs();
        List<String> requiredFiles = this.getWeblogicHomeFiles();
        String errorPrefix = "Invalid Weblogic installation. ";
        String errorSuffix = "Make sure the WL_HOME directory you have specified "
                + "points to the right location (It's currently pointing to ["
                + getWeblogicHome() + "])";
        verify(errorPrefix, errorSuffix, requiredDirs, requiredFiles);
    }

    /**
     * Verify that the Bea home directory structure is valid and throw a ContainerException if not.
     */
    protected void verifyBeaHome()
    {
        List<String> requiredDirs = this.getBeaHomeDirs();
        List<String> requiredFiles = this.getBeaHomeFiles();
        String errorPrefix = "Invalid Weblogic installation. ";
        String errorSuffix = "Make sure the BEA_HOME directory you have specified "
                + "points to the right location (It's currently pointing to ["
                + getBeaHome() + "])";
        verify(errorPrefix, errorSuffix, requiredDirs, requiredFiles);
    }

    /**
     * run through a list of expected files and directories that indicate a properly installed
     * product.
     * 
     * @param errorPrefix - Prefix to the ContainerException, if a file or directory is missing
     * @param errorSuffix - Suffix o the above
     * @param requiredDirs - Directories that are required to exist
     * @param requiredFiles - Files that are required to exist
     */
    protected void verify(String errorPrefix, String errorSuffix,
            List<String> requiredDirs, List<String> requiredFiles)
    {
        for (String dir : requiredDirs)
        {
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

        for (String file : requiredFiles)
        {
            if (!getFileHandler().exists(file))
            {
                throw new ContainerException(errorPrefix + "The [" + file
                        + "] file doesn't exist. " + errorSuffix);
            }
        }
    }

    /**
     * Check the WLS installation directory setting and if the beaHome attribute is not set, guess
     * it.
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
     * @see AbstractInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public final void doStart(final JvmLauncher java) throws Exception
    {
        initBeaHome();

        // Weblogic looks for files relative to the domain home, which is not
        // necessarily relative to the Bea home
        File serverDir = new File(this.getHome(), "server");

        if (getConfiguration().getPropertyValue(ServletPropertySet.PORT) != null)
        {
            java.setSystemProperty("weblogic.ListenPort",
                getConfiguration().getPropertyValue(ServletPropertySet.PORT));
        }
        if (getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) != null)
        {
            java.setSystemProperty("weblogic.ListenAddress",
                getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME));
        }
        java.setSystemProperty("weblogic.name",
            getConfiguration().getPropertyValue(WebLogicPropertySet.SERVER));
        java.setSystemProperty("bea.home", this.getBeaHome());
        java.setSystemProperty("weblogic.management.username",
            getConfiguration().getPropertyValue(WebLogicPropertySet.ADMIN_USER));
        java.setSystemProperty("weblogic.management.password",
            getConfiguration().getPropertyValue(WebLogicPropertySet.ADMIN_PWD));

        // Note: The "=" in the call below is on purpose. It is left so that
        // we end up with:
        // -Djava.security.policy==./server/lib/weblogic.policy
        // (otherwise, we would end up with:
        // -Djava.security.policy=./server/lib/weblogic.policy, which
        // will not add to the security policy but instead replace it).
        java.setSystemProperty("java.security.policy", "=" + serverDir + "/lib/weblogic.policy");

        java.addClasspathEntries(new File(serverDir, "lib/weblogic_sp.jar"));
        java.addClasspathEntries(new File(serverDir, "lib/weblogic.jar"));

        // The WebLogic startup scripts automatically includes the domain root folder in the
        // classpath. This folder is a common place to include configuration files, property files,
        // log4j configurations, etc. This is why we're adding it here.
        java.addClasspathEntries(new File(getConfiguration().getHome()));

        // Add the tools jar to the classpath so deployment will succeed due to appc compiles
        addToolsJarToClasspath(java);

        java.setMainClass("weblogic.Server");

        java.start();
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    public final void doStop(final JvmLauncher java) throws Exception
    {
        File serverDir = new File(this.getHome(), "server");

        java.addClasspathEntries(new File(serverDir, "lib/weblogic_sp.jar"));
        java.addClasspathEntries(new File(serverDir, "lib/weblogic.jar"));

        java.setMainClass("weblogic.Admin");
        java.addAppArguments("-url");
        java.addAppArguments(
                "t3://" + getConfiguration().getPropertyValue(
                    GeneralPropertySet.HOSTNAME)
                        + ":"
                        + getConfiguration().getPropertyValue(
                                ServletPropertySet.PORT));
        java.addAppArguments("-username");
        java.addAppArguments(
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_USER));
        java.addAppArguments("-password");
        java.addAppArguments(
                getConfiguration().getPropertyValue(
                        WebLogicPropertySet.ADMIN_PWD));

        // Forcing WebLogic shutdown to speed up the shutdown process
        java.addAppArguments("FORCESHUTDOWN");

        java.start();
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
