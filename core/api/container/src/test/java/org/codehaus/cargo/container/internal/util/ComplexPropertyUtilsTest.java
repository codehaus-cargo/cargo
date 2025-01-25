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
package org.codehaus.cargo.container.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for JvmArguments class.
 */
public final class ComplexPropertyUtilsTest
{
    /**
     * Test parsing of provided property.
     */
    @Test
    public void testParseProperty()
    {
        String toBeParsed = "com.ibm.ssl.rootCertValidDays:1234|"
                + "   com.ibm.websphere.security.krb.canonical_host:false";

        List<List<String>> parsedProperty = ComplexPropertyUtils.parseProperty(toBeParsed);
        Assertions.assertEquals(2, parsedProperty.size());
        Assertions.assertEquals(2, parsedProperty.get(0).size());
        Assertions.assertEquals(2, parsedProperty.get(1).size());

        Assertions.assertEquals("com.ibm.ssl.rootCertValidDays", parsedProperty.get(0).get(0));
        Assertions.assertEquals("1234", parsedProperty.get(0).get(1));
        Assertions.assertEquals(
            "com.ibm.websphere.security.krb.canonical_host", parsedProperty.get(1).get(0));
        Assertions.assertEquals("false", parsedProperty.get(1).get(1));
    }

    /**
     * Test parsing of provided property.
     */
    @Test
    public void testParseCustomProperty()
    {
        String toBeParsed = "com.ibm.ssl.rootCertValidDays%1234#"
                + "   com.ibm.websphere.security.krb.canonical_host%false";

        List<List<String>> parsedProperty =
                ComplexPropertyUtils.parseProperty(toBeParsed, "#", "%");
        Assertions.assertEquals(2, parsedProperty.size());
        Assertions.assertEquals(2, parsedProperty.get(0).size());
        Assertions.assertEquals(2, parsedProperty.get(1).size());

        Assertions.assertEquals("com.ibm.ssl.rootCertValidDays", parsedProperty.get(0).get(0));
        Assertions.assertEquals("1234", parsedProperty.get(0).get(1));
        Assertions.assertEquals(
            "com.ibm.websphere.security.krb.canonical_host", parsedProperty.get(1).get(0));
        Assertions.assertEquals("false", parsedProperty.get(1).get(1));
    }

    /**
     * Test parsing of provided simple property.
     */
    @Test
    public void testParseCustomSimpleProperty()
    {
        String toBeParsed = "com.ibm.ssl.rootCertValidDays#"
                + "   com.ibm.websphere.security.krb.canonical_host";

        List<String> parsedProperty = ComplexPropertyUtils.parseProperty(toBeParsed, "#");
        Assertions.assertEquals(2, parsedProperty.size());

        Assertions.assertEquals("com.ibm.ssl.rootCertValidDays", parsedProperty.get(0));
        Assertions.assertEquals(
            "com.ibm.websphere.security.krb.canonical_host", parsedProperty.get(1));
    }

    /**
     * Test parsing of provided simple property.
     */
    @Test
    public void testJoinOnDelimiter()
    {
        List<String> toBeJoined = new ArrayList<String>();
        toBeJoined.add("First Item");
        toBeJoined.add("Second Item");
        char delimiter = ':';

        String joinedProperty = ComplexPropertyUtils.joinOnDelimiter(toBeJoined, delimiter);

        Assertions.assertEquals("First Item:Second Item", joinedProperty);
    }
}
