/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module;

/**
 * Represents the various top-level tags in a deployment descriptor as a typesafe enumeration.
 *
 * @version $Id$
 */
public class DescriptorTag
{
    /**
     * The tag name.
     */
    private String tagName;

    /**
     * Whether multiple occurrences of the tag in the descriptor are allowed.
     */
    private boolean multipleAllowed;

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    public DescriptorTag(String tagName, boolean isMultipleAllowed)
    {
        this.tagName = tagName;
        this.multipleAllowed = isMultipleAllowed;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public boolean equals(Object other)
    {
        boolean eq = false;
        if (other instanceof DescriptorTag)
        {
            DescriptorTag tag = (DescriptorTag) other;
            if (tag.getTagName().equals(this.tagName))
            {
                eq = true;
            }
        }
        return eq;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return this.getTagName().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.DescriptorTag#getTagName()
     */
    public String getTagName()
    {
        return this.tagName;
    }

    /**
     * {@inheritDoc}
     * @see DescriptorTag#isMultipleAllowed()
     */
    public boolean isMultipleAllowed()
    {
        return this.multipleAllowed;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString
     */
    public String toString()
    {
        return getTagName();
    }
}
