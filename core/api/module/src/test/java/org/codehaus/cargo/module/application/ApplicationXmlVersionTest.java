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
package org.codehaus.cargo.module.application;

import org.jdom2.DocType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;

/**
 * Unit tests for {@link ApplicationXmlVersion}.
 */
public final class ApplicationXmlVersionTest extends AbstractDocumentBuilderTest
{
    /**
     * Verifies that comparing version 1.2 to version 1.2 yields zero.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testCompare12To12() throws Exception
    {
        Assertions.assertTrue(ApplicationXmlVersion.V1_2.compareTo(
            ApplicationXmlVersion.V1_2) == 0);
    }

    /**
     * Verifies that comparing version 1.2 to version 1.3 yields a negative value.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testCompare12To13() throws Exception
    {
        Assertions.assertTrue(ApplicationXmlVersion.V1_2.compareTo(
            ApplicationXmlVersion.V1_3) < 0);
    }

    /**
     * Verifies that comparing version 1.3 to version 1.3 yields zero.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testCompare13To13() throws Exception
    {
        Assertions.assertTrue(ApplicationXmlVersion.V1_3.compareTo(
            ApplicationXmlVersion.V1_3) == 0);
    }

    /**
     * Verifies that comparing version 1.2 to version 1.3 yields a negative value.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testCompare13To12() throws Exception
    {
        Assertions.assertTrue(ApplicationXmlVersion.V1_3.compareTo(
            ApplicationXmlVersion.V1_2) > 0);
    }

    /**
     * Verifies that calling ApplicationXmlVersion.valueOf(null) throws a NullPointerException.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testValueOfNull() throws Exception
    {
        try
        {
            ApplicationXmlVersion.valueOf((DocType) null);
            Assertions.fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }

    /**
     * Verifies that calling ApplicationXmlVersion.valueOf() with a unknown document type returns
     * null.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testValueOfUnknownDocType() throws Exception
    {
        DocType docType = new DocType("application",
            "foo", "bar");

        Assertions.assertNull(ApplicationXmlVersion.valueOf(docType));
    }

    /**
     * Verifies that calling ApplicationXmlVersion.valueOf() with a application 1.2 document type
     * returns the correct instance.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testValueOfDocType12() throws Exception
    {
        DocType docType = new DocType("application",
            ApplicationXmlVersion.V1_2.getPublicId(),
            ApplicationXmlVersion.V1_2.getSystemId());
        Assertions.assertEquals(ApplicationXmlVersion.V1_2, ApplicationXmlVersion.valueOf(docType));
    }

    /**
     * Verifies that calling ApplicationXmlVersion.valueOf() with a application 1.3 document type
     * returns the correct instance.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testValueOfDocType13() throws Exception
    {
        DocType docType = new DocType("application",
            ApplicationXmlVersion.V1_3.getPublicId(),
            ApplicationXmlVersion.V1_3.getSystemId());
        Assertions.assertEquals(ApplicationXmlVersion.V1_3, ApplicationXmlVersion.valueOf(docType));
    }

}
