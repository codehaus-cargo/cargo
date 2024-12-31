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
package org.codehaus.cargo.util;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Test for MissingXmlElementAppender.
 */
public class MissingXmlElementAppenderTest extends TestCase
{
    /**
     * Nothing will be appended, document is already as expected.
     * @throws Exception if anything goes wrong
     */
    public void testAppendNotNeeded() throws Exception
    {
        String xml = "<Root/>";
        String xPath = "/Root";

        MissingXmlElementAppender missingXmlElementAppender =
                new MissingXmlElementAppender(parse(xml), xPath);
        missingXmlElementAppender.append();
        assertEquals("<Root/>", toString(missingXmlElementAppender.getDocument()));
    }

    /**
     * Assert that one hierarchy level will be added.
     * @throws Exception if anything goes wrong
     */
    public void testAppendBranch() throws Exception
    {
        String xml = "<Root/>";
        String xPath = "/Root/Branch";

        MissingXmlElementAppender missingXmlElementAppender =
                new MissingXmlElementAppender(parse(xml), xPath);
        missingXmlElementAppender.append();
        assertEquals("<Root><Branch/></Root>", toString(missingXmlElementAppender.getDocument()));
    }

    /**
     * Assert that more hierarchy levels will be added
     * @throws Exception if anything goes wrong
     */
    public void testAppendLeave() throws Exception
    {
        String xml = "<Root/>";
        String xPath = "/Root/Branch/Leave";

        MissingXmlElementAppender missingXmlElementAppender =
                new MissingXmlElementAppender(parse(xml), xPath);
        missingXmlElementAppender.append();
        assertEquals("<Root><Branch><Leave/></Branch></Root>",
                toString(missingXmlElementAppender.getDocument()));
    }

    /**
     * Assert that an element with attribute is correctly found
     * @throws Exception if anything goes wrong
     */
    public void testAppendLeaveForBranchWithAttribute() throws Exception
    {
        String xml = "<Root><Branch key='value'/></Root>";
        String xPath = "/Root/Branch[@key='value']/Leave";

        MissingXmlElementAppender missingXmlElementAppender =
                new MissingXmlElementAppender(parse(xml), xPath);
        missingXmlElementAppender.append();
        assertEquals("<Root><Branch key=\"value\"><Leave/></Branch></Root>",
                toString(missingXmlElementAppender.getDocument()));
    }

    /**
     * Assert that an element with a child is correctly found
     * @throws Exception if anything goes wrong
     */
    public void testAppendLeaveForBranchWithChild() throws Exception
    {
        String xml = "<Root><Branch><Child/></Branch></Root>";
        String xPath = "/Root/Branch[Child]/Leave";

        MissingXmlElementAppender missingXmlElementAppender =
                new MissingXmlElementAppender(parse(xml), xPath);
        missingXmlElementAppender.append();
        assertEquals("<Root><Branch><Child/><Leave/></Branch></Root>",
                toString(missingXmlElementAppender.getDocument()));
    }

    /**
     * Parse XML string
     * 
     * @param xml string representation
     * @return DOM document
     * @throws Exception if anything goes wrong
     */
    private Document parse(String xml) throws Exception
    {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Convert the DOM document to its String representation.
     * 
     * @param document DOM that is to be converted
     * @return String representation of the document
     * @throws TransformerException if anything goes wrong
     */
    private String toString(Document document) throws TransformerException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }
}
