/* 
 * ========================================================================
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
package org.codehaus.cargo.module.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.codehaus.cargo.module.DescriptorIo;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Adapter class to convert streams into documents.
 * 
 * This is needed because the DocumentMerger understands DOM Documents, but items
 * in War files are accessed as streams - therefore use this wrapper to pass 
 * streams into the DocumentMerger (or subclass).
 * 
 * @version $Id: $
 */
public class DocumentStreamAdapter implements MergeProcessor
{
    /**
     * The next item to merge.
     */
    private MergeProcessor next;

    /**
     * The Descriptor IO.
     */
    private DescriptorIo   descriptorIo;
    
    /**
     * constructor.
     * 
     * @param next the processor to adapt
     */
    public DocumentStreamAdapter(MergeProcessor next)
    {
        this.next = next;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#addMergeItem(java.lang.Object)
     */
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (mergeItem instanceof InputStream)
        {
            try
            {
                this.next.addMergeItem(getDocument((InputStream) mergeItem));
            }
            catch (Exception e)
            {
                throw new MergeException("Exception creating document", e);
            }
        }

        else
        {
            throw new MergeException("DocumentMerger can only merge InputStreams");
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#performMerge()
     */
    public Object performMerge() throws MergeException
    {
        try
        {
            Document doc = (Document) this.next.performMerge();

            if (doc == null)
            {
                return null;
            }
            
            XMLOutputter serializer = new XMLOutputter();
            Format format = Format.getPrettyFormat();
           
            ByteArrayOutputStream out = new ByteArrayOutputStream();     
            serializer.setFormat(format);
            serializer.output(doc, out);
                       
            byte[] data = out.toByteArray();
            return new ByteArrayInputStream(data);
        }
        catch (Exception e)
        {
            throw new MergeException("Error whilst merging documents", e);
        }
    }

    /**
     * Parse the input stream into a document.
     * 
     * @param theInput in the InputStream to read
     * @return Document generated from the stream
     * @throws IOException on IO exception
     * @throws JDOMException if there is an XML problem
     */
    protected Document getDocument(InputStream theInput) throws 
        IOException, JDOMException
    {
        if (descriptorIo == null)
        {
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            
            // We don't know what the DTD of the document is, so we won't have a local
            // copy - so we don't want to fail if we can't get it!
            
            builder.setEntityResolver(new EntityResolver()
            {
                public InputSource resolveEntity(String thePublicId, 
                    String theSystemId) throws SAXException
                {
                    return new InputSource(new StringReader(""));
                }
            });
            
            return builder.build(theInput);
        }
      
        return descriptorIo.createDocumentBuilder().build(theInput);

    }

    /**
     * @return the descriptorIo
     */
    public DescriptorIo getDescriptorIo()
    {
        return this.descriptorIo;
    }

    /**
     * @param descriptorIo the descriptorIo to set
     */
    public void setDescriptorIo(DescriptorIo descriptorIo)
    {
        this.descriptorIo = descriptorIo;
    }
        
}
