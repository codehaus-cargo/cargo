/*
 * ========================================================================
 *
 * Copyright 2005 - 2007 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.jboss;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.EjbRef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link JBossWebXm}.
 *
 * @version $Id:  $
 */
public class JBossWebXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests {@link JBossWebXml.addEjbReference}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReference() throws Exception
    {
        String xml = "<jboss-web></jboss-web>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        JBossWebXml descr = new JBossWebXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(false);
        descr.addEjbReference(ref);

        Element ejbRef = (Element)doc.getDocumentElement().getFirstChild();
        assertEquals("ejb-ref", ejbRef.getNodeName());
        Element ejbRefName = (Element)ejbRef.getFirstChild();
        assertEquals("ejb-ref-name", ejbRefName.getNodeName());
        assertEquals("foo", ejbRefName.getFirstChild().getNodeValue());
        Element jndiName = (Element)ejbRef.getChildNodes().item(1);
        assertEquals("jndi-name", jndiName.getNodeName());
        assertEquals("fee", jndiName.getFirstChild().getNodeValue());
    }

    /**
     * Tests {@link JBossWebXml.addEjbReference}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddLocalEjbReference() throws Exception
    {
        String xml = "<jboss-web></jboss-web>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        JBossWebXml descr = new JBossWebXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(true);
        descr.addEjbReference(ref);

        Element ejbRef = (Element)doc.getDocumentElement().getFirstChild();
        assertEquals("ejb-local-ref", ejbRef.getNodeName());
        Element ejbRefName = (Element)ejbRef.getFirstChild();
        assertEquals("ejb-ref-name", ejbRefName.getNodeName());
        assertEquals("foo", ejbRefName.getFirstChild().getNodeValue());
        Element jndiName = (Element)ejbRef.getChildNodes().item(1);
        assertEquals("local-jndi-name", jndiName.getNodeName());
        assertEquals("fee", jndiName.getFirstChild().getNodeValue());
    }

    /**
     * Tests that {@link JBossWebXml.addEjbReference} can add ejb reference in correct
     * order.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceInCorrectOrder() throws Exception
    {
        String xml = "<jboss-web>"
            + "<security-domain/>"
            + "<resource-ref/>"
            + "</jboss-web>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        JBossWebXml descr = new JBossWebXml(doc);
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(false);
        descr.addEjbReference(ref);

        Element secDomain = (Element)doc.getDocumentElement().getChildNodes().item(0);
        assertEquals("security-domain", secDomain.getNodeName());
        Element resRef = (Element)doc.getDocumentElement().getChildNodes().item(1);
        assertEquals("resource-ref", resRef.getNodeName());
        Element ejbRef = (Element)doc.getDocumentElement().getChildNodes().item(2);
        assertEquals("ejb-ref", ejbRef.getNodeName());
    }
}
