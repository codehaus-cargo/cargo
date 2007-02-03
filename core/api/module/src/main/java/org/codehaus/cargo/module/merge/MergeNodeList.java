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

import java.util.Iterator;

import org.codehaus.cargo.module.internal.util.xml.AbstractElement;
import org.codehaus.cargo.module.internal.util.xml.AbstractNodeList;
import org.w3c.dom.Element;

/**
 * A MergedNodeList is a MergeSet that applies to AbstractNodeLists.
 *
 * @version $Id: $
 */
public class MergeNodeList extends AbstractMergeSet
{
    /**
     * The set of everything from the left hand side.
     */
    private AbstractNodeList leftSet;

    /**
     * The set of everything from the right hand side.
     */
    private AbstractNodeList rightSet;

    /**
     * Constructor.
     *
     * @param leftSet in the left hand set
     * @param rightSet in the right hand set
     */
    protected MergeNodeList(AbstractNodeList leftSet, AbstractNodeList rightSet)
    {
        this.leftSet = leftSet;
        this.rightSet = rightSet;
    }

    /**
     * Static constructor. Make a merge set from the two node sets, using the
     * elements 'name' in order to determine whether it is in conflict.
     * 
     * @param leftSet in the left hand set
     * @param rightSet in the right hand set
     * @return the generated merge set
     */
    public static final AbstractMergeSet createFromNames(AbstractNodeList leftSet,
        AbstractNodeList rightSet)
    {
        AbstractMergeSet results = new MergeNodeList(leftSet, rightSet);

        Iterator i = leftSet.iterator();
        while (i.hasNext())
        {
            AbstractElement left = (AbstractElement) i.next();
            AbstractElement right = rightSet.getByElementId(left.getElementId());

            if (right == null)
            {
                results.inLeftOnly.add(left);
            }
            else
            {
                results.inBoth.add(new MergePair(left, right));
            }
        }

        i = rightSet.iterator();

        while (i.hasNext())
        {
            AbstractElement right = (AbstractElement) i.next();
            AbstractElement left = leftSet.getByElementId(right.getElementId());

            if (left == null)
            {
                results.inRightOnly.add(right);
            }
        }
        return results;
    }

    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.AbstractMergeSet#add(org.w3c.dom.Element)
     */
    public void add(Element e)
    {
        this.leftSet.add(e);       
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.AbstractMergeSet#remove(org.w3c.dom.Element)
     */
    public void remove(Element e)
    {
        this.leftSet.remove(e);        
    }
}
