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
package org.codehaus.cargo.module.webapp.websphere;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.Grammar;

/**
 * Grammar for a websphere web application descriptor.
 * 
 * @version $Id$
 */
public class IbmWebBndXmiGrammar implements Grammar
{
    /**
     * Name of the root tag.
     */
    private static final String ROOT = "WebAppBinding";
    
    /**
     * Name with namespace of the root tag.
     */
    private static final String ROOT_NAMESPACE = 
        "com.ibm.ejs.models.base.bindings.webappbnd:WebAppBinding";

    /**
     * {@inheritDoc}
     * @see Grammar#getElementOrder(String) 
     */
    public List getElementOrder(String tagName)
    {
        List elementOrder = null;
        if (tagName.equals(ROOT) || tagName.equals(ROOT_NAMESPACE))
        {
            elementOrder = new ArrayList();
            elementOrder.add(new DescriptorTag(
                IbmWebBndXmiType.getInstance(), "virtualHostName", true));
            elementOrder.add(new DescriptorTag(
                IbmWebBndXmiType.getInstance(), "webapp", true));
            elementOrder.add(new DescriptorTag(
                IbmWebBndXmiType.getInstance(), "resRefBindings", true));
            elementOrder.add(new DescriptorTag(
                IbmWebBndXmiType.getInstance(), "ejbRefBindings", true));
            elementOrder.add(new DescriptorTag(
                IbmWebBndXmiType.getInstance(), "resourceEnvRefBindings", true));
        }
        
        return elementOrder;
    }
}
