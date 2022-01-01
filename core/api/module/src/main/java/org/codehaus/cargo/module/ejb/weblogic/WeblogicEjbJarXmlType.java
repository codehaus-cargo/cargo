/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.module.ejb.weblogic;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 */
public class WeblogicEjbJarXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static WeblogicEjbJarXmlType instance = new WeblogicEjbJarXmlType();

    /**
     * All the tags in this type.<br>
     * The warning <i>value of the field is not used</i> is irrelevant: the
     * <code>DescriptorTag</code> constructor performs the registrations.
     */
    private WeblogicEjbJarXmlTag[] tags = new WeblogicEjbJarXmlTag[] {
        new WeblogicEjbJarXmlTag(this, "ejb-name"),
        new WeblogicEjbJarXmlTag(this, "local-jndi-name"),
        new WeblogicEjbJarXmlTag(this, "jndi-name"),
        new WeblogicEjbJarXmlTag(this, "dispatch-policy")
    };

    /**
     * Protected constructor.
     */
    protected WeblogicEjbJarXmlType()
    {
        super(null, WeblogicEjbJarXml.class,
            new Dtd("http://www.bea.com/servers/wls810/dtd/weblogic-ejb-jar.dtd"));
    }

    /**
     * Get the static instance.
     * @return The instance
     */
    public static WeblogicEjbJarXmlType getInstance()
    {
        return instance;
    }

}
