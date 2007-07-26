/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.resin;

import org.codehaus.cargo.module.XmlMerger;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;


/**
 * Class to manage the merging of two resin web descriptors.
 * 
 * @version $Id $
 */
public class ResinWebXmlMerger extends XmlMerger
{     
    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private ResinWebXml webXml;      
    
    /**
     * Constructor.
     * 
     * @param theWebXml The original descriptor
     */
    public ResinWebXmlMerger(ResinWebXml theWebXml)
    {
        if (theWebXml == null)
        {
            throw new IllegalArgumentException("Must pass a resin web xml");
        }
        this.webXml = theWebXml;
        
        DescriptorMergerByTag dmt = new DescriptorMergerByTag();

        // Default behaviours
        dmt.setDefaultStrategyIfNoneSpecified(DescriptorMergerByTag.OVERWRITE);
        
        addMerger(dmt);
    }

    /**
     * Merges the merge descriptor with the original descriptor.
     * 
     * @param theMergeWebXml The descriptor to merge in
     */
    public final void merge(ResinWebXml theMergeWebXml)
    {
        if (theMergeWebXml == null)
        {
            throw new IllegalArgumentException("Must pass a resin web xml");
        }

        super.merge(theMergeWebXml);
    }


}
