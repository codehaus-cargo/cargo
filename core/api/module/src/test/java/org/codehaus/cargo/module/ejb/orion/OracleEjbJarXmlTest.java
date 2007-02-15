/*
 * ========================================================================
 *
 * Copyright 2005-2007 Vincent Massol.
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
package org.codehaus.cargo.module.ejb.orion;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link OrionEjbJarXml}.
 *
 * @version $Id$
 */
public class OracleEjbJarXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link OrionEjbJarXml.getJndiName}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiName() throws Exception
    {
        String xml = "<orion-ejb-jar>"
            + "  <enterprise-beans>"
            + "    <session-deployment name=\"MyEjb\" location=\"mycomp/MyEjb\">"
            + "    </session-deployment>"
            + "  </enterprise-beans>"
            + "</orion-ejb-jar>";

        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        OrionEjbJarXml descr = new OrionEjbJarXml(doc);
        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }

    /**
     * Tests to get the jndi name of an local ejb.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiNameForLocalEjb() throws Exception
    {
        String xml = "<orion-ejb-jar>"
            + "  <enterprise-beans>"
            + "    <session-deployment name=\"MyEjb\" location=\"mycomp/MyEjb\""
            + " local-location=\"MyLocalEjb\">"
            + "    </session-deployment>"
            + "  </enterprise-beans>"
            + "</orion-ejb-jar>";

        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        OrionEjbJarXml descr = new OrionEjbJarXml(doc);
        EjbDef def = new EjbDef("MyEjb");
        def.setLocal("com.mycomp.MyEjb");
        assertEquals("MyLocalEjb", descr.getJndiName(def));
    }

    /**
     * Tests {@link OrionEjbJarXml.getJndiName}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiNameWithWrongName() throws Exception
    {
        String xml = "<orion-ejb-jar>"
            + "  <enterprise-beans>"
            + "    <session-deployment name=\"MyEjb\" location=\"mycomp/MyEjb\">"
            + "    </session-deployment>"
            + "  </enterprise-beans>"
            + "</orion-ejb-jar>";

        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        OrionEjbJarXml descr = new OrionEjbJarXml(doc);
        assertNull(descr.getJndiName(new EjbDef("foo")));
    }

    /**
     * Tests {@link OrionEjbJarXml.getJndiName}.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiNameOfEntityEjb() throws Exception
    {
        String xml = "<orion-ejb-jar>"
            + "  <enterprise-beans>"
            + "    <entity-deployment name=\"MyEjb\" location=\"mycomp/MyEjb\"/>"
            + "  </enterprise-beans>"
            + "</orion-ejb-jar>";

        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        OrionEjbJarXml descr = new OrionEjbJarXml(doc);
        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }
}
