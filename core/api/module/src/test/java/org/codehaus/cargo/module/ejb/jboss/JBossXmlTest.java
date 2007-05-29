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
package org.codehaus.cargo.module.ejb.jboss;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.webapp.jboss.JBossWebXmlIo;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link JBossXmlTest}.
 *
 * @version $Id: $
 */
public class JBossXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link JBossXmlTest.getJndiName}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiName() throws Exception
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<jboss>"
            + "<enterprise-beans>"
            + "<session>"
            + "<ejb-name>BeanOne</ejb-name>"
            + "<jndi-name>test/Tester</jndi-name>"
            + "</session>"
            + "</enterprise-beans>"
            + "</jboss>";

        JBossXml descr = JBossXmlIo.parseJBossXml(new ByteArrayInputStream(xml.getBytes()));
        assertEquals("test/Tester", descr.getJndiName(new EjbDef("BeanOne")));
    }

    /**
     * Tests {@link JBossXmlTest.getJndiName} with a faulty ejb name.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetLocalJndiNameWithWrongEjbName() throws Exception
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<jboss>"
            + "<enterprise-beans>"
            + "<session>"
            + "<ejb-name>BeanOne</ejb-name>"
            + "<jndi-name>test/Tester</jndi-name>"
            + "</session>"
            + "</enterprise-beans>"
            + "</jboss>";

        JBossXml descr = JBossXmlIo.parseJBossXml(new ByteArrayInputStream(xml.getBytes()));
        assertNull(descr.getJndiName(new EjbDef("BeanOn")));
    }
}
