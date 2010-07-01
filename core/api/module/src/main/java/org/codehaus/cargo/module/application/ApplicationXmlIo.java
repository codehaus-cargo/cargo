/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides convenience methods for reading and writing enterprise application deployment 
 * descriptors (application.xml).
 *
 * @version $Id$
 */
public final class ApplicationXmlIo extends AbstractDescriptorIo
{
    /**
     * Utility class should not have a public or default constructor.
     */
    private ApplicationXmlIo()
    {
      super(ApplicationXmlType.getInstance());   
    }
    
    /**
     * Implementation of the SAX EntityResolver interface that looks up the application DTDs from 
     * the JAR.
     */
    private static class ApplicationXmlEntityResolver implements EntityResolver
    {
        /**
         * {@inheritDoc}
         * @see org.xml.sax.EntityResolver#resolveEntity
         */
        public InputSource resolveEntity(String thePublicId, String theSystemId)
            throws SAXException, IOException
        {
            ApplicationXmlVersion version = ApplicationXmlVersion.valueOf(thePublicId);
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
     * Parses a deployment descriptor stored in a regular file.
     * 
     * @param file The file to parse
     * @param entityResolver A SAX entity resolver, or <code>null</code> to use the default
     * @return The parsed descriptor
     * @throws IOException If an I/O error occurs
     * @throws JDOMException If the file could not be parsed
     */
    public static ApplicationXml parseApplicationXmlFromFile(File file,
        EntityResolver entityResolver)
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            return parseApplicationXml(in, entityResolver);
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
     * @param input The input stream
     * @param entityResolver A SAX entity resolver, or <code>null</code> to use the default
     * @return The parsed descriptor
     * @throws IOException If an I/O error occurs
     * @throws JDOMException If the input could not be parsed
     */
    public static ApplicationXml parseApplicationXml(InputStream input,
        EntityResolver entityResolver)
        throws IOException, JDOMException
    {
        ApplicationXmlIo io = new ApplicationXmlIo();  
        SAXBuilder builder = io.createDocumentBuilder();
        
        if (entityResolver != null)
        {
            builder.setEntityResolver(entityResolver);
        }
        else
        {
            builder.setEntityResolver(new ApplicationXmlEntityResolver());
        }
        return (ApplicationXml) builder.build(input);
    }    
    
    /**
     * Writes the specified document to a file.
     * 
     * @param appXml The descriptor to serialize
     * @param file The file to write to
     * @throws IOException If an I/O error occurs
     */
    public static void writeApplicationXml(ApplicationXml appXml, 
                                           File file)
        throws IOException
    {
        writeApplicationXml(appXml, file, null, false);
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param appXml The descriptor to serialize
     * @param file The file to write to
     * @param encoding The character encoding to use
     * @throws IOException If an I/O error occurs
     */
    public static void writeApplicationXml(ApplicationXml appXml, 
                                           File file,
                                           String encoding)
        throws IOException
    {
        writeApplicationXml(appXml, file, encoding, false);
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param appXml The descriptor to serialize
     * @param file The file to write to
     * @param encoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     * @throws IOException If an I/O error occurs
     */
    public static void writeApplicationXml(ApplicationXml appXml, 
                                           File file,
                                           String encoding, 
                                           boolean isIndent)
        throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            writeApplicationXml(appXml, out, encoding, isIndent);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {
                    // we'll pass on the original IO error, so ignore this one
                }
            }
        }
    }

    /**
     * Writes the specified document to an output stream.
     * 
     * @param appXml The descriptor to serialize
     * @param output The output stream to write to
     * @param encoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     * @throws IOException If an I/O error occurs
     */
    public static void writeApplicationXml(ApplicationXml appXml, 
                                           OutputStream output,
                                           String encoding, 
                                           boolean isIndent)
        throws IOException
    {
        Format format = Format.getPrettyFormat();
        if (encoding != null)
        {
            format.setEncoding(encoding);
        }

        XMLOutputter serializer = new XMLOutputter(format);

        serializer.output(appXml.getDocument(), output);
    }

}
