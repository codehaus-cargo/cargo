/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import org.codehaus.cargo.module.JarArchive;
import org.jdom.JDOMException;

/**
 * Class that encapsulates access to an EJB JAR.
 * 
 */
public interface EjbArchive extends JarArchive
{
    /**
     * Return the ejb-jar.xml.
     * 
     * @return the EjbJarXml
     * @throws IOException If there was a problem reading the deployment descriptor in the EAR
     * @throws JDOMException If there is a problem with the XML
     */
    EjbJarXml getEjbJarXml() throws IOException, JDOMException;
}
