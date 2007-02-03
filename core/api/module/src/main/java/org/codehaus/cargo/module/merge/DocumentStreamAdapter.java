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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.w3c.dom.Document;
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
public class DocumentStreamAdapter extends AbstractDescriptorIo implements MergeProcessor
{
    /**
     * The next item to merge.
     */
    private MergeProcessor next;

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
            
            OutputFormat outputFormat = new OutputFormat(doc);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            XMLSerializer serializer = new XMLSerializer(out, outputFormat);
            serializer.serialize(doc);

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
     * @throws ParserConfigurationException on parse exception
     * @throws SAXException on sax exception
     * @throws IOException on IO exception
     */
    protected Document getDocument(InputStream theInput) throws ParserConfigurationException,
        SAXException, IOException
    {
        DocumentBuilder builder = createDocumentBuilder();
        return builder.parse(theInput);
    }

}
