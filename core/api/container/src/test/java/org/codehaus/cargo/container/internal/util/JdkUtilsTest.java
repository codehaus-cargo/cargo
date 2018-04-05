package org.codehaus.cargo.container.internal.util;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JdkUtils}.
 */
public class JdkUtilsTest extends TestCase
{

    /**
     * Test parsing major java versions
     */
    public void testParseMajorJavaVersion()
    {
        assertEquals(7, JdkUtils.parseMajorJavaVersion("1.7.0_3"));
        assertEquals(8, JdkUtils.parseMajorJavaVersion("1.8.0_121"));
        assertEquals(9, JdkUtils.parseMajorJavaVersion("\"9-ea\""));
        assertEquals(10, JdkUtils.parseMajorJavaVersion("\"10\" 2018-03-20"));
        assertTrue(JdkUtils.getMajorJavaVersion() > 0);
    }
}
