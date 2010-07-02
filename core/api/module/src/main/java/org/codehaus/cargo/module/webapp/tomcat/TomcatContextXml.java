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
package org.codehaus.cargo.module.webapp.tomcat;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.cargo.module.AbstractDescriptor;
import org.codehaus.cargo.module.DescriptorType;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Encapsulates the DOM representation of a web deployment descriptor 
 * <code>META-INF/context.xml</code> to provide convenience methods for easy access and 
 * manipulation.
 * 
 * @version $Id$
 */
public class TomcatContextXml extends AbstractDescriptor
{
    /**
     * File name of this descriptor.
     */
    private static final String FILE_NAME = "jboss-web.xml";

    /**
     * Specifies the order in which the top-level elements must appear in the descriptor, according 
     * to the DTD.
     */
//    private static final TomcatContextXmlTag[] ELEMENT_ORDER =
//    {
//        TomcatContextXmlTag.CONTEXT_PATH
//    };

    /**
     * Constructor.
     * 
     * @param rootElement The root element of the context
     * @param type the document descriptor type
     */
    public TomcatContextXml(Element rootElement, DescriptorType type)
    {
        super(rootElement, type);
    }

    /**
     * @return the context path element found in the <code>context.xml</code> file (available in 
     *         the <code>path</code> attribute) or null if not defined
     */
    public String getPath()
    {
        Attribute attr = getRootElement().getAttribute(TomcatContextXmlTag.CONTEXT_PATH);
        if (attr == null)
        {
            return null;
        }
        
        String path = attr.getValue();

        // An empty path string means a path not defined.
        if (path.length() == 0)
        {
            path = null;
        }
        else
        {
            // Remove leading slash if there is one.
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }
        }

        return path;
    }

    /**
     * Gets all the parameters that match XPath "<tt>Context/Parameter</tt>".
     *
     * @return
     *      Always non-null (but possibly empty) map keyed by
     *      <tt>Context/Parameter/@name</tt> and value is
     *      <tt>Context/Parameter/@value</tt>
     */
    public Map getParameters()
    {
        Map r = new TreeMap();
        for (Iterator itr = getElements(TomcatContextXmlTag.PARAMETER); itr.hasNext();)
        {
            Element e = (Element) itr.next();
            r.put(e.getAttribute("name"), e.getAttribute("value"));
        }

        return r;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.Descriptor#getFileName()
     */
    public final String getFileName()
    {
        return FILE_NAME;
    }
}
