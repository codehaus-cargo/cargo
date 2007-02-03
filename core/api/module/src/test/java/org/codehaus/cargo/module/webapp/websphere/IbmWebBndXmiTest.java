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
package org.codehaus.cargo.module.webapp.websphere;

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for {@link IbmWebBndXmi}.
 *
 * @version $Id$
 */
public class IbmWebBndXmiTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests that a ejb reference description can be added
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescription() throws Exception
    {
        String xml = "<com.ibm.ejs.models.base.bindings.webappbnd:WebAppBinding "
            + "xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" "
            + "xmlns:com.ibm.ejs.models.base.bindings.webappbnd=\"webappbnd.xmi\" "
            + "xmi:id=\"WebAppBinding_1082390762531\">"
            + "</com.ibm.ejs.models.base.bindings.webappbnd:WebAppBinding>";
        Document doc = this.builder.parse(new ByteArrayInputStream(xml.getBytes()));
        IbmWebBndXmi descr = new IbmWebBndXmi(doc);
        descr.addEjbReference("foo", "fee");
        
        NodeList nl = descr.getDocument().getElementsByTagName("ejbRefBindings");
        Element n = (Element)nl.item(0);
        assertEquals("fee", n.getAttribute("jndiName"));
        assertEquals(1, nl.getLength());
        nl = n.getElementsByTagName("bindingEjbRef");
        n = (Element)nl.item(0);
        assertEquals("WEB-INF/web.xml#foo", n.getAttribute("href"));
        assertEquals(1, nl.getLength());
    }
}
