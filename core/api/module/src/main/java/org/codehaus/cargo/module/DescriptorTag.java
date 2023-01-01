/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.lang.reflect.Constructor;

import org.codehaus.cargo.util.CargoException;
import org.jdom2.Namespace;

/**
 * Represents the various top-level tags in a deployment descriptor as a typesafe enumeration.
 */
public class DescriptorTag
{
    /**
     * The type this descriptor tag is in.
     */
    private DescriptorType descriptorType;

    /**
     * The tag name.
     */
    private String tagName;

    /**
     * The tag namespace.
     */
    private Namespace tagNamespace;

    /**
     * Whether multiple occurrences of the tag in the descriptor are allowed.
     */
    private boolean multipleAllowed;

    /**
     * Optional identifier for this tag.
     */
    private Identifier identifier;

    /**
     * Element Implementation class.
     */
    private Class implementationClass;

    /**
     * Constructor.
     * 
     * @param descriptorType the type of this tag
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     * @param identifier optional tag identifier
     * @param namespace The namespace to use
     * @param clazz implementation class for this tag
     */
    public DescriptorTag(DescriptorType descriptorType, String tagName, Namespace namespace,
        boolean isMultipleAllowed, Identifier identifier, Class clazz)
    {
        this.descriptorType = descriptorType;
        this.tagName = tagName;
        this.tagNamespace = namespace;
        this.multipleAllowed = isMultipleAllowed;
        this.identifier = identifier;
        this.implementationClass = clazz;
        if (this.descriptorType != null)
        {
            descriptorType.addTag(this);
        }
    }

    /**
     * Constructor.
     * 
     * @param descriptorType the type of this tag
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    public DescriptorTag(DescriptorType descriptorType, String tagName, boolean isMultipleAllowed)
    {
        this(descriptorType, tagName, null, isMultipleAllowed, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public int hashCode()
    {
        return this.getTagName().hashCode();
    }

    /**
     * Get tag name.
     * @return the name of the tag.
     */
    public String getTagName()
    {
        return this.tagName;
    }

    /**
     * Get tag namespace.
     * @return the namespace this tag is in.
     */
    public Namespace getTagNamespace()
    {
        return this.tagNamespace;
    }

    /**
     * Is this tag allowed multiple times?
     * @return whether the tag is allowed multiple times.
     */
    public boolean isMultipleAllowed()
    {
        return this.multipleAllowed;
    }

    /**
     * @return the identifier
     */
    public Identifier getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the webXmlElementClass
     */
    public Class getImplementationClass()
    {
        return this.implementationClass;
    }

    /**
     * Return the descriptor type that this tag is defined in.
     * @return descriptor type The descriptor type
     */
    public DescriptorType getDescriptorType()
    {
        return this.descriptorType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getTagName();
    }

    /**
     * @return instantiated descriptor element, or null if no implementation class.
     * @throws CargoException if any configuration problem
     */
    public DescriptorElement create() throws CargoException
    {
        DescriptorElement returnValue = null;

        if (implementationClass == null)
        {
            returnValue = new DescriptorElement(this);
        }
        else
        {
            Constructor[] constructors = implementationClass.getConstructors();

            for (Constructor cons : constructors)
            {
                if (cons.getParameterTypes().length == 1)
                {
                    try
                    {
                        if (cons.getParameterTypes()[0].isAssignableFrom(this.getClass()))
                        {
                            returnValue = (DescriptorElement) cons.newInstance(this);
                            break;
                        }
                    }
                    catch (Exception e)
                    {
                        throw new CargoException(
                            "Error instantiating class for " + getTagName(), e);
                    }
                }
            }
        }
        return returnValue;
    }
}
