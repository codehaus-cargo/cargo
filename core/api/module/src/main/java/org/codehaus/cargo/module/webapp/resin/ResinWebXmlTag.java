/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.resin;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;


/**
 * Represents the various top-level tags in a Resin context web deployment 
 * descriptor as a typesafe enumeration.
 * 
 * @version $Id $
 */
public final class ResinWebXmlTag extends DescriptorTag
{
    /**
     * Attribute name 'path'.
     */
    public static final String SYSTEM_PROPERTY = "system-property";
    
    /**
     * Elements of resource-ref.
     */
    public static final String RESOURCE_REFERENCE = "resource-ref";

    /**
     * Elements of session-config.
     */
    public static final String SESSION_CONFIG = "session-config";

    /**
     * Elements of directory-servlet.
     */
    public static final String DIRECTORY_SERVLET = "directory-servlet";

    /**
     * Elements of jndi-link.
     */
    public static final String JNDI_LINK = "jndi-link";
    
    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in
     *         the descriptor
     */
    protected ResinWebXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected ResinWebXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }
}
