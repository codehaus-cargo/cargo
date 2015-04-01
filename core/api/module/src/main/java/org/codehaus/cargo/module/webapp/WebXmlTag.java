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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;
import org.codehaus.cargo.module.Identifier;

/**
 * Represents the various top-level tags in a web deployment descriptor as a typesafe enumeration.
 * 
 */
public final class WebXmlTag extends DescriptorTag
{

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     * @param identifier Optional tag identifier instance
     * @param clazz Name of the implementation class
     */
    protected WebXmlTag(DescriptorType type, String tagName,
        boolean isMultipleAllowed, Identifier identifier, Class clazz)
    {
        super(type, tagName, ((WebXmlType) type).getVersion().getNamespace(), isMultipleAllowed,
                identifier, clazz);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected WebXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, ((WebXmlType) type).getVersion().getNamespace(), isMultipleAllowed,
                null, null);
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected WebXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }

}
