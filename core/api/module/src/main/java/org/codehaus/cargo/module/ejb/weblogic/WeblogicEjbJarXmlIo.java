/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.module.ejb.weblogic;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.jdom2.JDOMException;

/**
 * Provides convenience methods for reading WebLogic ejb jar deployment descriptor.
 */
public class WeblogicEjbJarXmlIo extends AbstractDescriptorIo
{
    /**
     * Protected Constrictor.
     */
    protected WeblogicEjbJarXmlIo()
    {
        super(WeblogicEjbJarXmlType.getInstance());
    }

    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param input The input stream
     * @return The parsed descriptor
     * @throws IOException If an I/O error occurs
     * @throws JDOMException If an XML Parsing problem
     */
    public static WeblogicEjbJarXml parseWeblogicEjbJarXml(InputStream input)
        throws IOException, JDOMException
    {
        WeblogicEjbJarXmlIo xio = new WeblogicEjbJarXmlIo();
        return (WeblogicEjbJarXml) xio.parseXml(input);
    }

}
