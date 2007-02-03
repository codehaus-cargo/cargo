/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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
/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.merge.strategy.MergeStrategy;
import org.w3c.dom.Element;

/**
 * Abstract class containing a set of items that need to be merged, split up into items
 * that just appear in the left-hand side, those in the right-hand side, and
 * those that are in both (potential conflicts).
 * 
 * @version $Id $
 *
 */
public abstract class AbstractMergeSet
{
    /**
     * Items just in the left hand side.
     */
    public List inLeftOnly = new ArrayList();

    /**
     * Items just in the right hand side.
     */
    public List inRightOnly = new ArrayList();

    /**
     * Items in both sides (conflicts).
     */
    public List inBoth = new ArrayList();

    /**
     * Perform the merge using the passed strategy.
     * @param action in the strategy to use when merging
     * @return the count of merges performed
     */
    public int merge(MergeStrategy action)
    {
        int count = 0;

        for (Iterator i = this.inLeftOnly.iterator(); i.hasNext();)
        {
            Element element = (Element) i.next();
            count += action.inLeft(this, element);
        }
        for (Iterator i = this.inRightOnly.iterator(); i.hasNext();)
        {
            Element element = (Element) i.next();
            count += action.inRight(this, element);
        }
        // Merge common items by overwriting them
        for (Iterator i = this.inBoth.iterator(); i.hasNext();)
        {
            MergePair pair = (MergePair) i.next();
            count += action.inBoth(this, pair);
        }
        return count;
    }

    /**
     * As a result of the merge, add an element to the output.
     * @param e the element to be added
     */
    public abstract void add(Element e);

    /**
     * As a result of the merge, remove an element from the output.
     * @param e the element to be removed
     */
    public abstract void remove(Element e);

}
