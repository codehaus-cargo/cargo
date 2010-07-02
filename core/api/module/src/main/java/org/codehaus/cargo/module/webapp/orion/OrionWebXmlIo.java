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

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.codehaus.cargo.module.DescriptorType;
import org.jdom.JDOMException;

/**
 * Provides convenience methods for reading Oracle web deployment descriptor.
 * 
 * @version $Id$
 */
public class OrionWebXmlIo extends AbstractDescriptorIo
{
    /**
     * @param factory
     */
    protected OrionWebXmlIo()
    {
        super(OrionWebXmlType.getInstance());
    }

    /**
     * Constructor.
     * @param type Document descriptor type
     */
    public OrionWebXmlIo(DescriptorType type)
    {
        super(type);
    }

  /**
   * Parses a deployment descriptor provided as input stream.
   * 
   * @param input The input stream
   * @return The parsed descriptor
   * @throws JDOMException If the input could not be parsed
   * @throws IOException If an I/O error occurs
   */
    public static OrionWebXml parseOrionXml(InputStream input) throws
      IOException, JDOMException
    {
        OrionWebXmlIo io = new OrionWebXmlIo();
        return (OrionWebXml) io.parseXml(input);
    }

}
