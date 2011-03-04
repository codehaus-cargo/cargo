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
package org.codehaus.cargo.module.ejb;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;

/**
 * Unit tests for {@link EjbJarXml}.
 * 
 * @version $Id$
 */
public class EjbJarXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link EjbJarXml#getSessionEjbs()}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetSessionEjbNames() throws Exception
    {
        String xml = "<ejb-jar>"
            + "  <enterprise-beans>"
            + "    <message-driven>"
            + "      <ejb-name>MyMDB</ejb-name>"
            + "    </message-driven>"
            + "    <session>"
            + "      <ejb-name>MyFirstSession</ejb-name>"
            + "    </session>"
            + "    <session>"
            + "      <ejb-name>MySecondSession</ejb-name>"
            + "    </session>"
            + "    <entity>"
            + "      <ejb-name>MyEntity</ejb-name>"
            + "    </entity>"
            + "  </enterprise-beans>"
            + "  <assembly-descriptor>"
            + "  </assembly-descriptor>"
            + "</ejb-jar>";

        EjbJarXml ejbJar =
            EjbJarXmlIo.parseEjbJarXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        List<Session> ejbs = ejbJar.getSessionEjbs();
        assertEquals(2, ejbs.size());
        assertEquals("MyFirstSession", ejbs.get(0).getName());
        assertEquals("MySecondSession", ejbs.get(1).getName());
    }

    /**
     * Tests the basic functionality of {@link EjbJarXml#getSessionEjbs()}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetEntityEjbNames() throws Exception
    {
        String xml = "<ejb-jar>"
            + "  <enterprise-beans>"
            + "    <message-driven>"
            + "      <ejb-name>MyMDB</ejb-name>"
            + "    </message-driven>"
            + "    <session>"
            + "      <ejb-name>MyFirstSession</ejb-name>"
            + "    </session>"
            + "    <session>"
            + "      <ejb-name>MySecondSession</ejb-name>"
            + "    </session>"
            + "    <entity>"
            + "      <ejb-name>MyEntity</ejb-name>"
            + "    </entity>"
            + "  </enterprise-beans>"
            + "  <assembly-descriptor>"
            + "  </assembly-descriptor>"
            + "</ejb-jar>";

        EjbJarXml ejbJar =
            EjbJarXmlIo.parseEjbJarXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        List<Entity> ejbs = ejbJar.getEntityEjbs();
        assertEquals(1, ejbs.size());
        assertEquals("MyEntity", ejbs.get(0).getName());
    }

    /**
     * Tests the basic functionality of {@link EjbJarXml#getSessionEjb(String)}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetSessionLocalInterfaces() throws Exception
    {
        String xml = "<ejb-jar>"
            + "  <enterprise-beans>"
            + "    <session>"
            + "      <ejb-name>MyFirstSession</ejb-name>"
            + "      <local>com.wombat.MyFirstSession</local>"
            + "      <local-home>com.wombat.MyFirstSessionHome</local-home>"
            + "    </session>"
            + "  </enterprise-beans>"
            + "</ejb-jar>";

        EjbJarXml ejbJar =
            EjbJarXmlIo.parseEjbJarXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null);
        Session ejb = ejbJar.getSessionEjb("MyFirstSession");
        assertEquals("com.wombat.MyFirstSession", ejb.getLocal());
        assertEquals("com.wombat.MyFirstSessionHome", ejb.getLocalHome());
    }

    /**
     * Tests the basic functionality of {@link EjbJarXml#getEntityEjb(String)}
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetEntityLocalInterfaces() throws Exception
    {
        String xml = "<ejb-jar>"
            + "  <enterprise-beans>"
            + "    <entity>"
            + "      <ejb-name>MyEntity</ejb-name>"
            + "      <local>com.wombat.MyEntity</local>"
            + "      <local-home>com.wombat.MyEntityHome</local-home>"
            + "    </entity>"
            + "  </enterprise-beans>"
            + "</ejb-jar>";

        EjbJarXml ejbJar =
            EjbJarXmlIo.parseEjbJarXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null);
        Entity ejb = ejbJar.getEntityEjb("MyEntity");
        assertEquals("com.wombat.MyEntity", ejb.getLocal());
        assertEquals("com.wombat.MyEntityHome", ejb.getLocalHome());
    }

}
