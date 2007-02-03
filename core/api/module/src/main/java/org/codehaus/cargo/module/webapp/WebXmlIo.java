/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading and writing web deployment descriptors.
 *
 * @version $Id$
 */
public final class WebXmlIo extends AbstractDescriptorIo
{
    /**
     * Utility class should not have a public or default constructor.
     */
    private WebXmlIo()
    {
        // Voluntarily empty constructor as utility classes should not have a public or default
        // constructor
    }

    /**
     * Implementation of the SAX EntityResolver interface that looks up the web-app DTDs from the
     * JAR.
     */
    private static class WebXmlEntityResolver implements EntityResolver
    {
        /**
         * {@inheritDoc}
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            WebXmlVersion version = WebXmlVersion.valueOf(thePublicId);
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
     * Creates a new empty deployment descriptor.
     *
     * @param theVersion The version of the descriptor to create
     *
     * @return The new descriptor
     *
     * @throws ParserConfigurationException If the XML parser was not correctly configured
     */
    public static WebXml newWebXml(WebXmlVersion theVersion)
        throws ParserConfigurationException
    {
        DocumentBuilder builder = createDocumentBuilder();
        DocumentType docType = null;
        if (theVersion != null)
        {
            docType = builder.getDOMImplementation().createDocumentType("web-app",
                theVersion.getPublicId(), theVersion.getSystemId());
        }
        Document doc = builder.getDOMImplementation().createDocument("", "web-app", docType);
        return new WebXml(doc);
    }

    /**
     * Parses a deployment descriptor stored in a regular file.
     *
     * @param theFile The file to parse
     * @param theEntityResolver A SAX entity resolver, or <code>null</code> to use the default
     *
     * @return The parsed descriptor
     *
     * @throws SAXException If the file could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly configured
     * @throws IOException If an I/O error occurs
     */
    public static WebXml parseWebXmlFromFile(File theFile,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(theFile);
            return parseWebXml(in, theEntityResolver);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }

    /**
     * Parses a deployment descriptor provided as input stream.
     *
     * @param theInput The input stream
     * @param theEntityResolver A SAX entity resolver, or <code>null</code> to use the default
     *
     * @return The parsed descriptor
     *
     * @throws SAXException If the input could not be parsed
     * @throws ParserConfigurationException If the XML parser was not correctly configured
     * @throws IOException If an I/O error occurs
     */
    public static WebXml parseWebXml(InputStream theInput,
        EntityResolver theEntityResolver)
        throws SAXException, ParserConfigurationException, IOException
    {
        DocumentBuilder builder = createDocumentBuilder();
        if (theEntityResolver != null)
        {
            builder.setEntityResolver(theEntityResolver);
        }
        else
        {
            builder.setEntityResolver(new WebXmlEntityResolver());
        }
        return new WebXml(builder.parse(theInput));
    }
}
