/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import org.jdom2.DocType;

/**
 * Enumerated type that represents the version of the deployment descriptor of a enterprise
 * application (application.xml).
 */
public final class ApplicationXmlVersion implements Comparable<ApplicationXmlVersion>
{
    /**
     * Instance for version 1.2.
     */
    public static final ApplicationXmlVersion V1_2 = new ApplicationXmlVersion(
        "1.2",
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN",
        "http://java.sun.com/j2ee/dtds/application_1_2.dtd");

    /**
     * Instance for version 1.3.
     */
    public static final ApplicationXmlVersion V1_3 = new ApplicationXmlVersion(
        "1.3",
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN",
        "http://java.sun.com/dtd/application_1_3.dtd");

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
     * @param theVersion The version as string
     * @param thePublicId The public ID of the correspondig document type
     * @param theSystemId The system ID of the correspondig document type
     */
    private ApplicationXmlVersion(String theVersion, String thePublicId, String theSystemId)
    {
        this.version = theVersion;
        this.publicId = thePublicId;
        this.systemId = theSystemId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ApplicationXmlVersion other)
    {
        double thisVersion = this.version == null ? 0 : Double.parseDouble(this.version);
        double thatVersion =
            (other == null || other.version == null) ? 0 : Double.parseDouble(other.version);
        return Double.compare(thisVersion, thatVersion);
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
     */
    @Override
    public String toString()
    {
        return getVersion();
    }

    /**
     * Returns the version corresponding to the given document type.
     * 
     * @param theDocType The document type
     * @return The version that matches the document type, or <code>null</code> if the doctype is
     * not recognized
     * @throws NullPointerException If the document type is <code>null</code>
     */
    public static ApplicationXmlVersion valueOf(DocType theDocType) throws NullPointerException
    {
        return valueOf(theDocType.getPublicID());
    }

    /**
     * Returns the version corresponding to the given public ID.
     * 
     * @param thePublicId The public ID
     * @return The version that matches the public ID, or <code>null</code> if the ID is not
     * recognized
     */
    public static ApplicationXmlVersion valueOf(String thePublicId)
    {
        ApplicationXmlVersion version = null;

        if (V1_2.getPublicId().equals(thePublicId))
        {
            version = ApplicationXmlVersion.V1_2;
        }
        else if (V1_3.getPublicId().equals(thePublicId))
        {
            version = ApplicationXmlVersion.V1_3;
        }
        return version;
    }
}
