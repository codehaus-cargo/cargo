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
package org.codehaus.cargo.module.merge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.cargo.util.CargoException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 */
public class DocumentMergerByXslt implements MergeProcessor
{
    /**
     * The list of merge documents.
     */
    private List<Document> documents = new ArrayList<Document>();

    /**
     * The XSLT source to use.
     */
    private StreamSource xsltSource;

    /**
     * The lazily-compiled XSLT transformer.
     */
    private Transformer transformer;

    /**
     * Constructor.
     * @param stream XML Stream for source XSLT
     */
    public DocumentMergerByXslt(InputStream stream)
    {
        xsltSource = new StreamSource(stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (mergeItem instanceof Document)
        {
            this.documents.add((Document) mergeItem);
        }
        else
        {
            throw new MergeException("DocumentMerger can only merge Documents");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object performMerge() throws MergeException
    {
        if (this.documents.isEmpty())
        {
            return null;
        }

        Document doc = this.documents.get(0);

        for (int i = 1; i < this.documents.size(); i++)
        {
            doc = merge(doc, this.documents.get(i));
        }

        return doc;
    }

    /**
     * Merge 2 documents by XSLT.
     * 
     * @param left Left hand document
     * @param right Right hand document
     * @return The merged document
     */
    private Document merge(Document left, Document right)
    {
        try
        {
            Document doc = createUnifiedDocument(left, right);

            DOMOutputter outputter = new DOMOutputter();
            org.w3c.dom.Document domDocument = outputter.output(doc);

            javax.xml.transform.Source xmlSource =
                new javax.xml.transform.dom.DOMSource(domDocument);

            if (transformer == null)
            {
                TransformerFactory tFactory = TransformerFactory.newInstance();
                transformer = tFactory.newTransformer(xsltSource);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            StreamResult xmlResult = new StreamResult(baos);

            transformer.transform(xmlSource, xmlResult);

            // PArse it back into a JDOM document
            SAXBuilder factory = new SAXBuilder();
            factory.setValidation(false);

            // We don't know what the DTD of the document is, so we won't have a local
            // copy - so we don't want to fail if we can't get it!

            factory.setEntityResolver(new EntityResolver()
            {
                @Override
                public InputSource resolveEntity(String thePublicId,
                    String theSystemId) throws SAXException
                {
                    return new InputSource(new StringReader(""));
                }
            });

            return factory.build(new ByteArrayInputStream(baos.toByteArray()));
        }
        catch (Exception e)
        {
            throw new CargoException("Exception whilst trying to transform documents", e);
        }
    }

    /**
     * Create a document that has both the left and the right items.
     * 
     * @param left Left Document
     * @param right Right Document
     * @return Merged Document
     */
    private Document createUnifiedDocument(Document left, Document right)
    {
        Document mergedDocument = new Document();

        Element rootElement = new Element("merge");
        Element leftElement = new Element("left");
        Element rightElement = new Element("right");

        rootElement.addContent(leftElement);
        rootElement.addContent(rightElement);

        leftElement.addContent((Element) left.getRootElement().clone());
        rightElement.addContent((Element) right.getRootElement().clone());

        mergedDocument.setRootElement(rootElement);

        return mergedDocument;
    }

}
