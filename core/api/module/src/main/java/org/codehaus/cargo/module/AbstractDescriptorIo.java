/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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
package org.codehaus.cargo.module;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides convenience methods for reading and writing web deployment descriptors.
 *
 * @version $Id$
 */
public abstract class AbstractDescriptorIo
{
    /**
     * Utility class should not have a public or default constructor.
     */
    protected AbstractDescriptorIo()
    {
        // Voluntarily empty constructor as utility classes should not have a public or default
        // constructor
    }

    /**
     * @return a new non-validating, non-namespace-aware {@link DocumentBuilder} instance
     * @throws ParserConfigurationException in case of error
     */
    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new XmlEntityResolver());

        return documentBuilder;
    }

    /**
     * Writes the specified document to a file.
     *
     * @param descriptor The descriptor to serialize
     * @param file The file to write to
     *
     * @throws IOException If an I/O error occurs
     */
    public static void writeDescriptor(Descriptor descriptor, File file) throws IOException
    {
        writeDescriptor(descriptor, file, null, false);
    }

    /**
     * Writes the specified document to a file.
     *
     * @param descriptor The descriptor to serialize
     * @param file The file to write to
     * @param encoding The character encoding to use
     * @throws IOException If an I/O error occurs
     */
    public static void writeDescriptor(Descriptor descriptor, File file, String encoding)
        throws IOException
    {
        writeDescriptor(descriptor, file, encoding, false);
    }

    /**
     * Writes the specified document to a file.
     *
     * @param descriptor The descriptor to serialize
     * @param file The file to write to
     * @param encoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     * @throws IOException If an I/O error occurs
     */
    public static void writeDescriptor(Descriptor descriptor, File file, String encoding,
        boolean isIndent) throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            writeDescriptor(descriptor, out, encoding, isIndent);
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
     * @param descriptor The descriptor to serialize
     * @param out The output stream to write to
     * @param encoding The character encoding to use
     * @param isIndent Whether the written XML should be indented
     *
     * @throws IOException If an I/O error occurs
     */
    public static void writeDescriptor(Descriptor descriptor, OutputStream out, String encoding,
        boolean isIndent) throws IOException
    {
        OutputFormat outputFormat =
            new OutputFormat(descriptor.getDocument());
        if (encoding != null)
        {
            outputFormat.setEncoding(encoding);
        }
        outputFormat.setIndenting(isIndent);
        outputFormat.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(out, outputFormat);
        serializer.serialize(descriptor.getDocument());
    }

    /**
     * Writes the WebXml and its associated vendor descriptors to the specified directory.
     *
     * @param descriptor The descriptor to serialize
     * @param dir Directory to store the descriptors in
     * @return Array of files for every created file
     * @throws IOException if a I/O error occurs
     */
    public static File[] writeAll(J2eeDescriptor descriptor, String dir) throws IOException
    {
        List files = new ArrayList();
        File webXmlFile = new File(dir, "web.xml");
        writeDescriptor(descriptor, webXmlFile, null, true);
        files.add(webXmlFile);
        Iterator vendorDescriptors = descriptor.getVendorDescriptors();
        while (vendorDescriptors.hasNext())
        {
            Descriptor descr = (Descriptor) vendorDescriptors.next();
            File file = new File(dir, descr.getFileName());
            AbstractDescriptorIo.writeDescriptor(descr, file, null, true);
            files.add(file);
        }

        return (File[]) files.toArray(new File[files.size()]);
    }
}
