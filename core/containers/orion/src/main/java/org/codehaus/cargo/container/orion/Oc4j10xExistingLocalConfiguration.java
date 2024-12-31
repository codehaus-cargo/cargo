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
package org.codehaus.cargo.container.orion;

import java.io.File;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.orion.internal.Oc4jExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.ZipCompressor;

/**
 * Existing local configuration for the OC4J 10.x application server.
 */
public class Oc4j10xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{

    /**
     * Capability of the OC4J existing configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Oc4jExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public Oc4j10xExistingLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        String autoDeployDirSetting = getPropertyValue(Oc4jPropertySet.AUTO_DEPLOY_DIR);
        if (autoDeployDirSetting == null)
        {
            throw new CargoException("Can not start container without the "
                    + Oc4jPropertySet.AUTO_DEPLOY_DIR + " property set");
        }
        File autoDeployDir = new File(autoDeployDirSetting);

        String appDir = autoDeployDir.getAbsolutePath();

        // Deploy all deployables into the applications directory
        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() == DeployableType.EAR)
            {
                getFileHandler().copyFile(deployable.getFile(),
                    getFileHandler().append(appDir,
                        getFileHandler().getName(deployable.getFile())), true);
            }
            else
            {
                throw new CargoException(
                    "Only deployables of type " + DeployableType.EAR + " are supported");
            }
        }

        // Deploy the cargocpc web-app by packaging it as an EAR and auto-deploy
        String earDirectory = getFileHandler().createUniqueTmpDirectory();
        String metaInf = getFileHandler().append(earDirectory, "META-INF");
        getFileHandler().mkdirs(metaInf);
        getResourceUtils().copyResource(
            RESOURCE_PATH + "cargocpc.war",
                getFileHandler().append(earDirectory, "cargocpc.war"), getFileHandler());
        getResourceUtils().copyResource(
            RESOURCE_PATH + "oc4j10x/application.xml",
                getFileHandler().append(metaInf, "application.xml"), getFileHandler());
        ZipCompressor compressor = new ZipCompressor(getFileHandler());
        compressor.compress(earDirectory, getFileHandler().append(appDir, "cargocpc.ear"));
        getFileHandler().delete(earDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }
}
