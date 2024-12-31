/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.module.webapp.merge;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.webapp.WarArchive;

/**
 * Class to store the war file that is required for saving, together with any options such as lib
 * files for filtering.
 */
public class MergeWarFileDetails implements FileFilter
{
    /**
     * War Archive.
     */
    private WarArchive warArchive;

    /**
     * List of exclusions.
     */
    private List<String> exclusionPatterns = new ArrayList<String>();

    /**
     * Constructor.
     * @param warArchive War Archive to use
     */
    public MergeWarFileDetails(WarArchive warArchive)
    {
        this.warArchive = warArchive;
    }

    /**
     * @return War Archive
     */
    public WarArchive getWarFile()
    {
        return warArchive;
    }

    /**
     * Add a file or library exclusion pattern.
     * @param pattern Exclusion Pattern
     */
    public void addExclusionPattern(String pattern)
    {
        this.exclusionPatterns.add(pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(File pathname)
    {
        return true;
    }
}
