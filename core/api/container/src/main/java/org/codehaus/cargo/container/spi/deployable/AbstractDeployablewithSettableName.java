/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.spi.deployable;

import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Common class for easy Deployable implementations with settable names.
 */
public abstract class AbstractDeployablewithSettableName extends AbstractDeployable
{
    /**
     * The name of this deployable (it can be anything, there's no special rule). If not specified
     * by user, it is computed from the EAR's file name (removing the filename extension).
     */
    private String name;

    /**
     * @param file the location of the deployable file being wrapped.
     */
    public AbstractDeployablewithSettableName(String file)
    {
        super(file);
        this.name = super.getName();
    }

    /**
     * Sets the name of this deployable. It can be anything (there's no special rule), except
     * <code>null</code> or an empty string.
     * @param name the name of this deployable
     */
    public void setName(String name)
    {
        if (name == null || DefaultFileHandler.sanitizeFilename(name, getLogger()).isEmpty())
        {
            throw new NullPointerException("Custom deployable name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * Default value is the Deployable sanitized {@link #getName()} with the deployable type as
     * extension.
     */
    @Override
    public String getFilename()
    {
        return DefaultFileHandler.sanitizeFilename(
            getName() + "." + getType().getType(), getLogger());
    }
}
