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

/**
 * Interface implemented by merger classes.
 *
 * @version $Id: $
 */
public interface MergeProcessor
{    
    /**
     * Add an item to be merged.
     *
     * @param mergeItem in the item to merge.
     * @throws MergeException on exceptions
     */
    void addMergeItem(Object mergeItem) throws MergeException;
    
    /**
     * Perform the merge.
     *
     * @return the merged artifact
     * @throws MergeException if there is a problem 
     */
    Object performMerge() throws MergeException;    
}
