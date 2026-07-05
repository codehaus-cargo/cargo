/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.configuration.script;

import org.junit.jupiter.api.Test;

import junit.framework.Assert;

/**
 * Tests {@link AbstractScriptCommand} implementation.
 */
public class ScriptCommandTest extends AbstractScriptCommand
{

    /**
     * Instanciates {@link AbstractScriptCommand} with a <code>null</code> configuration.
     */
    public ScriptCommandTest()
    {
        super(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readScript()
    {
        throw new UnsupportedOperationException("Unimplemented method 'readScript'");
    }

    /**
     * Tests the <code>escapeDeployableName</code> method.
     */
    @Test
    public void testEscapeDeployableName()
    {
        Assert.assertEquals("ROOT", escapeDeployableName(null));
        Assert.assertEquals("ROOT", escapeDeployableName(""));
        Assert.assertEquals("ROOT", escapeDeployableName(" "));
        Assert.assertEquals("ROOT", escapeDeployableName("/"));
        Assert.assertEquals("ROOT", escapeDeployableName("\\"));
        Assert.assertEquals("ROOT", escapeDeployableName(" / \\  "));

        Assert.assertEquals("test", escapeDeployableName("/test"));
        Assert.assertEquals("test", escapeDeployableName("test/"));
        Assert.assertEquals("test", escapeDeployableName("\\test"));
        Assert.assertEquals("test", escapeDeployableName("test\\"));

        Assert.assertEquals("test_test", escapeDeployableName("/test/test"));
        Assert.assertEquals("test-test_test", escapeDeployableName("test-test/test"));
    }
}
