/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import java.io.FileOutputStream;
import java.io.InputStream;

import org.codehaus.cargo.module.JarArchive;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeProcessor;

/**
 * Class to store merging operations.
 */
public class ArchiveResourceMerger
{
    /**
     * Merge processor itself.
     */
    private MergeProcessor next;

    /**
     * The resource being merged.
     */
    private String resource;

    /**
     * Constructor.
     * 
     * @param resource name of the resource path
     * @param next the processor
     */
    public ArchiveResourceMerger(String resource, MergeProcessor next)
    {
        this.next = next;
        this.resource = resource;
    }

    /**
     * Add an item into the merge.
     * 
     * @param mergeItem the item
     * @throws MergeException if a problem
     */
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        try
        {
            JarArchive jar = (JarArchive) mergeItem;

            InputStream is = jar.getResource(this.resource);
            if (is != null)
            {
                this.next.addMergeItem(is);
            }
            // If it isn't present, then it doesn't matter, it won't
            // get merged.
        }
        catch (Exception ex)
        {
            throw new MergeException("Problem when fetching merge item from War Archive",
                ex);
        }

    }

    /**
     * Execute the merge into the assembly directory.
     * 
     * @param assembleDir the directory to save to
     * @throws MergeException if a problem
     */
    public void execute(File assembleDir) throws MergeException
    {
        try (InputStream is = (InputStream) this.next.performMerge())
        {
            // If we get back nothing, then none of the source files
            // had this resource to do the merge.
            if (is == null)
            {
                return;
            }

            File outputFile = new File(assembleDir.getAbsolutePath() + File.separator
                + this.resource);

            // Make sure the directory actually exists
            outputFile.getParentFile().mkdir();

            try (FileOutputStream fos = new FileOutputStream(outputFile))
            {
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) > 0)
                {
                    fos.write(buffer, 0, count);
                }
            }
        }
        catch (Exception e)
        {
            throw new MergeException("Problem executing merge", e);
        }
    }
}
