/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.module;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.merge.DescriptorMerger;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Class for merging XML FIles.
 */
public class XmlMerger extends LoggedObject implements DescriptorMerger
{
    /**
     * List of mergers that will be applied to the xml file.
     */
    private List<DescriptorMerger> descriptorMergers = new ArrayList<DescriptorMerger>();

    /**
     * @param merger merge class to add
     */
    public void addMerger(DescriptorMerger merger)
    {
        this.descriptorMergers.add(merger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Descriptor base)
    {
        for (DescriptorMerger merger : descriptorMergers)
        {
            merger.init(base);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(Descriptor other)
    {
        for (DescriptorMerger merger : descriptorMergers)
        {
            merger.merge(other);
        }
    }

    /**
     * @return list of merge classes
     */
    public List<DescriptorMerger> getMergers()
    {
        return this.descriptorMergers;
    }

}
