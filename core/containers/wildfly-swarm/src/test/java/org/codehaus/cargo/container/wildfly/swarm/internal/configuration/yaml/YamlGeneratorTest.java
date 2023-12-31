/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.configuration.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Tests YAML creation used by WildFly Swarm configurator classes.
 */
public class YamlGeneratorTest extends TestCase
{
    /**
     * Factory for creating {@link YAMLGenerator} instances.
     */
    private final YAMLFactory yamlFactory = new YAMLFactory();

    /**
     * YAML generator under test.
     */
    private YAMLGenerator yamlGenerator;

    /**
     * Prepares test environment - creates a new YAML generator.
     */
    @Override
    public void setUp()
    {
        this.yamlGenerator = createYamlGenerator();
    }

    /**
     * Closes the YAML generator used in tests.
     */
    @Override
    public void tearDown()
    {
        closeGenerator();
    }

    /**
     * Tests a simple {@link YAMLGenerator} usage.
     * @throws IOException if creating YAML content fails.
     */
    public void testSimpleYamlGeneration() throws IOException
    {
        assertFalse(yamlFactory.isEnabled(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        assertTrue(yamlFactory.isEnabled(YAMLGenerator.Feature.MINIMIZE_QUOTES));

        yamlGenerator.writeStartObject();
        yamlGenerator.writeFieldName("swarm");
        yamlGenerator.writeStartObject();
        yamlGenerator.writeStringField("management", "true");
        yamlGenerator.writeEndObject();
        yamlGenerator.writeEndObject();

        final String result = getYamlString();

        assertEquals("swarm:\n  management: \"true\"", result);
    }

    /**
     * Converts YAML generator's content to string.
     * @return YAML string.
     */
    private String getYamlString()
    {
        try
        {
            yamlGenerator.flush();
            String result = yamlGenerator.getOutputTarget().toString();
            return result;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            try
            {
                yamlGenerator.close();
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }

    }

    /**
     * Closes the YAML generator.
     */
    private void closeGenerator()
    {
        if (yamlGenerator != null)
        {
            try
            {
                yamlGenerator.close();
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Creates a new generator.
     * @return YAMLGenerator instance.
     */
    private YAMLGenerator createYamlGenerator()
    {
        final Writer writer = new StringWriter();
        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        try
        {
            return yamlFactory.createGenerator(writer);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Cannot create YAML generator.", ex);
        }
    }
}
