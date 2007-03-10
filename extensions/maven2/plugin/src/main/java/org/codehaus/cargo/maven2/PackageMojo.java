/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.maven2;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.maven2.configuration.Packager;

/**
 * Package a container distribution, a Configuration and deployed deployables. See
 * {@link org.codehaus.cargo.container.packager.Packager}.
 *
 * @version $Id: $
 * @goal package
 * @requiresDependencyResolution compile
 * @since Maven2 Cargo Plugin v0.3
 */
public class PackageMojo extends AbstractCargoMojo
{
    /**
     * @parameter
     * @see #getPackagerElement()
     */
    private Packager packager;

    /**
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.packager.Packager}. See the
     *         <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2
     *         plugin reference guide</a> and
     *         {@link org.codehaus.cargo.maven2.configuration.Packager} for more details.
     */
    protected Packager getPackagerElement()
    {
        return this.packager;
    }

    /**
     * @param packagerElement the {@link org.codehaus.cargo.container.packager.Packager}
     *         configuration defined by the user
     * @see #getPackagerElement() 
     */
    protected void setPackagerElement(Packager packagerElement)
    {
        this.packager = packagerElement;
    }

    /**
     * {@inheritDoc}
     * @see AbstractCargoMojo#doExecute()
     */
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (container.getType() != ContainerType.INSTALLED)
        {
            throw new MojoExecutionException("Only installed local containers can be packaged");
        }

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;

        // Note: The configuration must have been generated at this point. In the future we might
        // want to raise a warning if it hasn't been.
        
        createPackager(installedContainer).packageContainer(installedContainer);
    }

    protected org.codehaus.cargo.container.packager.Packager createPackager(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        // If no packager output location has been specified, set up a default location in the
        // build directory.
        if (getPackagerElement() == null)
        {
            setPackagerElement(new Packager());
        }

        if (getPackagerElement().getOutputLocation() == null)
        {
            String outputLocation = getFileHandler().append(getCargoProject().getBuildDirectory(),
                "package");
            getPackagerElement().setOutputLocation(outputLocation);
        }

        return getPackagerElement().createPackager(container);
    }
}
