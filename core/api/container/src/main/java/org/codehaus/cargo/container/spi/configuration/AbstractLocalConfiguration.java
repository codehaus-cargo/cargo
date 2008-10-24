/*
 * ========================================================================
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
package org.codehaus.cargo.container.spi.configuration;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.resource.Resource;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.DefaultFileHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} that can be
 * specialized for standalone configuration, existing configuration or other local configurations.
 *
 * @version $Id$
 */
public abstract class AbstractLocalConfiguration extends AbstractConfiguration
    implements LocalConfiguration
{
    /**
     * The path under which the container resources are stored in the JAR.
     */
    protected static final String RESOURCE_PATH =
        "/org/codehaus/cargo/container/internal/resources/";

    /**
     * List of {@link Deployable}s to deploy into the container.
     */
    private List deployables;

    /**
     * The home directory for the configuration. This is where the associated container will be
     * set up to start and where it will deploy its deployables.
     */
    private String home;

    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * Resource utility class.
     */
    private ResourceUtils resourceUtils;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * List of {@link Resource}s to add to a container.
     */
    private List resources;

    /**
     * @param home the home directory where the container will be set up to start and where it
     *        will deploy its deployables.
     */
    public AbstractLocalConfiguration(String home) 
    {
        super();

        this.deployables = new ArrayList();
        this.fileHandler = new DefaultFileHandler();
        this.antUtils = new AntUtils();
        this.resourceUtils = new ResourceUtils();
        this.resources = new ArrayList();

        this.home = home;
    }

    /**
     * @return the file utility class to use for performing all file I/O.
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the file utility class to use for performing all file I/O.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * @return the Ant utility class
     */
    protected final AntUtils getAntUtils()
    {
        return this.antUtils;
    }

    /**
     * @return the Resource utility class
     */
    protected final ResourceUtils getResourceUtils()
    {
        return this.resourceUtils;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#addDeployable(org.codehaus.cargo.container.deployable.Deployable)
     */
    public synchronized void addDeployable(Deployable newDeployable)
    {
        this.deployables.add(newDeployable);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#getDeployables()
     */
    public List getDeployables()
    {
        return this.deployables;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#getHome()
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     * @see LocalConfiguration#configure(LocalContainer)
     */
    public void configure(LocalContainer container)
    {
        verify();

        try
        {
            doConfigure(container);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName() + " "
                + getType().getType() + " configuration", e);
        }
    }

    /**
     * Implementation of {@link LocalConfiguration#configure(LocalContainer)} that all local
     * configuration using this class must implement. This provides the ability to perform
     * generic actions before and after the container-specific implementation. Another way would
     * be to use AOP...
     *
     * @param container the container to configure
     * @throws Exception if any error is raised during the configuration
     */
    protected abstract void doConfigure(LocalContainer container) throws Exception;

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#addResource(Resource)
     */
    public void addResource(Resource resource)
    {
        this.resources.add(resource);
    }

    /**
     * @return the configured resources for this container.
     */
    public List getResources()
    {
        return this.resources;
    }
}
