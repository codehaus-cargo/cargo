/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.module;

import junit.framework.TestCase;

/**
 * Unit tests for {@link XmlEntityResolver}.
 */
public class XmlEntityResolverTest extends TestCase
{
    /**
     * Verifies that the method <code>getDtdFileName()</code> works with known filename.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetKnownFileName() throws Exception
    {
        String pId = "-//ORACLE//DTD OC4J Web Application 9.04//EN";
        String sId = "http://xmlns.oracle.com/ias/dtds/orion-web-9_04.dtd";
        XmlEntityResolver resolver = new XmlEntityResolver();
        String file = resolver.getDtdFileName(pId, sId);
        assertEquals(file, "orion-web-9_04.dtd");
    }

    /**
     * Verifies that the method <code>getDtdFileName()</code> works with unknown filename.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetUnknownFileName() throws Exception
    {
        String pId = "-//BEA Systems, Inc.//DTD Web Application 6.1//EN";
        String sId = "http://www.bea.com/servers/wls610/dtd/weblogic610-web-jar.dtd";
        XmlEntityResolver resolver = new XmlEntityResolver();
        String file = resolver.getDtdFileName(pId, sId);
        assertEquals(file, "weblogic610-web-jar.dtd");
    }
}
