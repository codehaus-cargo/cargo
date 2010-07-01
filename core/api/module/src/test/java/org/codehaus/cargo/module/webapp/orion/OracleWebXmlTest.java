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
package org.codehaus.cargo.module.webapp.orion;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.jdom.Element;
import org.codehaus.cargo.module.webapp.EjbRef;

/**
 * Unit tests for {@link OracleWebXml}.
 *
 * @version $Id$
 */
public class OracleWebXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests that a ejb reference description can be added.
     *
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbReferenceDescription() throws Exception
    {
        String xml = "<orion-web-app></orion-web-app>";
        
        OrionWebXml descr = OrionWebXmlIo.parseOrionXml(new ByteArrayInputStream(xml.getBytes()));        
        EjbRef ref = new EjbRef();
        ref.setName("foo");
        ref.setJndiName("fee");
        descr.addEjbReference(ref);

        List nl = descr.getDocument().getRootElement().getChildren("ejb-ref-mapping");
        Element n = (Element)nl.get(0);
        assertEquals("foo", n.getAttribute("name").getValue());
        assertEquals("fee", n.getAttribute("location").getValue());
        assertEquals(1, nl.size());
    }

}
