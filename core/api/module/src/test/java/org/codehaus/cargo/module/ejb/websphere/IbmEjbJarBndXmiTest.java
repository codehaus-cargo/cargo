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
package org.codehaus.cargo.module.ejb.websphere;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.ejb.EjbDef;
import org.codehaus.cargo.module.ejb.weblogic.WeblogicEjbJarXml;
import org.codehaus.cargo.module.ejb.weblogic.WeblogicEjbJarXmlIo;


/**
 * Unit tests for {@link IbmEjbJarXmi}.
 *
 * @version $Id$
 */
public class IbmEjbJarBndXmiTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests the basic functionality of {@link OrionEjbJarXml.getJndiName}.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetJndiName() throws Exception
    {
        String xml = "<ejbbnd:EJBJarBinding xmlns:ejbbnd=\"ejbbnd.xmi\" xmlns:xmi=\"http://www.omg.org/XMI\">"
            + "  <ejbJar href=\"META-INF/ejb-jar.xml#ejb-jar_ID\"/>"
            + "  <ejbBindings xmi:id=\"bindingId\" jndiName=\"mycomp/MyEjb\">"
            + "    <enterpriseBean xmi:type=\"com.ibm.etools.ejb:Session\" "
            + "                    href=\"META-INF/ejb-jar.xml#ejbId\"/>"
            + "  </ejbBindings>"
            + "</ejbbnd:EJBJarBinding>";
            
        IbmEjbJarBndXmi descr = IbmEjbJarBndXmiIo.parseIbmEjbJarXmi(new ByteArrayInputStream(xml.getBytes()));
        assertEquals("mycomp/MyEjb", descr.getJndiName(new EjbDef("MyEjb", "ejbId")));
    }

}
