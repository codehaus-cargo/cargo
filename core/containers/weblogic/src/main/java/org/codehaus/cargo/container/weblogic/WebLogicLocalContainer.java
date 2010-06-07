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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.LocalContainer;

/**
 * All WebLogic configuration implementations must implement this interface
 * which provides method to find out the location of key directories needed to
 * operate WebLogic.
 * 
 * @version $Id$
 */
public interface WebLogicLocalContainer extends LocalContainer
{
    /**
     * The BEA_HOME is a root directory for various versions of WebLogic server.
     * Multiple BEA_HOMEs may exist on a machine, and any changes to this folder
     * will affect all configurations that use it. The BEA_HOME is read-only to
     * running processes. It is modified when patching or installing new
     * versions of WebLogic.
     * 
     * @return The BEA_HOME of this WebLogic installation.
     */
    String getBeaHome();

    /**
     * There are one or many WL_HOMEs per BEA_HOME. This path contains the
     * versioned WebLogic libraries used by running configurations. This area is
     * typically read-only to running processes. It is modified when patching or
     * adding new extensions to an existing version of WebLogic.
     * 
     * @return The WL_HOME, or version-specific installation
     */
    String getWeblogicHome();

    /**
     * When valid deployment files are written to auto-deploy directory,
     * WebLogic will deploy and start them automatically. This mechanism only
     * works when ProductionMode is disabled, on single-server domains.
     * 
     * @return The auto-deploy directory
     */
    String getAutoDeployDirectory();
}
