/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.EntityResolver;

/**
 * Provides convenience methods for reading and writing descriptors.
 */
public abstract class AbstractDescriptorIo implements DescriptorIo
{
    /**
     * The type of this descriptor IO.
     */
    private DescriptorType factory;

    /**
     * Constructor.
     * 
     * @param descriptorType the descriptor type.
     */
    protected AbstractDescriptorIo(DescriptorType descriptorType)
    {
        // Voluntarily empty constructor as utility classes should not have a public or default
        // constructor
        this.factory = descriptorType;
    }

    /**
     * Create a document builder.
     * @return new document builder
     */
    @Override
    public SAXBuilder createDocumentBuilder()
    {
        return createDocumentBuilder(null);
    }

    /**
     * @param theEntityResolver entity resolver or null
     * @return a new non-validating, non-namespace-aware {@link javax.xml.parsers.DocumentBuilder}
     * instance
     */
    @Override
    public SAXBuilder createDocumentBuilder(EntityResolver theEntityResolver)
    {
        SAXBuilder factory = new SAXBuilder();
        factory.setValidation(false);
        factory.setFactory(this.factory.getJDOMFactory());

        EntityResolver resolver = theEntityResolver;
        if (resolver == null)
        {
            resolver = getEntityResolver();
        }

        if (resolver != null)
        {
            factory.setEntityResolver(resolver);
        }

        return factory;
    }

    /**
     * Get the default entity resolver for this type.
     * @return default resolver, or null if none
     */
    protected EntityResolver getEntityResolver()
    {
        return null;
    }

    /**
     * @param input the input stream
     * @return JDOM Document
     * @throws IOException if problem reading the stream
     * @throws JDOMException if problem parsing the stream
     */
    @Override
    public Document parseXml(InputStream input) throws JDOMException, IOException
    {
        return parseXml(input, new XmlEntityResolver());
    }

    /**
     * Create a document from the input stream and resolver.
     * 
     * @param input the input stream
     * @param resolver entity resolver, or null
     * @return JDOM Document
     * @throws IOException if problem reading the stream
     * @throws JDOMException if problem parsing the stream
     */
    @Override
    public Document parseXml(InputStream input, EntityResolver resolver)
        throws JDOMException, IOException
    {
        SAXBuilder builder = createDocumentBuilder(resolver);
        return builder.build(input);
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
        writeDescriptor(descriptor, file, StandardCharsets.UTF_8, false);
    }

    /**
     * Writes the specified document to a file.
     * 
     * @param descriptor The descriptor to serialize
     * @param file The file to write to
     * @param encoding The character encoding to use
     * @throws IOException If an I/O error occurs
     */
    public static void writeDescriptor(Descriptor descriptor, File file, Charset encoding)
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
    public static void writeDescriptor(Descriptor descriptor, File file, Charset encoding,
        boolean isIndent) throws IOException
    {
        try (OutputStream out = new FileOutputStream(file))
        {
            writeDescriptor(descriptor, out, encoding, isIndent);
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
    public static void writeDescriptor(Descriptor descriptor, OutputStream out, Charset encoding,
        boolean isIndent) throws IOException
    {
        if (encoding == null)
        {
            throw new IllegalArgumentException("Encoding must not be null");
        }

        XMLOutputter serializer = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setEncoding(encoding.name());

        // First, output as a String so we can fix some known issues with output
        serializer.setFormat(format);
        StringWriter writer = new StringWriter();
        serializer.output((Document) descriptor, writer);
        String result = writer.toString();

        // Save the root tag (including namespace) and replace all other xmlns attributes
        String root = descriptor.getRootElement().getName();
        int startRoot = result.indexOf(root);
        int endRoot = result.indexOf('>', startRoot);

        String subString = result.substring(endRoot + 1);
        subString = subString.replaceAll(" xmlns=\".*\"", "");
        StringBuilder sb = new StringBuilder(result.substring(0, endRoot + 1));
        sb.append(subString);
        result = sb.toString();

        // Remove empty extension tags
        result = result.replace("<extension />", "");

        // Then, output the String into the OutputStream
        out.write(result.getBytes(encoding));
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
        List<File> files = new ArrayList<File>();
        File webXmlFile = new File(dir, "web.xml");
        writeDescriptor(descriptor, webXmlFile, StandardCharsets.UTF_8, true);
        files.add(webXmlFile);
        for (Descriptor descr : descriptor.getVendorDescriptors())
        {
            File file = new File(dir, descr.getFileName());
            AbstractDescriptorIo.writeDescriptor(descr, file, StandardCharsets.UTF_8, true);
            files.add(file);
        }

        return files.toArray(new File[files.size()]);
    }
}
