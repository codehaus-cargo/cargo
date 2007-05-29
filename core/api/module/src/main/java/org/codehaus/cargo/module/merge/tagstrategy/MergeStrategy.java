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
 * @version $Id: $
 */
public interface MergeStrategy
{
    /**
     * Process when the element appears in both descriptors.
     * 
     * @param target output descriptor
     * @param left left element
     * @param right right element
     * @return count of merged elements
     */
    int inBoth(Descriptor target, DescriptorElement left, DescriptorElement right);

    /**
     * Process when the element appears just in the left descriptor.
     * 
     * @param target output descriptor
     * @param left left element
     * @return count of merged elements
     */
    int inLeft(Descriptor target, DescriptorElement left);

    /**
     * Process when the element appears just in the left descriptor.
     * 
     * @param target output descriptor
     * @param right  right element
     * @return count of merged elements
     */
    int inRight(Descriptor target, DescriptorElement right);
}
