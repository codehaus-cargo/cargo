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
package org.codehaus.cargo.module.ejb;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;

/**
 * Represents the various tags in a ejb jar deployment descriptor.
 * 
 * @version $Id$
 */
public class EjbJarXmlTag extends DescriptorTag
{
    /**
     * Element name 'session'.
     */
    public static final String SESSION = "session";

    /**
     * Element name 'entity'.
     */
    public static final String ENTITY = "entity";

    /**
     * Element name 'ejb-name'.
     */
    public static final String EJB_NAME = "ejb-name";

    /**
     * Element name 'local'.
     */
    public static final String LOCAL = "local";

    /**
     * Element name 'local-home'.
     */
    public static final String LOCAL_HOME = "local-home";

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected EjbJarXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected EjbJarXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }
}
