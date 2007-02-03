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
package org.codehaus.cargo.container.packager;

import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * Gathers an installed container distribution and a local configuration in a single location.
 * There can be several packagers like a directory packager which packeg them in a given directory
 * on the file system, a ZIP packager which does the same but packaged as a ZIP file, etc.
 *
 * @version $Id: Container.java 886 2006-02-28 12:40:47Z vmassol $
 */
public interface Packager
{
    /**
     * Package an installed container distribution and its local configuration in a single location.
     *
     * @param container the installed container to package
     */
    void packageContainer(InstalledLocalContainer container);
}
