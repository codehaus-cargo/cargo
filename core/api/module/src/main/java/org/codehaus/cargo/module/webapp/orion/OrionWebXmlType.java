/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.orion;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id$
 * 
 */
public class OrionWebXmlType extends AbstractDescriptorType
{
    /**
     * Single instance.
     */
    private static OrionWebXmlType instance = new OrionWebXmlType();

    /**
     * Protected Constructor.
     */
    protected OrionWebXmlType()
    {
        super(null, OrionWebXml.class, new Dtd(
            "http://www.oracle.com/technology/ias/dtds/orion-web-9_04.dtd"));
        setDescriptorIo(new OrionWebXmlIo(this));
    }

    /**
     * Get Static Singleton instance.
     * @return OrionWebXmlType
     */
    public static OrionWebXmlType getInstance()
    {
        return instance;
    }

}
