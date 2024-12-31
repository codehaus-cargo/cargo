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
package org.codehaus.cargo.module.webapp;

import org.jdom2.DocType;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Enumerated type that represents the version of the web deployment descriptor.
 */
public final class WebXmlVersion implements Comparable<WebXmlVersion>
{
    /**
     * Instance for version 2.2.
     */
    public static final WebXmlVersion V2_2 = new WebXmlVersion("2.2",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
        "http://java.sun.com/j2ee/dtds/web-app_2_2.dtd");

    /**
     * Instance for version 2.3.
     */
    public static final WebXmlVersion V2_3 = new WebXmlVersion("2.3",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
        "http://java.sun.com/dtd/web-app_2_3.dtd");

    /**
     * Instance for version 2.4.
     */
    public static final WebXmlVersion V2_4 = new WebXmlVersion("2.4",
        "http://java.sun.com/xml/ns/j2ee");

    /**
     * Instance for version 2.5.
     */
    public static final WebXmlVersion V2_5 = new WebXmlVersion("2.5",
        "http://java.sun.com/xml/ns/javaee");

    /**
     * Instance for version 3.0.
     */
    public static final WebXmlVersion V3_0 = new WebXmlVersion("3.0",
        "http://java.sun.com/xml/ns/javaee");

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
     * The namespace for tags.
     */
    private Namespace namespace;

    /**
     * Constructor.
     * 
     * @param theVersion The version as string
     * @param thePublicId The public ID of the corresponding document type
     * @param theSystemId The system ID of the corresponding document type
     */
    private WebXmlVersion(String theVersion, String thePublicId, String theSystemId)
    {
        this.version = theVersion;
        this.publicId = thePublicId;
        this.systemId = theSystemId;
    }

    /**
     * Constructor.
     * 
     * @param theVersion The version as string
     * @param namespaceUri The uri of the namespace
     */
    private WebXmlVersion(String theVersion, String namespaceUri)
    {
        this.version = theVersion;
        this.namespace = Namespace.getNamespace(namespaceUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(WebXmlVersion other)
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
     * 
     * @return The version that matches the document type, or <code>null</code> if the doctype is
     * not recognized
     * 
     * @throws NullPointerException If the document type is <code>null</code>
     */
    public static WebXmlVersion valueOf(DocType theDocType) throws NullPointerException
    {
        return valueOf(theDocType.getPublicID());
    }

    /**
     * Returns the version corresponding to the given element.
     * @param rootElement The element
     * @return The version that matches the element
     */
    public static WebXmlVersion valueOf(Element rootElement)
    {
        WebXmlVersion result = null;
        String value = rootElement.getAttributeValue("version");
        if (null != value)
        {
            switch (value)
            {
                case "2.4":
                    result = WebXmlVersion.V2_4;
                    break;

                case "2.5":
                    result = WebXmlVersion.V2_5;
                    break;

                case "3.0":
                    result = WebXmlVersion.V3_0;
                    break;

                default:
                    // result remains null
                    break;
            }
        }
        return result;
    }

    /**
     * Returns the version corresponding to the given public ID.
     * 
     * @param thePublicId The public ID
     * 
     * @return The version that matches the public ID, or <code>null</code> if the ID is not
     * recognized
     */
    public static WebXmlVersion valueOf(String thePublicId)
    {
        WebXmlVersion version = null;
        if (V2_2.getPublicId().equals(thePublicId))
        {
            version = WebXmlVersion.V2_2;
        }
        else if (V2_3.getPublicId().equals(thePublicId))
        {
            version = WebXmlVersion.V2_3;
        }
        return version;
    }

    /**
     * Return the namespace of this web xml file, or null if none.
     * @return namespace
     */
    public Namespace getNamespace()
    {
        return this.namespace;
    }

}
