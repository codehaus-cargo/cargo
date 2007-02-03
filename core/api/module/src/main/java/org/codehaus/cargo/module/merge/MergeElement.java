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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * MergeElement - a MergeSet that only contains one element from left or right.
 *
 * @version $Id: $
 */
public class MergeElement extends AbstractMergeSet
{
    /**
     * The parent of the left-hand element.
     */
    private Element leftParent;

    /**
     * Constructor.
     * 
     * NB: leftParent is needed as it is allowable for left to be null, in 
     * the event that it does not exist (And so could not be determined otherwise)
     * 
     * @param leftParent in the left hand elements parent
     * @param left in the left hand element
     * @param right in the right hand element
     */
    public MergeElement(Element leftParent, Element left, Element right)
    {
        super();

        this.leftParent = leftParent;

        if (left == null)
        {
            this.inRightOnly.add(right);
        }
        else if (right == null)
        {
            this.inLeftOnly.add(left);
        }
        else
        {
            this.inBoth.add(new MergePair(left, right));
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.AbstractMergeSet#add(org.w3c.dom.Element)
     */
    public void add(Element e)
    {
        Node n = this.leftParent.getOwnerDocument().importNode(e, true);
        this.leftParent.appendChild(n);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.AbstractMergeSet#remove(org.w3c.dom.Element)
     */
    public void remove(Element e)
    {
        this.leftParent.removeChild(e);
    }
}
