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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.Identifier;

/**
 * A merging strategy that is determined by name.
 * 
 * @version $Id: $
 */
public class ChooseByNameMergeStrategy extends AbstractChoiceMergeStrategy
{
    /**
     * The default strategy to use.
     */
    private MergeStrategy defaultStrategy;

    /**
     * Map of named values to use.
     */
    private Map strategyMap;

    /**
     * Constructor.
     *
     * @param defaultStrategy in the default strategy to use
     */
    public ChooseByNameMergeStrategy(MergeStrategy defaultStrategy)
    {
        this.defaultStrategy = defaultStrategy;
        this.strategyMap = new HashMap();
    }

    /**
     * If the element has this name, then use this strategy.
     *
     * @param name in the name
     * @param strategy in the strategy
     */
    public void addStrategyForName(String name, MergeStrategy strategy)
    {
        this.strategyMap.put(name, strategy);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.module.merge.strategy.AbstractChoiceMergeStrategy#getApplicableStrategy(org.codehaus.cargo.module.merge.AbstractMergeSet,
     *      org.w3c.dom.Element)
     */
    MergeStrategy getApplicableStrategy(Descriptor set, DescriptorElement element)
    {
        Identifier id = element.getTag().getIdentifier();

        if (id != null)
        {

            String identityValue = id.getIdentifier(element);
            if (this.strategyMap.containsKey(identityValue))
            {
                return (MergeStrategy) this.strategyMap.get(identityValue);
            }
        }
        return this.defaultStrategy;
    }
}
