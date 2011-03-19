/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.application;

import org.codehaus.cargo.module.DescriptorTag;

/**
 * Represents the various top-level tags in a enterprise application deployment descriptor as a
 * typesafe enumeration.
 * 
 * @version $Id$
 */
public final class ApplicationXmlTag extends DescriptorTag
{

    /**
     * Element name 'icon'.
     */
    public static final String ICON = "icon";

    /**
     * Element name 'display-name'.
     */
    public static final String DISPLAY_NAME = "display-name";

    /**
     * Element name 'description'.
     */
    public static final String DESCRIPTION = "description";

    /**
     * Element name 'module'.
     */
    public static final String MODULE = "module";

    /**
     * Element name 'ejb'.
     */
    public static final String EJB = "ejb";

    /**
     * Element name 'web'.
     */
    public static final String WEB = "web";

    /**
     * Element name 'web-uri'.
     */
    public static final String WEB_URI = "web-uri";

    /**
     * Element name 'context-root'.
     */
    public static final String CONTEXT_ROOT = "context-root";

    /**
     * Element name 'security-role'.
     */
    public static final String SECURITY_ROLE = "security-role";

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected ApplicationXmlTag(ApplicationXmlType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected ApplicationXmlTag(ApplicationXmlType type, String tagName)
    {
        this(type, tagName, true);
    }
}
