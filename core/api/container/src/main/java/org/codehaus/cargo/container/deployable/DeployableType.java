/*
 * ========================================================================
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
package org.codehaus.cargo.container.deployable;

/**
 * Type of Depoyable. Can be WAR, EAR, EXPANDED_WAR, etc.
 * 
 * @version $Id$
 */
public final class DeployableType
{
    /**
     * The WAR deployable type.
     */
    public static final DeployableType WAR = new DeployableType("war");

    /**
     * The EAR deployable type.
     */
    public static final DeployableType EAR = new DeployableType("ear");

    /**
     * The EJB deployable type.
     */
    public static final DeployableType EJB = new DeployableType("ejb");

    /**
     * The SAR deployable type.
     */
    public static final DeployableType SAR = new DeployableType("sar");

    /**
     * The RAR deployable type.
     */
    public static final DeployableType RAR = new DeployableType("rar");

    /**
     * The File deployable type.
     */
    public static final DeployableType FILE = new DeployableType("file");

    /**
     * The OSGi Bundle deployable type.
     */
    public static final DeployableType BUNDLE = new DeployableType("bundle");

    /**
     * The HAR deployable type.
     */
    public static final DeployableType HAR = new DeployableType("har");

    /**
     * The AOP deployable type.
     */
    public static final DeployableType AOP = new DeployableType("aop");

    /**
     * A unique id that identifies a deployable type.
     */
    private String type;

    /**
     * @param type A unique id that identifies a deployable type
     */
    private DeployableType(String type)
    {
        this.type = type;
    }

    /**
     * Transform a type represented as a string into a {@link DeployableType} object.
     * 
     * @param typeAsString the string to transform
     * @return the {@link DeployableType} object
     */
    public static DeployableType toType(String typeAsString)
    {
        DeployableType type;

        if (typeAsString.equalsIgnoreCase(WAR.type))
        {
            type = WAR;
        }
        else if (typeAsString.equalsIgnoreCase(EAR.type))
        {
            type = EAR;
        }
        else if (typeAsString.equalsIgnoreCase(EJB.type))
        {
            type = EJB;
        }
        else if (typeAsString.equalsIgnoreCase(SAR.type))
        {
            type = SAR;
        }
        else if (typeAsString.equalsIgnoreCase(RAR.type))
        {
            type = RAR;
        }
        else if (typeAsString.equalsIgnoreCase(FILE.type))
        {
            type = FILE;
        }
        else if (typeAsString.equalsIgnoreCase(BUNDLE.type))
        {
            type = BUNDLE;
        }
        else
        {
            type = new DeployableType(typeAsString);
        }

        return type;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object instanceof DeployableType)
        {
            DeployableType type = (DeployableType) object;
            if (type.type.equalsIgnoreCase(this.type))
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
    @Override
    public int hashCode()
    {
        return this.type.hashCode();
    }

    /**
     * @return the deployable type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return this.type;
    }
}
