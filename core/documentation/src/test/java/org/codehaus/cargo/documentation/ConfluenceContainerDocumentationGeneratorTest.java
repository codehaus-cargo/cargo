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
package org.codehaus.cargo.documentation;

import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.util.FileHandler;

/**
 * Unit tests for {@link ConfluenceContainerDocumentationGenerator}
 */
public class ConfluenceContainerDocumentationGeneratorTest
{
    /**
     * Documentation generator.
     */
    private ConfluenceContainerDocumentationGenerator generator;

    /**
     * Creates the {@link ConfluenceProjectStructureDocumentationGenerator}
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        this.generator = new ConfluenceContainerDocumentationGenerator();
    }

    /**
     * Test computed qualified class name.
     */
    @Test
    public void testComputedFQCN()
    {
        Assertions.assertEquals("o.c.c.c.myc.MyContainerIsTheBest",
            this.generator.computedFQCN("org.codehaus.cargo.container.myc.MyContainerIsTheBest"));
    }

    /**
     * Generate datasource documentation.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGenerateDatasourceDocumentation() throws Exception
    {
        try (Writer writer =
            new FileWriter(System.getProperty("basedir") + "/target/datasource.log"))
        {
            writer.write(this.generator.generateDatasourceDocumentation());
        }
    }

    /**
     * Generate documentation for all containers.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGenerateDocumentationForAllContainers() throws Exception
    {
        try (Writer writer =
            new FileWriter(System.getProperty("basedir") + "/target/container-urls.properties"))
        {
            ContainerFactory factory = new DefaultContainerFactory();
            Map<String, Set<ContainerType>> containerIds = factory.getContainerIds();
            for (String containerId : containerIds.keySet())
            {
                generateDocumentationForContainer(containerId);
                String url = this.generator.getContainerServerDownloadUrl(containerId);
                if (url != null)
                {
                    writer.write("cargo." + containerId + ".url=" + url
                        + FileHandler.NEW_LINE);
                }
            }
        }
    }

    /**
     * Generate documentation for a given container.
     * @param containerId Container id.
     * @throws Exception If anything goes wrong.
     */
    private void generateDocumentationForContainer(String containerId) throws Exception
    {
        try (Writer writer = new FileWriter(System.getProperty("basedir") + "/target/" + containerId
            + ".log"))
        {
            writer.write(this.generator.generateDocumentation(containerId));
        }
    }
}
