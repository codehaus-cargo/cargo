/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import junit.framework.TestCase;

/**
 * Generates project structure documentation using ConfluenceProjectStructureDocumentationGenerator.
 */
public class ConfluenceProjectStructureDocumentationGeneratorTest extends TestCase
{
    /**
     * The doc generator under test.
     */
    private ConfluenceProjectStructureDocumentationGenerator generator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        this.generator = new ConfluenceProjectStructureDocumentationGenerator();
    }

    /**
     * Tests for the creation of the project structure markup.
     * @throws Exception if something goes wrong with the markup generation.
     */
    public void testDocGeneration() throws Exception
    {
        File projectStructureMarkup = new File(System.getProperty("basedir")
            + "/target/project-structure.log");
        try (Writer writer = new FileWriter(projectStructureMarkup))
        {
            writer.write(this.generator.generateDocumentation());
        }

        assertTrue(projectStructureMarkup.exists());
    }

}
