/*
 * ========================================================================
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
package org.codehaus.cargo.module.webapp.tomcat;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 */
public class TomcatContextXmlType extends AbstractDescriptorType
{
    /**
     * Static instance.
     */
    private static TomcatContextXmlType instance = new TomcatContextXmlType();

    /**
     * All the tags for the descriptor type.<br>
     * The warning <i>value of the field is not used</i> is irrelevant: the
     * <code>DescriptorTag</code> constructor performs the registrations.
     */
    private TomcatContextXmlTag[] tags =
        new TomcatContextXmlTag[] {
            new TomcatContextXmlTag(this, "path", false),
            new TomcatContextXmlTag(this, "Parameter", true)
        };

    /**
     * Protected constructor.
     */
    protected TomcatContextXmlType()
    {
        super(null, TomcatContextXml.class, new Dtd("file:sample/tomcat-context.dtd"));
    }

    /**
     * Get the static instance.
     * @return The instance
     */
    public static TomcatContextXmlType getInstance()
    {
        return instance;
    }
}
