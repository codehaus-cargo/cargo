/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.application;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;

/**
 * Unit tests for {@link ApplicationXml}.
 * 
 * @version $Id$
 */
public final class ApplicationXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether the construction of a ApplicationXml object with a <code>null</code> parameter
     * for the DOM document throws a <code>NullPointerException</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testConstructionWithNullDocument() throws Exception
    {
        try
        {
            new ApplicationXml(null, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // expected
        }

    }

    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns an empty list for a
     * descriptor with no web module definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithEmptyDocument() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";

        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);
        List<String> webUris = applicationXml.getWebModuleUris();
        assertTrue("No web modules defined", webUris.isEmpty());
    }

    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns a list with the correct
     * web-uri for a descriptor with a single web module definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithSingleWebModule() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule.jar</web-uri>"
            + "      <context-root>/webmodule</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        List<String> webUris = applicationXml.getWebModuleUris();
        assertEquals(1, webUris.size());
        assertEquals("webmodule.jar", webUris.get(0));
    }

    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns a list with the correct
     * web-uris for a descriptor with multiple web module definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleUrisWithMultipleWebModules() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule1.jar</web-uri>"
            + "      <context-root>/webmodule1</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule2.jar</web-uri>"
            + "      <context-root>/webmodule2</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule3.jar</web-uri>"
            + "      <context-root>/webmodule3</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        List<String> webUris = applicationXml.getWebModuleUris();
        assertEquals(3, webUris.size());
        assertEquals("webmodule1.jar", webUris.get(0));
        assertEquals("webmodule2.jar", webUris.get(1));
        assertEquals("webmodule3.jar", webUris.get(2));
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> throws an
     * <code>IllegalARgumentException</code> when the specified web module is not defined.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootUndefined() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        try
        {
            applicationXml.getWebModuleContextRoot("webmodule.jar");
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> returns an the correct
     * context root for a descriptor with a single web module.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootSingleWebModule() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule.jar</web-uri>"
            + "      <context-root>/webmodule</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        assertEquals("/webmodule",
            applicationXml.getWebModuleContextRoot("webmodule.jar"));
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> returns an the correct
     * context roots for a descriptor with multiple web modules.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetWebModuleContextRootMultipleWebModules() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule1.jar</web-uri>"
            + "      <context-root>/webmodule1</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule2.jar</web-uri>"
            + "      <context-root>/webmodule2</context-root>"
            + "    </web>"
            + "  </module>"
            + "  <module>"
            + "    <web>"
            + "      <web-uri>webmodule3.jar</web-uri>"
            + "      <context-root>/webmodule3</context-root>"
            + "    </web>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        assertEquals("/webmodule1",
            applicationXml.getWebModuleContextRoot("webmodule1.jar"));
        assertEquals("/webmodule2",
            applicationXml.getWebModuleContextRoot("webmodule2.jar"));
        assertEquals("/webmodule3",
            applicationXml.getWebModuleContextRoot("webmodule3.jar"));
    }

    /**
     * Verifies that the method <code>addEjbModule()</code> adds a correct tag.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testAddEjbModule() throws Exception
    {
        String xml = "<application></application>";

        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        applicationXml.addEjbModule("ejbmodule1.jar");

        // Extract all ejbmodules, should be just one
        List<String> ejbModules = applicationXml.getEjbModules();
        assertEquals(1, ejbModules.size());
        assertEquals("ejbmodule1.jar", ejbModules.get(0));
    }

    /**
     * Verifies that the method <code>getEjbModules()</code> returns the correct ejb names.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testGetEjbModules() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <ejb>myFirstEjb.jar</ejb>"
            + "  </module>"
            + "  <module>"
            + "    <ejb>mySecondEjb.jar</ejb>"
            + "  </module>"
            + "</application>";

        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes("UTF-8")), null);

        List<String> ejbModules = applicationXml.getEjbModules();
        assertEquals(2, ejbModules.size());
        assertEquals("myFirstEjb.jar", ejbModules.get(0));
        assertEquals("mySecondEjb.jar", ejbModules.get(1));
    }
}
