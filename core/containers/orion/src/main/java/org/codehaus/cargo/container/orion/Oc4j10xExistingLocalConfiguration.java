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
package org.codehaus.cargo.container.orion;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.taskdefs.Ear;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.orion.internal.Oc4jExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.orion.internal.Oc4jPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.util.CargoException;

/**
 * Existing local configuration for the OC4J 10.x application server.
 *
 * @version $Id: $
 */
public class Oc4j10xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{

    /**
     * Capability of the OC4J existing configuration.
     */
    private static ConfigurationCapability capability =
        new Oc4jExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     */
    public Oc4j10xExistingLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        FileUtils fileUtils = FileUtils.newFileUtils();

        String autoDeployDirSetting = getPropertyValue(Oc4jPropertySet.AUTO_DEPLOY_DIR);
        if(autoDeployDirSetting == null)
        {
            throw new CargoException("Can not start container without the " +
                                     Oc4jPropertySet.AUTO_DEPLOY_DIR + " property set");
        }
        File autoDeployDir = new File(autoDeployDirSetting);

        String appDir = autoDeployDir.getAbsolutePath();

        // Deploy all deployables into the applications directory
        Iterator it = getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();
            if ((deployable.getType() == DeployableType.EAR))
            {
                fileUtils.copyFile(deployable.getFile(),
                                   getFileHandler().append(appDir, getFileHandler().getName(deployable.getFile())),
                                   null, true);
            }
        }

        // Deploy the cargocpc web-app by packaging it as an EAR and auto-deploy
        Ear ear = (Ear)getAntUtils().createAntTask("ear");
        File tmpDir = new File(getFileHandler().createUniqueTmpDirectory());
        File appXml = new File(tmpDir, "application.xml");
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                                        new File(tmpDir, "cargocpc.war"));
        getResourceUtils().copyResource(RESOURCE_PATH + "oc4j10x/application.xml",
                                        appXml);
        ear.setAppxml(appXml);
        FileSet fileSet = new FileSet();
        fileSet.setDir(tmpDir);
        fileSet.createInclude().setName("cargocpc.war");
        ear.addFileset(fileSet);
        ear.setDestFile(new File(appDir, "cargocpc.ear"));
        ear.execute();

        getFileHandler().delete(tmpDir.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }
}
