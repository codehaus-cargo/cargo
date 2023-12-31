/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.ejb.EjbDef;

/**
 * Unit tests for {@link OrionEjbJarXml}.
 */
public class OracleEjbJarXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link OrionEjbJarXml#getJndiName}.
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

        OrionEjbJarXml descr = OrionEjbJarXmlIo.parseOracleEjbJarXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }

    /**
     * Tests {@link OrionEjbJarXml#getJndiName}.
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

        OrionEjbJarXml descr = OrionEjbJarXmlIo.parseOracleEjbJarXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertNull(descr.getJndiName(new EjbDef("foo")));
    }

    /**
     * Tests {@link OrionEjbJarXml#getJndiName}.
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

        OrionEjbJarXml descr = OrionEjbJarXmlIo.parseOracleEjbJarXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb")));
    }

    /**
     * Tests {@link OrionEjbJarXml#getJndiName}.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiNameOfLocalEjb() throws Exception
    {
        String xml = "<orion-ejb-jar>"
            + "  <enterprise-beans>"
            + "    <entity-deployment name=\"MyEjb\" location=\"mycomp/MyEjb\" "
            + "                       local-location=\"localJndiName\"/>"
            + "  </enterprise-beans>"
            + "</orion-ejb-jar>";

        OrionEjbJarXml descr = OrionEjbJarXmlIo.parseOracleEjbJarXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        EjbDef def = new EjbDef("MyEjb");
        def.setLocal("sdf");
        def.setLocalHome("laskfj");
        assertEquals("localJndiName", descr.getJndiName(def));
    }
}
