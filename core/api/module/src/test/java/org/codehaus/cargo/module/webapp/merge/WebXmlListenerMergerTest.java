/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.webapp.merge;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jdom2.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.elements.Listener;

/**
 * Unit tests for {@link WebXmlMerger}.
 */
public final class WebXmlListenerMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Test for CARGO-409 that merging listeners does not mess with the order.
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testMergeListeners() throws Exception
    {
        String file1 = "<web-app>\r\n"
            + "   <context-param>\r\n"
            + "       <param-name>file1-c1</param-name>\r\n"
            + "       <param-value>file1-v1</param-value>\r\n"
            + "   </context-param>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file1-lc1</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file1-lc2</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <servlet>\r\n"
            + "       <servlet-name>file1-S1</servlet-name>\r\n"
            + "       <servlet-class>file1-C2</servlet-class>\r\n"
            + "       <load-on-startup>file1-los</load-on-startup>\r\n"
            + "   </servlet>\r\n"
            + "</web-app>\r\n";

        String file2 = "<web-app>\r\n"
            + "   <context-param>\r\n"
            + "       <param-name>file2-c1</param-name>\r\n"
            + "       <param-value>file2-v1</param-value>\r\n"
            + "   </context-param>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file2-lc1</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file2-lc2</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <servlet>\r\n"
            + "       <servlet-name>file2-S1</servlet-name>\r\n"
            + "       <servlet-class>file2-C2</servlet-class>\r\n"
            + "       <load-on-startup>file2-los</load-on-startup>\r\n"
            + "   </servlet>\r\n"
            + "</web-app>\r\n"
            + "";

        WebXml file1WebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(file1.getBytes(StandardCharsets.UTF_8)), null);
        WebXml file2WebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(file2.getBytes(StandardCharsets.UTF_8)), null);

        WebXmlMerger merger = new WebXmlMerger(file1WebXml);
        merger.merge(file2WebXml);

        List<Element> ejbRefs = file1WebXml.getElements("listener");
        Assertions.assertEquals(4, ejbRefs.size());
        Assertions.assertEquals(((Listener) ejbRefs.get(0)).getListenerClass(), "file1-lc1");
        Assertions.assertEquals(((Listener) ejbRefs.get(1)).getListenerClass(), "file1-lc2");
        Assertions.assertEquals(((Listener) ejbRefs.get(2)).getListenerClass(), "file2-lc1");
        Assertions.assertEquals(((Listener) ejbRefs.get(3)).getListenerClass(), "file2-lc2");
    }

    /**
     * Test for CARGO-1209 that merging listener elements when the web-app element contains the
     * xmlns attribute
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testMergeListenersWithNamespaces() throws Exception
    {
        String file1 = "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\">\r\n"
            + "   <context-param>\r\n"
            + "       <param-name>file1-c1</param-name>\r\n"
            + "       <param-value>file1-v1</param-value>\r\n"
            + "   </context-param>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file1-lc1</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file1-lc2</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <servlet>\r\n"
            + "       <servlet-name>file1-S1</servlet-name>\r\n"
            + "       <servlet-class>file1-C2</servlet-class>\r\n"
            + "       <load-on-startup>file1-los</load-on-startup>\r\n"
            + "   </servlet>\r\n"
            + "</web-app>\r\n";

        String file2 = "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\">\r\n"
            + "   <context-param>\r\n"
            + "       <param-name>file2-c1</param-name>\r\n"
            + "       <param-value>file2-v1</param-value>\r\n"
            + "   </context-param>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file2-lc1</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "    <listener>\r\n"
            + "      <listener-class>file2-lc2</listener-class>\r\n"
            + "    </listener>\r\n"
            + "\r\n"
            + "\r\n"
            + "    <servlet>\r\n"
            + "       <servlet-name>file2-S1</servlet-name>\r\n"
            + "       <servlet-class>file2-C2</servlet-class>\r\n"
            + "       <load-on-startup>file2-los</load-on-startup>\r\n"
            + "   </servlet>\r\n"
            + "</web-app>\r\n"
            + "";

        WebXml file1WebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(file1.getBytes(StandardCharsets.UTF_8)), null);
        WebXml file2WebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(file2.getBytes(StandardCharsets.UTF_8)), null);

        WebXmlMerger merger = new WebXmlMerger(file1WebXml);
        merger.merge(file2WebXml);

        List<Element> ejbRefs = file1WebXml.getElements("listener");
        Assertions.assertEquals(4, ejbRefs.size());
        Assertions.assertEquals(((Listener) ejbRefs.get(0)).getListenerClass(), "file1-lc1");
        Assertions.assertEquals(((Listener) ejbRefs.get(1)).getListenerClass(), "file1-lc2");
        Assertions.assertEquals(((Listener) ejbRefs.get(2)).getListenerClass(), "file2-lc1");
        Assertions.assertEquals(((Listener) ejbRefs.get(3)).getListenerClass(), "file2-lc2");
    }
}
