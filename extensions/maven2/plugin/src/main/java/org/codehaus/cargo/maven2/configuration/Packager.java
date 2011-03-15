/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.maven2.configuration;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.generic.packager.DefaultPackagerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Holds configuration data for the <code>&lt;packager&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
 * 
 * @version $Id$
 */
public class Packager
{
    /**
     * The location where the package will be generated. For example for a Directory Packager this
     * will be the directory into which the package will be generated.
     */
    private String outputLocation;

    /**
     * Packager type.
     */
    private String type = PackagerType.DIRECTORY.getType();

    /**
     * Packager implementation.
     */
    private String implementation;

    /**
     * @return Packager implementation.
     */
    public String getImplementation()
    {
        return this.implementation;
    }

    /**
     * @param implementation Packager implementation.
     */
    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    /**
     * @return Packager type.
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @param type Packager type.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return The location where the package will be generated. For example for a Directory
     * Packager this will be the directory into which the package will be generated.
     */
    public String getOutputLocation()
    {
        return this.outputLocation;
    }

    /**
     * @param outputLocation The location where the package will be generated. For example for a
     * Directory Packager this will be the directory into which the package will be generated.
     */
    public void setOutputLocation(String outputLocation)
    {
        this.outputLocation = outputLocation;
    }

    /**
     * Create packager.
     * @param container Container.
     * @return Packager instance.
     * @throws MojoExecutionException If packager implementation not found.
     */
    public org.codehaus.cargo.container.packager.Packager createPackager(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        org.codehaus.cargo.container.packager.Packager packager;
        PackagerFactory factory = new DefaultPackagerFactory();

        PackagerType type = PackagerType.toType(getType());

        // If the user has specified a custom packager class, register it against the
        // default packager factory.
        if (getImplementation() != null)
        {
            try
            {
                Class packagerClass = Class.forName(getImplementation(), true,
                    this.getClass().getClassLoader());
                factory.registerPackager(container.getId(), type, packagerClass);
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new MojoExecutionException("Custom packager implementation ["
                    + getImplementation() + "] cannot be loaded", cnfe);
            }
        }

        packager = factory.createPackager(container.getId(), type, getOutputLocation());

        return packager;
    }
}
