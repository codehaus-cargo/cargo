/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.module.ejb;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.cargo.module.JarArchive;
import org.xml.sax.SAXException;

/**
 * Class that encapsulates access to an EJB JAR.
 * 
 * @version $Id$
 */
public interface EjbArchive extends JarArchive
{
    /**
     * Return the ejb-jar.xml.
     *
     * @return the EjbJarXml
     * @throws IOException If there was a problem reading the deployment descriptor in the EAR
     * @throws SAXException If the deployment descriptor of the EAR could not be parsed
     * @throws ParserConfigurationException If there is an XML parser configration problem
     */
    EjbJarXml getEjbJarXml() throws IOException, SAXException, ParserConfigurationException;
}
