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
package org.codehaus.cargo.module.webapp.tomcat;

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various top-level tags in a Tomcat context web deployment 
 * descriptor as a typesafe enumeration.
 * 
 * @version $Id$
 */
public final class TomcatContextXmlTag extends DescriptorTag
{
    /**
     * Attribute name 'path'.
     */
    public static final TomcatContextXmlTag CONTEXT_PATH =
        new TomcatContextXmlTag("path", false);

    /**
     * Element name 'Parameter'.
     */
    public static final TomcatContextXmlTag PARAMETER =
        new TomcatContextXmlTag("Parameter", true);

    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in
     *         the descriptor
     */
    protected TomcatContextXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param tagName The tag name of the element
     */
    protected TomcatContextXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
