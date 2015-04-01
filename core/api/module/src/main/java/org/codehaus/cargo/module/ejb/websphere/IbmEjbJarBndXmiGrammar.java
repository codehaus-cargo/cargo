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
package org.codehaus.cargo.module.ejb.websphere;

import java.util.List;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.Grammar;

/**
 * Websphere specific grammar implementation. Since websphere uses xml schemas to describe their
 * descriptors and no general xml schema Grammar implementation exists we have to use a specific
 * one.
 * 
 */
public class IbmEjbJarBndXmiGrammar implements Grammar
{
    /**
     * {@inheritDoc}
     * @see Grammar#getElementOrder(String)
     */
    public List<DescriptorTag> getElementOrder(String tagName)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
