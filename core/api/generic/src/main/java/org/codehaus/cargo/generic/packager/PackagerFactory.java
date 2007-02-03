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
package org.codehaus.cargo.generic.packager;

import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.container.packager.PackagerType;

/**
 * Create a {@link org.codehaus.cargo.container.packager.Packager} instance for a given container.
 * 
 * @version $Id: $
 */
public interface PackagerFactory
{
    /**
     * Registers a {@link org.codehaus.cargo.container.packager.Packager} implementation.
     *
     * @param containerId the container attached to this packager
     * @param packagerType the packager's type (directory, zip, etc)
     * @param packagerClass the packager implementation class to register
     */
    void registerPackager(String containerId, PackagerType packagerType, Class packagerClass);

    /**
     * @param containerId the container attached to this packager class
     * @param packagerType the type to differentiate this packager from others for the specified
     *        container
     * @return true if the specified packager is already registered or false otherwise 
     */
    boolean isPackagerRegistered(String containerId, PackagerType packagerType);

    /**
     * Create a {@link org.codehaus.cargo.container.packager.Packager} instance matching the
     * specified container id.
     *
     * @param containerId the container for which we need to create a packager instance
     * @param packagerType the packager's type (directory, zip, etc)
     * @param outputLocation the location where the package will be generated. For example for
     *        a Directory Packager this will be the directory into which the package will be
     *        generated. 
     * @return the packager instance
     */
    Packager createPackager(String containerId, PackagerType packagerType, String outputLocation);
}
