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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.JarArchive;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;

/**
 * Encapsulates access to a WAR.
 *
 * @version $Id$
 */
public interface WarArchive extends JarArchive
{
    /**
     * Returns the deployment descriptor of the web application.
     *
     * @return The parsed deployment descriptor
     *
     * @throws IOException If there was a problem reading the  deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    WebXml getWebXml() throws IOException, JDOMException;

    /**
     * Stores the war archive to file. Changes to the descriptors of the war archive will be stored
     * as well.
     *
     * @param warFile file to store the war in.
     *
     * @throws IOException If there was a problem reading the  deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    void store(File warFile) throws IOException, JDOMException;
}
