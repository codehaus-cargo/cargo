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
import org.jdom.Element;


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
        
        JBossWebXml descr = JBossWebXmlIo.parseJBossWebXml( new ByteArrayInputStream(xml.getBytes()) );
        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(false);
        descr.addEjbReference(ref);

        Element ejbRef = (Element)descr.getRootElement().getChildren().get(0);
        assertEquals("ejb-ref", ejbRef.getName());
        Element ejbRefName = (Element)ejbRef.getChildren().get(0);
        assertEquals("ejb-ref-name", ejbRefName.getName());
        assertEquals("foo", ejbRefName.getValue());
        Element jndiName = (Element)ejbRef.getChildren().get(1);
        assertEquals("jndi-name", jndiName.getName());
        assertEquals("fee", jndiName.getValue());
    }

    /**
     * Tests {@link JBossWebXml.addEjbReference}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddLocalEjbReference() throws Exception
    {
        String xml = "<jboss-web></jboss-web>";
        JBossWebXml descr = JBossWebXmlIo.parseJBossWebXml( new ByteArrayInputStream(xml.getBytes()) );
        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(true);
        descr.addEjbReference(ref);

        Element ejbRef = (Element)descr.getRootElement().getChildren().get(0);
        assertEquals("ejb-local-ref", ejbRef.getName());
        Element ejbRefName = (Element)ejbRef.getChildren().get(0);
        assertEquals("ejb-ref-name", ejbRefName.getName());
        assertEquals("foo", ejbRefName.getValue());
        Element jndiName = (Element)ejbRef.getChildren().get(1);
        assertEquals("local-jndi-name", jndiName.getName());
        assertEquals("fee", jndiName.getValue());
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
        
        JBossWebXml descr = JBossWebXmlIo.parseJBossWebXml( new ByteArrayInputStream(xml.getBytes()) );
        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        ref.setLocal(false);
        descr.addEjbReference(ref);

        Element secDomain = (Element)descr.getRootElement().getChildren().get(0);
        assertEquals("security-domain", secDomain.getName());
        Element resRef = (Element)descr.getRootElement().getChildren().get(1);
        assertEquals("resource-ref", resRef.getName());
        Element ejbRef = (Element)descr.getRootElement().getChildren().get(2);
        assertEquals("ejb-ref", ejbRef.getName());
    }
}
