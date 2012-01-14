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
package org.codehaus.cargo.documentation;

import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;

/**
 * Unit tests for {@link ConfluenceContainerDocumentationGenerator}
 * 
 * @version $Id$
 */
public class ConfluenceContainerDocumentationGeneratorTest extends TestCase
{
    /**
     * Documentation generator.
     */
    private ConfluenceContainerDocumentationGenerator generator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        this.generator = new ConfluenceContainerDocumentationGenerator();
    }

    /**
     * Test computed qualified class name.
     */
    public void testComputedFQCN()
    {
        assertEquals("o.c.c.c.myc.MyContainerIsTheBest",
            this.generator.computedFQCN("org.codehaus.cargo.container.myc.MyContainerIsTheBest"));
    }

    /**
     * Generate datasource documentation.
     * @throws Exception If anything goes wrong.
     */
    public void testGenerateDatasourceDocumentation() throws Exception
    {
        Writer writer = new FileWriter(System.getProperty("basedir") + "/target/datasource.log");
        writer.write(this.generator.generateDatasourceDocumentation());
        writer.close();
    }

    /**
     * Generate documentation for all containers.
     * @throws Exception If anything goes wrong.
     */
    public void testGenerateDocumentationForAllContainers() throws Exception
    {
        ContainerFactory factory = new DefaultContainerFactory();

        Map<String, Set<ContainerType>> containerIds = factory.getContainerIds();
        for (String containerId : containerIds.keySet())
        {
            generateDocumentationForContainer(containerId);
        }
    }

    /**
     * Generate documentation for a given container.
     * @param containerId Container id.
     * @throws Exception If anything goes wrong.
     */
    private void generateDocumentationForContainer(String containerId) throws Exception
    {
        Writer writer = new FileWriter(System.getProperty("basedir") + "/target/" + containerId
            + ".log");
        writer.write(this.generator.generateDocumentation(containerId));
        writer.close();
    }
}
