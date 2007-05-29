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

package org.codehaus.cargo.module.merge.tagstrategy;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorElement;

/**
 * A merging strategy that can make a decision between various differing merge strategies,
 * depending upon the state that it is passed.
 * 
 * Child classes implement the getApplicableStrategy based on whatever information is neccessary to
 * make that decision
 *
 * @version $Id: $
 */
public abstract class AbstractChoiceMergeStrategy implements MergeStrategy
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.strategy.MergeStrategy#inLeft(org.codehaus.cargo.module.merge.AbstractMergeSet, org.w3c.dom.Element)
     */
    public int inLeft(Descriptor set, DescriptorElement element)
    {
        return getApplicableStrategy(set, element).inLeft(set, element);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.strategy.MergeStrategy#inRight(org.codehaus.cargo.module.merge.AbstractMergeSet, org.w3c.dom.Element)
     */
    public int inRight(Descriptor set, DescriptorElement element)
    {
        return getApplicableStrategy(set, element).inRight(set, element);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.strategy.MergeStrategy#inBoth(org.codehaus.cargo.module.merge.AbstractMergeSet, org.codehaus.cargo.module.merge.MergePair)
     */
    public int inBoth(Descriptor set, DescriptorElement left, DescriptorElement right)
    {
        return getApplicableStrategy(set, left).inBoth(set, left, right);
    }

    /**
     * Get an appropriate merge strategy given the passed parameters.
     * 
     * @param set the parent set
     * @param element the element under consideration
     * @return the applicable merge strategy
     */
    abstract MergeStrategy getApplicableStrategy(Descriptor set, DescriptorElement element);
}
