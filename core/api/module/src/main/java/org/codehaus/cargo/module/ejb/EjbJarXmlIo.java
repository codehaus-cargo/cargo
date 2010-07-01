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
package org.codehaus.cargo.module.ejb;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading and writing ejb deployment descriptors (ejb-jar.xml).
 *
 * @version $Id$
 */
public class EjbJarXmlIo extends AbstractDescriptorIo
{
    /**
     * @param factory
     */
    protected EjbJarXmlIo()
    {
        super(EjbJarXmlType.getInstance());
    }
  
    /**
     * Implementation of the SAX EntityResolver interface that looks up the application DTDs from 
     * the JAR.
     */
    private static class EjbJarXmlEntityResolver implements EntityResolver
    {
        /**
         * {@inheritDoc}
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            EjbJarXmlVersion version = EjbJarXmlVersion.valueOf(thePublicId);
            if (version != null)
            {
                String fileName = version.getSystemId().substring(
                    version.getSystemId().lastIndexOf('/'));
                InputStream in = this.getClass().getResourceAsStream(
                    "/org/codehaus/cargo/module/internal/resource" + fileName);
                if (in != null)
                {
                    return new InputSource(in);
                }
            }
            return null;
        }

    }
    
    /**
     * Parses a deployment descriptor provided as input stream.
     * 
     * @param input The input stream
     * @param entityResolver A SAX entity resolver, or <code>null</code> to use the default
     * @return The parsed descriptor
     * @throws IOException If an I/O error occurs
     * @throws JDOMException If the XML parser was not correctly configured
     */
    public static EjbJarXml parseEjbJarXml(InputStream input,
        EntityResolver entityResolver)
        throws IOException, JDOMException
    {
        EjbJarXmlIo io = new EjbJarXmlIo();
        SAXBuilder builder = io.createDocumentBuilder();
        if (entityResolver != null)
        {
            builder.setEntityResolver(entityResolver);
        }
        else
        {
            builder.setEntityResolver(new EjbJarXmlEntityResolver());
        }
        return (EjbJarXml) builder.build(input);                   
    }

}
