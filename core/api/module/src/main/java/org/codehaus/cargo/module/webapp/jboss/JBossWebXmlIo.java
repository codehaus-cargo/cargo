/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.jboss;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading Tomcat web context descriptor.
 * 
 * @version $Id$
 */
public class JBossWebXmlIo extends AbstractDescriptorIo
{
    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param theInput The input stream
     * @return The parsed descriptor
     * @throws SAXException If the input could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly
     *          configured
     * @throws IOException If an I/O error occurs
     */
    public static JBossWebXml parseJBossWebXml(InputStream theInput)
        throws SAXException, ParserConfigurationException, IOException
    {
        return new JBossWebXml(createDocumentBuilder().parse(theInput));
    }
}
