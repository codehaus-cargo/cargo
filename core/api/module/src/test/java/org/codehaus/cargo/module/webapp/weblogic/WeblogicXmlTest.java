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
package org.codehaus.cargo.module.webapp.weblogic;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for {@link WeblogicXml}.
 *
 * @version $Id$
 */
public class WeblogicXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests that a ejb reference description can be added
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescription() throws Exception
    {
        String xml = "<weblogic-web-app>"
            + "  <reference-descriptor>"
            + "  </reference-descriptor>"
            + "</weblogic-web-app>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicXml descr = new WeblogicXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        NodeList nl = descr.getDocument().getElementsByTagName(WeblogicXmlTag.REFERENCE_DESCRIPTOR.getTagName());
        Element n = (Element)nl.item(0);
        assertEquals("reference-descriptor", n.getNodeName());
        n = (Element)n.getElementsByTagName(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION.getTagName()).item(0);
        assertEquals("ejb-reference-description", n.getNodeName());
        Element m = (Element)n.getElementsByTagName(WeblogicXmlTag.EJB_REF_NAME.getTagName()).item(0);
        assertEquals("ejb-ref-name", m.getNodeName());
        assertEquals("foo", m.getFirstChild().getNodeValue());
        m = (Element)n.getElementsByTagName(WeblogicXmlTag.JNDI_NAME.getTagName()).item(0);
        assertEquals("jndi-name", m.getNodeName());
        assertEquals("fee", m.getFirstChild().getNodeValue());
    }

    /**
     * Tests that a ejb reference description can be added
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescriptionWithNoReferenceDescriptor()
        throws Exception
    {
        String xml = "<weblogic-web-app>"
            + "  <run-as-role-assignment/>"
            + "  <session-descriptor/>"
            + "</weblogic-web-app>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicXml descr = new WeblogicXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List elements = getAllElements(descr.getDocument().getFirstChild());
        Element n = (Element)elements.get(0);
        assertEquals("run-as-role-assignment", n.getNodeName());
        n = (Element)elements.get(2);
        assertEquals("session-descriptor", n.getNodeName());
        n = (Element)elements.get(1);
        assertEquals("reference-descriptor", n.getNodeName());
        n = (Element)n.getElementsByTagName(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION.getTagName()).item(0);
        assertEquals("ejb-reference-description", n.getNodeName());
        Element m = (Element)n.getElementsByTagName(WeblogicXmlTag.EJB_REF_NAME.getTagName()).item(0);
        assertEquals("ejb-ref-name", m.getNodeName());
        assertEquals("foo", m.getFirstChild().getNodeValue());
        m = (Element)n.getElementsByTagName(WeblogicXmlTag.JNDI_NAME.getTagName()).item(0);
        assertEquals("jndi-name", m.getNodeName());
        assertEquals("fee", m.getFirstChild().getNodeValue());
    }

    /**
     * Tests that a ejb reference description can be added
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescriptionWhenOtherDescriptionsExists()
        throws Exception
    {
        String xml = "<weblogic-web-app>"
            + "  <reference-descriptor>"
            + "    <resource-description/>"
            + "    <resource-env-description/>"
            + "  </reference-descriptor>"
            + "</weblogic-web-app>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        WeblogicXml descr = new WeblogicXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        NodeList nl = descr.getDocument().getElementsByTagName(WeblogicXmlTag.REFERENCE_DESCRIPTOR.getTagName());
        Element n = (Element)nl.item(0);
        assertEquals("reference-descriptor", n.getNodeName());
        List elements = getAllElements(n);
        n = (Element)elements.get(0);
        assertEquals("resource-description", n.getNodeName());
        n = (Element)elements.get(1);
        assertEquals("resource-env-description", n.getNodeName());
        n = (Element)elements.get(2);
        assertEquals("ejb-reference-description", n.getNodeName());
    }

    private List getAllElements(Node n)
    {
        List elements = new ArrayList();

        NodeList nl = n.getChildNodes();
        for(int i=0; i<nl.getLength(); i++)
        {
            if(nl.item(i) instanceof Element)
            {
                elements.add(nl.item(i));
            }
        }

        return elements;
    }
}
