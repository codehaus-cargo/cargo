/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.packager;

/**
 * Types of {@link org.codehaus.cargo.container.packager.Packager}s.
 * There's currently one type only: "directory".
 *
 * @version $Id: $
 */
public class PackagerType
{
    /**
     * Represents a directory packager, ie a packager that creates a package in a given directory.
     */
    public static final PackagerType DIRECTORY = new PackagerType("directory");

    /**
     * {@inheritDoc}
     * @see #PackagerType(String)
     */
    private String type;

    /**
     * @param type the internal representation of the packager type. For example: "directory".
     */
    public PackagerType(String type)
    {
        this.type = type;
    }

    /**
     * Transform a type represented as a string into a {@link PackagerType} object.
     *
     * @param typeAsString the string to transform
     * @return the {@link PackagerType} object
     */
    public static PackagerType toType(String typeAsString)
    {
        PackagerType type;
        if (typeAsString.equalsIgnoreCase(DIRECTORY.getType()))
        {
            type = DIRECTORY;
        }
        else
        {
            type = new PackagerType(typeAsString);
        }

        return type;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    public boolean equals(Object object)
    {
        boolean result = false;
        if ((object != null) && (object instanceof PackagerType))
        {
            PackagerType type = (PackagerType) object;
            if (type.getType().equals(getType()))
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return this.type.hashCode();
    }

    /**
     * @return the packager's type as a string
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return getType();
    }
}
