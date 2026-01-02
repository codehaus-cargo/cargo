/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;

/**
 * Unit tests for {@link ApplicationXml}.
 */
public final class ApplicationXmlTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether the construction of a ApplicationXml object with a <code>null</code> parameter
     * for the DOM document throws a <code>NullPointerException</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testConstructionWithNullDocument() throws Exception
    {
        try
        {
            new ApplicationXml(null, null);
            Assertions.fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
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
    @Test
    public void testGetWebModuleUrisWithEmptyDocument() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";

        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        List<String> webUris = applicationXml.getWebModuleUris();
        Assertions.assertTrue(webUris.isEmpty(), "No web modules defined");
    }

    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns a list with the correct
     * web-uri for a descriptor with a single web module definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
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
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        List<String> webUris = applicationXml.getWebModuleUris();
        Assertions.assertEquals(1, webUris.size());
        Assertions.assertEquals("webmodule.jar", webUris.get(0));
    }

    /**
     * Verifies that the method <code>getWebModuleUris()</code> returns a list with the correct
     * web-uris for a descriptor with multiple web module definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
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
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        List<String> webUris = applicationXml.getWebModuleUris();
        Assertions.assertEquals(3, webUris.size());
        Assertions.assertEquals("webmodule1.jar", webUris.get(0));
        Assertions.assertEquals("webmodule2.jar", webUris.get(1));
        Assertions.assertEquals("webmodule3.jar", webUris.get(2));
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> throws an
     * <code>IllegalARgumentException</code> when the specified web module is not defined.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testGetWebModuleContextRootUndefined() throws Exception
    {
        String xml = "<application>"
            + "  <module>"
            + "    <java>javaclient.jar</java>"
            + "  </module>"
            + "</application>";
        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        try
        {
            applicationXml.getWebModuleContextRoot("webmodule.jar");
            Assertions.fail("IllegalArgumentException expected");
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
    @Test
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
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        Assertions.assertEquals("/webmodule",
            applicationXml.getWebModuleContextRoot("webmodule.jar"));
    }

    /**
     * Verifies that the method <code>getWebModuleContextRoot()</code> returns an the correct
     * context roots for a descriptor with multiple web modules.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
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
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        Assertions.assertEquals("/webmodule1",
            applicationXml.getWebModuleContextRoot("webmodule1.jar"));
        Assertions.assertEquals("/webmodule2",
            applicationXml.getWebModuleContextRoot("webmodule2.jar"));
        Assertions.assertEquals("/webmodule3",
            applicationXml.getWebModuleContextRoot("webmodule3.jar"));
    }

    /**
     * Verifies that the method <code>addEjbModule()</code> adds a correct tag.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testAddEjbModule() throws Exception
    {
        String xml = "<application></application>";

        ApplicationXml applicationXml = ApplicationXmlIo.parseApplicationXml(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        applicationXml.addEjbModule("ejbmodule1.jar");

        // Extract all ejbmodules, should be just one
        List<String> ejbModules = applicationXml.getEjbModules();
        Assertions.assertEquals(1, ejbModules.size());
        Assertions.assertEquals("ejbmodule1.jar", ejbModules.get(0));
    }

    /**
     * Verifies that the method <code>getEjbModules()</code> returns the correct ejb names.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
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
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);

        List<String> ejbModules = applicationXml.getEjbModules();
        Assertions.assertEquals(2, ejbModules.size());
        Assertions.assertEquals("myFirstEjb.jar", ejbModules.get(0));
        Assertions.assertEquals("mySecondEjb.jar", ejbModules.get(1));
    }
}
