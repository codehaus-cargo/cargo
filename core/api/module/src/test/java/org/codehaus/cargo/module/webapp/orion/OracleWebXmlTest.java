/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.orion;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for {@link OracleWebXml}.
 *
 * @version $Id$
 */
public class OracleWebXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests that a ejb reference description can be added
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescription() throws Exception
    {
        String xml = "<orion-web-app></orion-web-app>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        OrionWebXml descr = new OrionWebXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        NodeList nl = descr.getDocument().getElementsByTagName("ejb-ref-mapping");
        Element n = (Element)nl.item(0);
        assertEquals("foo", n.getAttribute("name"));
        assertEquals("fee", n.getAttribute("location"));
        assertEquals(1, nl.getLength());
    }

}
