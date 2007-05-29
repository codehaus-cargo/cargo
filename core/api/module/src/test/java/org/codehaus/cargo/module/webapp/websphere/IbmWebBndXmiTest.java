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
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.jdom.Element;
import org.codehaus.cargo.module.webapp.EjbRef;

/**
 * Unit tests for {@link IbmWebBndXmi}.
 *
 * @version $Id$
 */
public class IbmWebBndXmiTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests that a ejb reference description can be added.
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
        IbmWebBndXmi descr = IbmWebBndXmiIo.parseIbmWebBndXmi(
            new ByteArrayInputStream(xml.getBytes()) );
        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List nl = descr.getDocument().getRootElement().getChildren("ejbRefBindings");
        Element n = (Element) nl.get(0);
        assertEquals("fee", n.getAttribute("jndiName").getValue());
        assertEquals(1, nl.size());
        nl = n.getChildren("bindingEjbRef");
        n = (Element) nl.get(0);
        assertEquals("WEB-INF/web.xml#foo", n.getAttribute("href").getValue());
        assertEquals(1, nl.size());
    }
}
