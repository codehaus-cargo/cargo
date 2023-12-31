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
package org.codehaus.cargo.maven3.merge;

import org.codehaus.cargo.maven3.Merge;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.webapp.merge.WarArchiveMerger;

/**
 * Factory for creating merge processors.
 */
public interface MergeProcessorFactory
{
    /**
     * Create a merge processor.
     * @param wam WAR archive merger.
     * @param xml XML merge.
     * @return Merge processor.
     */
    MergeProcessor create(WarArchiveMerger wam, Merge xml);
}
