/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.ant;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for {@link DeployableElement}.
 * 
 * @version $Id$
 */
public class DeployableElementTest extends TestCase
{
    /**
     * The deployable element.
     */
    private DeployableElement element = new DeployableElement();

    /**
     * Test the getter and setter method name.
     */
    public void testGetSetterMethodName()
    {
        assertEquals("setContext", this.element.getSetterMethodName("context"));
    }

    /**
     * Test the creation of a WAR deployable with context.
     */
    public void testCreateWarDeployableWithContext()
    {
        Property contextProperty = new Property();
        contextProperty.setName("context");
        contextProperty.setValue("customContext");

        this.element.setType("war");
        this.element.setFile("/some/path/to/war");
        this.element.addConfiguredProperty(contextProperty);

        Deployable war = this.element.createDeployable("customContainer");

        assertEquals(DeployableType.WAR, war.getType());
        assertEquals("customContext", ((WAR) war).getContext());
    }

    /**
     * Test the creation of a WAR deployable with an invalid property.
     */
    public void testCreateWarDeployableWithInvalidProperty()
    {
        Property property = new Property();
        property.setName("invalidProperty");
        property.setValue("whatever");

        this.element.setType("war");
        this.element.setFile("/some/path/to/war");
        this.element.addConfiguredProperty(property);

        try
        {
            this.element.createDeployable("customContainer");
            fail("Should have thrown an exception here");
        }
        catch (BuildException expected)
        {
            assertEquals("Invalid property [invalidProperty] for deployable type [war]",
                expected.getMessage());
        }

    }
}
