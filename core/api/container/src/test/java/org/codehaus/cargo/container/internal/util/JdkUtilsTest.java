package org.codehaus.cargo.container.internal.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JdkUtils}.
 */
public class JdkUtilsTest
{

    /**
     * Test parsing major java versions
     */
    @Test
    public void testParseMajorJavaVersion()
    {
        Assertions.assertEquals(7, JdkUtils.parseMajorJavaVersion("1.7.0_3"));
        Assertions.assertEquals(8, JdkUtils.parseMajorJavaVersion("1.8.0_121"));
        Assertions.assertEquals(9, JdkUtils.parseMajorJavaVersion("\"9-ea\""));
        Assertions.assertEquals(10, JdkUtils.parseMajorJavaVersion("\"10\" 2018-03-20"));
        Assertions.assertTrue(JdkUtils.getMajorJavaVersion() > 0);
    }
}
