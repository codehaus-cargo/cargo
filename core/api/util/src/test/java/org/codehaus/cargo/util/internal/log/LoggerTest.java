/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.util.internal.log;

import junit.framework.TestCase;

import org.codehaus.cargo.util.log.LogLevel;

/**
 * Unit tests for {@link AbstractLogger}.
 * 
 */
public class LoggerTest extends TestCase
{
    /**
     * Testable implementation for {@link AbstractLogger}.
     */
    public class TestableAbstractLogger extends AbstractLogger
    {
        /**
         * Last logged message.
         */
        private String message;

        /**
         * Save the log message. {@inheritDoc}
         * @param level Level.
         * @param message Message.
         * @param category Category.
         */
        @Override
        protected void doLog(LogLevel level, String message, String category)
        {
            this.message = "[" + level.getLevel() + "][" + category + "][" + message + "]";
        }

        /**
         * @return The last logged message.
         */
        public String popMessage()
        {
            String result = this.message;
            this.message = null;
            return result;
        }
    }

    /**
     * Test that the default level is {@link LogLevel#INFO}.
     */
    public void testDefaultLevelIsInfo()
    {
        TestableAbstractLogger logger = new TestableAbstractLogger();
        assertEquals(LogLevel.INFO, logger.getLevel());
    }

    /**
     * Test the matching between logging levels.
     */
    public void testLoggingAccordingToLogLevels()
    {
        TestableAbstractLogger logger = new TestableAbstractLogger();

        logger.setLevel(LogLevel.WARN);
        logger.debug("test1", "category");
        assertNull(logger.popMessage());
        logger.info("test2", "category");
        assertNull(logger.popMessage());
        logger.warn("test3", "category");
        assertEquals("[warn][category][test3]", logger.popMessage());

        logger.setLevel(LogLevel.INFO);
        logger.debug("test4", "category");
        assertNull(logger.popMessage());
        logger.info("test5", "category");
        assertEquals("[info][category][test5]", logger.popMessage());
        logger.warn("test6", "category");
        assertEquals("[warn][category][test6]", logger.popMessage());

        logger.setLevel(LogLevel.DEBUG);
        logger.debug("test7", "category");
        assertEquals("[debug][category][test7]", logger.popMessage());
        logger.info("test8", "category");
        assertEquals("[info][category][test8]", logger.popMessage());
        logger.warn("test9", "category");
        assertEquals("[warn][category][test9]", logger.popMessage());
    }
}
