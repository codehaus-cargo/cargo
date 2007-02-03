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
package org.codehaus.cargo.module.merge.strategy;

import org.codehaus.cargo.module.merge.AbstractMergeSet;
import org.codehaus.cargo.module.merge.MergePair;
import org.w3c.dom.Element;

/**
 * A merge strategy is a vistor/stratey defining some way in which potential 
 * merge conflicts can be resolved in the set.
 * 
 * @version $Id $
 *
 */
public class MergeStrategy
{
    /**
     * Preserve strategy only adds items from the right.
     */
    public static final MergeStrategy PRESERVE = new MergeStrategy()
    {
        public int inRight(AbstractMergeSet set, Element element)
        {
            set.add(element);
            return 1;
        }
    };

    /**
     * Overwrite strategy adds items from the right, and overwrites duplicates.
     */
    public static final MergeStrategy OVERWRITE = new MergeStrategy()
    {
        public int inRight(AbstractMergeSet set, Element element)
        {
            set.add(element);
            return 1;
        }

        public int inBoth(AbstractMergeSet set, MergePair pair)
        {
            // Merge common items by overwriting them
            set.remove(pair.left);
            set.add(pair.right);
            return 1;
        }
    };

    /**
     * Deal with merging an element that only appears in the left set.
     *  
     * @param set in the calling MergeSet
     * @param element in the item present only in the left set
     * @return count of merged items
     */
    public int inLeft(AbstractMergeSet set, Element element)
    {
        return 0;
    }

    /**
     * Deal with merging an element that only appears in the right set.
     *
     * @param set in the calling MergeSet
     * @param element in the item present only in the left set
     * @return count of merged items
     */
    public int inRight(AbstractMergeSet set, Element element)
    {
        return 0;
    }

    /**
     * Deal with merging an element appears in both sets.
     *
     * @param set in the calling MergeSet
     * @param pair the pair of items
     * @return count of merged items
     */
    public int inBoth(AbstractMergeSet set, MergePair pair)
    {
        return 0;
    }

}
