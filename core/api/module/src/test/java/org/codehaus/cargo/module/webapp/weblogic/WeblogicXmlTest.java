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
import org.jdom.Element;
import org.codehaus.cargo.module.webapp.EjbRef;

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
        
        WeblogicXml descr = WeblogicXmlIo.parseWeblogicXml(new ByteArrayInputStream(xml.getBytes()) );
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List nl = descr.getDocument().getRootElement().getChildren(WeblogicXmlTag.REFERENCE_DESCRIPTOR);
        Element n = (Element)nl.get(0);
        assertEquals("reference-descriptor", n.getName());
        n = (Element)n.getChildren(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION).get(0);
        assertEquals("ejb-reference-description", n.getName());
        Element m = (Element)n.getChildren(WeblogicXmlTag.EJB_REF_NAME).get(0);
        assertEquals("ejb-ref-name", m.getName());
        assertEquals("foo", m.getText());
        m = (Element)n.getChildren(WeblogicXmlTag.JNDI_NAME).get(0);
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
        WeblogicXml descr = WeblogicXmlIo.parseWeblogicXml(new ByteArrayInputStream(xml.getBytes()) );
        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);
                
        List elements = getAllElements((Element)descr.getDocument().getRootElement() );
        Element n = (Element)elements.get(0);
        assertEquals("run-as-role-assignment", n.getName());
        n = (Element)elements.get(2);
        assertEquals("session-descriptor", n.getName());
        n = (Element)elements.get(1);
        assertEquals("reference-descriptor", n.getName());
        n = (Element)n.getChildren(WeblogicXmlTag.EJB_REFERENCE_DESCRIPTION).get(0);
        assertEquals("ejb-reference-description", n.getName());
        Element m = (Element)n.getChildren(WeblogicXmlTag.EJB_REF_NAME).get(0);
        assertEquals("ejb-ref-name", m.getName());
        assertEquals("foo", m.getValue());
        m = (Element)n.getChildren(WeblogicXmlTag.JNDI_NAME).get(0);
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
        WeblogicXml descr = WeblogicXmlIo.parseWeblogicXml(new ByteArrayInputStream(xml.getBytes()) );
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List nl = descr.getDocument().getRootElement().getChildren(WeblogicXmlTag.REFERENCE_DESCRIPTOR);
        Element n = (Element)nl.get(0);
        assertEquals("reference-descriptor", n.getName());
        List elements = getAllElements(n);
        n = (Element)elements.get(0);
        assertEquals("resource-description", n.getName());
        n = (Element)elements.get(1);
        assertEquals("resource-env-description", n.getName());
        n = (Element)elements.get(2);
        assertEquals("ejb-reference-description", n.getName());
    }

    private List getAllElements(Element n)
    {
        List elements = new ArrayList();

        List nl = n.getChildren();
        for(int i=0; i<nl.size(); i++)
        {
            if(nl.get(i) instanceof Element)
            {
                elements.add(nl.get(i));
            }
        }

        return elements;
    }
}
