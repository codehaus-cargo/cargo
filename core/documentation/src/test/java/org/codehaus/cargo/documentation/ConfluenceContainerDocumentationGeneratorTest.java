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
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;

/**
 * Unit tests for {@link ConfluenceContainerDocumentationGenerator}
 * 
 * @version $Id: ConfluenceContainerDocumentationGeneratorTest.java 2335 2010-07-01 23:35:19Z
 * alitokmen $
 */
public class ConfluenceContainerDocumentationGeneratorTest extends TestCase
{
    private ConfluenceContainerDocumentationGenerator generator;

    @Override
    protected void setUp()
    {
        this.generator = new ConfluenceContainerDocumentationGenerator();
    }

    public void testComputedFQCN()
    {
        assertEquals("o.c.c.c.myc.MyContainerIsTheBest",
            this.generator.computedFQCN("org.codehaus.cargo.container.myc.MyContainerIsTheBest"));
    }

    public void testGenerateDocumentationForAllContainers() throws Exception
    {
        ContainerFactory factory = new DefaultContainerFactory();

        Map containerIds = factory.getContainerIds();
        for (Iterator ids = containerIds.keySet().iterator(); ids.hasNext();)
        {
            String id = (String) ids.next();
            generateDocumentationForContainer(id);
        }
    }

    private void generateDocumentationForContainer(String containerId) throws Exception
    {
        Writer writer = new FileWriter(System.getProperty("basedir") + "/target/" + containerId
            + ".log");
        writer.write(this.generator.generateDocumentation(containerId));
        writer.close();
    }
}
