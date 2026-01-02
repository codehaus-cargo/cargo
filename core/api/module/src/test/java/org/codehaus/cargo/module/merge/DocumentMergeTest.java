/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * Test class which verifies that the DocumentMerge class is merging two Document objects correctly.
 */
public class DocumentMergeTest
{

    /**
     * Test to see that two documents are merged correctly.
     * 
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testMergeTwoDocuments() throws Exception
    {
        SAXBuilder sb = new SAXBuilder();
        String doc1 = "<topnode><parentnode><value>8</value></parentnode></topnode>";
        String doc2 = "<topnode><parentnode><value>13</value></parentnode></topnode>";
        String doc3 = "<topnode><parentnode><value>26</value></parentnode></topnode>";

        // build two different documents
        Document saxDoc1 = sb.build(new StringReader(doc1));
        Document saxDoc2 = sb.build(new StringReader(doc2));
        Document saxDoc3 = sb.build(new StringReader(doc3));

        // add them to DocumentMerger object
        DocumentMerger merger = new DocumentMerger();
        merger.addMergeItem(saxDoc1);
        merger.addMergeItem(saxDoc2);
        merger.addMergeItem(saxDoc3);

        // perform the merge
        Document mergedDoc = (Document) merger.performMerge();

        // verify this one Document has both entries
        Element rootNode = mergedDoc.getRootElement();
        List<Element> list = rootNode.getChildren("parentnode");
        Assertions.assertNotNull(list);
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals(((Element) list.get(0)).getChildText("value"), "8");
        Assertions.assertEquals(((Element) list.get(1)).getChildText("value"), "13");
        Assertions.assertEquals(((Element) list.get(2)).getChildText("value"), "26");
    }
}
