/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module.merge;

import org.codehaus.cargo.module.internal.util.xml.AbstractElement;
import org.w3c.dom.Element;

/**
 * Class containing a pair of items that need to be merged.
 * 
 * @version $Id: $
 */
public class MergePair
{
    /**
     * Left and right hand elements.
     */
    public Element left;
    
    /**
     * Right.
     */
    public Element right;

    /**
     * Constructor.
     *
     * @param left left element
     * @param right right element
     */
    MergePair(Element left, Element right)
    {
        this.left = left;
        this.right = right;
    }
    
    /**
     * Get the left hand side element in the pair.
     *
     * @return the left hand element
     */
    public Element getLeftElement()
    {
        if (this.left instanceof AbstractElement)
        {
            return (Element) ((AbstractElement) this.left).getNode();
        }
        
        return this.left;
    }
    
    /**
     * Get the right hand side element in the pair.
     *
     * @return the right hand element
     */
    public Element getRightElement()
    {
        if (this.right instanceof AbstractElement)
        {
            return (Element) ((AbstractElement) this.right).getNode();
        }
        
        return this.right;
    }
}
