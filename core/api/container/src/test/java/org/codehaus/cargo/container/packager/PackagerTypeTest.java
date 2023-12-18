package org.codehaus.cargo.container.packager;

import junit.framework.TestCase;

public class PackagerTypeTest extends TestCase {
    public void testEquals() {

        PackagerType packagerType1 = new PackagerType("TypeA");

        assertTrue(packagerType1.equals(packagerType1));

        PackagerType packagerType2 = new PackagerType("TypeA");

        assertTrue(packagerType1.equals(packagerType2));
        assertTrue(packagerType2.equals(packagerType1));


        PackagerType packagerType3 = new PackagerType("TypeB");


        assertFalse(packagerType1.equals(packagerType3));
        assertFalse(packagerType3.equals(packagerType1));


        assertFalse(packagerType1.equals(null));
        assertFalse(packagerType1.equals("TypeA"));
        assertFalse(packagerType1.equals(new Object()));
    }

}
