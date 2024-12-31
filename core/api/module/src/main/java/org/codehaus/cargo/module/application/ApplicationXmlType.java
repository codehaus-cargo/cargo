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

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * Descriptor type for application xml.
 */
public class ApplicationXmlType extends AbstractDescriptorType
{
    /**
     * Single instance of the ApplicationXmlType.
     */
    private static ApplicationXmlType instance = new ApplicationXmlType();

    /**
     * All the tags in this type.<br>
     * The warning <i>value of the field is not used</i> is irrelevant: the
     * <code>DescriptorTag</code> constructor performs the registrations.
     */
    private ApplicationXmlTag[] tags =
        new ApplicationXmlTag[] {
            new ApplicationXmlTag(this, "icon"),
            new ApplicationXmlTag(this, "display-name"),
            new ApplicationXmlTag(this, "description"),
            new ApplicationXmlTag(this, "module"),
            new ApplicationXmlTag(this, "ejb"),
            new ApplicationXmlTag(this, "web"),
            new ApplicationXmlTag(this, "web-uri"),
            new ApplicationXmlTag(this, "context-root"),
            new ApplicationXmlTag(this, "security-role"),
            new ApplicationXmlTag(this, "icon"),
            new ApplicationXmlTag(this, "display-name"),
            new ApplicationXmlTag(this, "description"),
            new ApplicationXmlTag(this, "module"),
            new ApplicationXmlTag(this, "ejb"),
            new ApplicationXmlTag(this, "web"),
            new ApplicationXmlTag(this, "web-uri"),
            new ApplicationXmlTag(this, "context-root"),
            new ApplicationXmlTag(this, "security-role")};

    /**
     * Protected Constructor.
     */
    protected ApplicationXmlType()
    {
        super(null, ApplicationXml.class, new Dtd("http://java.sun.com/dtd/application_1_3.dtd"));
    }

    /**
     * Get the application XML Descriptor type.
     * @return the singleton instance
     */
    public static ApplicationXmlType getInstance()
    {
        return instance;
    }

}
