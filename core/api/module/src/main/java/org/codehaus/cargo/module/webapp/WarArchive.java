/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
     * @throws SAXException If the deployment descriptor of the WAR could not be parsed
     * @throws ParserConfigurationException If there is an XML parser configuration problem
     */
    WebXml getWebXml() throws IOException, SAXException, ParserConfigurationException;

    /**
     * Stores the war archive to file. Changes to the descriptors of the war archive will be stored
     * as well.
     *
     * @param warFile file to store the war in.
     *
     * @throws IOException If there was a problem reading the  deployment descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not be parsed
     * @throws ParserConfigurationException If there is an XML parser configuration problem
     */
    void store(File warFile) throws IOException, SAXException, ParserConfigurationException;
}
