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
package org.codehaus.cargo.module.ejb.jboss;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;

/**
 * Defines the JBoss XML tags.
 * @version $Id$
 */
public class JBossXmlTag extends DescriptorTag
{
    /**
     * ejb-name.
     */
    public static final String EJB_NAME = "ejb-name";

    /**
     * jndi-name.
     */
    public static final String JNDI_NAME = "jndi-name";

    /**
     * local-jndi-name.
     */
    public static final String LOCAL_JNDI_NAME = "local-jndi-name";

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected JBossXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected JBossXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }

}
