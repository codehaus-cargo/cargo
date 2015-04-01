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
package org.codehaus.cargo.module.ejb;

import org.w3c.dom.DocumentType;

/**
 * Enumerated type that represents the version of the deployment descriptor of a ejb descriptor
 * (ejb-jar.xml).
 * 
 */
public final class EjbJarXmlVersion implements Comparable
{
    /**
     * Instance for version 2.0.
     */
    public static final EjbJarXmlVersion V2_0 = new EjbJarXmlVersion(
        "2.0",
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN",
        "http://java.sun.com/dtd/ejb-jar_2_0.dtd");

    /**
     * The system ID of the corresponding document type.
     */
    private String systemId;

    /**
     * The version as string.
     */
    private String version;

    /**
     * The public ID of the corresponding document type.
     */
    private String publicId;

    /**
     * Constructor.
     * 
     * @param version The version as string
     * @param publicId The public ID of the correspondig document type
     * @param systemId The system ID of the correspondig document type
     */
    private EjbJarXmlVersion(String version, String publicId, String systemId)
    {
        this.version = version;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Comparable#compareTo
     */
    public int compareTo(Object other)
    {
        int result = 1;

        if (other == this)
        {
            result = 0;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString
     */
    @Override
    public boolean equals(Object other)
    {
        return super.equals(other);
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Returns the tag name.
     * 
     * @return The tag name
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Returns the public ID of the document type corresponding to the descriptor version.
     * 
     * @return The public ID
     */
    public String getPublicId()
    {
        return this.publicId;
    }

    /**
     * Returns the system ID of the document type corresponding to the descriptor version.
     * 
     * @return The system ID
     */
    public String getSystemId()
    {
        return this.systemId;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString
     */
    @Override
    public String toString()
    {
        return getVersion();
    }

    /**
     * Returns the version corresponding to the given document type.
     * 
     * @param docType The document type
     * @return The version that matches the document type, or <code>null</code> if the doctype is
     * not recognized
     * @throws NullPointerException If the document type is <code>null</code>
     */
    public static EjbJarXmlVersion valueOf(DocumentType docType) throws NullPointerException
    {
        return valueOf(docType.getPublicId());
    }

    /**
     * Returns the version corresponding to the given public ID.
     * 
     * @param publicId The public ID
     * @return The version that matches the public ID, or <code>null</code> if the ID is not
     * recognized
     */
    public static EjbJarXmlVersion valueOf(String publicId)
    {
        EjbJarXmlVersion version = null;

        if (V2_0.getPublicId().equals(publicId))
        {
            version = EjbJarXmlVersion.V2_0;
        }
        return version;
    }
}
