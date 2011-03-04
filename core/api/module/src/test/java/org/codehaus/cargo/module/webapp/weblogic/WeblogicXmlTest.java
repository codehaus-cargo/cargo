/*
 * ========================================================================
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
package org.codehaus.cargo.module.webapp.weblogic;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.jdom.Element;

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

        WeblogicXml descr = WeblogicXmlIo
            .parseWeblogicXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List<Element> nl = descr.getDocument().getRootElement()
            .getChildren(WeblogicXmlTag.REFERENCE_DESCRIPTOR);
        Element n = nl.get(0);
        assertEquals("reference-descriptor", n.getName());
        n = (Element) n.getChildren(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION).get(0);
        assertEquals("ejb-reference-description", n.getName());
        Element m = (Element) n.getChildren(WeblogicXmlTag.EJB_REF_NAME).get(0);
        assertEquals("ejb-ref-name", m.getName());
        assertEquals("foo", m.getText());
        m = (Element) n.getChildren(WeblogicXmlTag.JNDI_NAME).get(0);
        assertEquals("jndi-name", m.getName());
        assertEquals("fee", m.getText());
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
        //
        WeblogicXml descr = WeblogicXmlIo
            .parseWeblogicXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));

        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List<Element> elements = getAllElements(descr.getDocument().getRootElement());
        Element n = elements.get(0);
        assertEquals("run-as-role-assignment", n.getName());
        n = elements.get(2);
        assertEquals("session-descriptor", n.getName());
        n = elements.get(1);
        assertEquals("reference-descriptor", n.getName());
        n = (Element) n.getChildren(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION).get(0);
        assertEquals("ejb-reference-description", n.getName());
        Element m = (Element) n.getChildren(WeblogicXmlTag.EJB_REF_NAME).get(0);
        assertEquals("ejb-ref-name", m.getName());
        assertEquals("foo", m.getValue());
        m = (Element) n.getChildren(WeblogicXmlTag.JNDI_NAME).get(0);
        assertEquals("jndi-name", m.getName());
        assertEquals("fee", m.getValue());
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
        WeblogicXml descr = WeblogicXmlIo
            .parseWeblogicXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List<Element> nl = descr.getDocument().getRootElement()
            .getChildren(WeblogicXmlTag.REFERENCE_DESCRIPTOR);
        Element n = nl.get(0);
        assertEquals("reference-descriptor", n.getName());
        List<Element> elements = getAllElements(n);
        n = elements.get(0);
        assertEquals("resource-description", n.getName());
        n = elements.get(1);
        assertEquals("resource-env-description", n.getName());
        n = elements.get(2);
        assertEquals("ejb-reference-description", n.getName());
    }

    /**
     * Get all sub-elements of an element.
     * @param n Element to analyze.
     * @return Children of <code>n</code>.
     */
    private List<Element> getAllElements(Element n)
    {
        List<Element> elements = new ArrayList<Element>();

        for (Object o : n.getChildren())
        {
            if (o instanceof Element)
            {
                elements.add((Element) o);
            }
        }

        return elements;
    }
}
