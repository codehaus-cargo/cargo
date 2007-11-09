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
package org.codehaus.cargo.module.webapp.merge;

import java.io.File;
import java.io.IOException;

import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.util.CargoException;
import org.jdom.JDOMException;

/**
 * Class for merging two War Archives into each other.
 * 
 * @version $Id: $
 */
public class WarArchiveMerger implements MergeProcessor
{

    /**
     * The result that we are building up.
     */
    private MergedWarArchive result;

    /**
     * Constructor Class for merging War archives together.
     */
    public WarArchiveMerger()
    {
        this.result = new MergedWarArchive();
    }

    /**
     * Add a merging processor that merges specified items witin the war files.
     * 
     * @param path in the path to merge to
     * @param merger in the merger
     */
    public void addMergeProcessor(String path, MergeProcessor merger)
    {
        if (path == null || merger == null)
        {
            throw new NullPointerException();
        }
        this.result.addProcessor(path, merger);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.module.merge.MergeProcessor#addMergeItem(java.lang.Object)
     */
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (mergeItem instanceof WarArchive)
        {
          this.result.add((WarArchive) mergeItem);         
        }
        else if(mergeItem instanceof File)
        {          
          this.result.addJar((File)mergeItem);         
        }
        else
        {        
          throw new MergeException(
            "WarArchiveMerger cannot merge things that are not WarArchives or files");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.module.merge.MergeProcessor#performMerge()
     */
    public Object performMerge()
    {
        WarArchive merge = this.result;
        this.result = new MergedWarArchive();
        return merge;
    }

    /**
     * Perform the archive merge, using the specified file as the output destination.
     * 
     * @param targetFile The target file to output to.
     * @throws JDOMException 
     * @throws IOException 
     */
    public void performMerge(File targetFile) throws IOException, JDOMException
    {
    	WarArchive output = (WarArchive)performMerge();
    	output.store(targetFile);    
    }
            
    /**
     * Control whether the merge should include JAR files.
     * 
     * @param doMergeJarFiles true if we do (default)
     */
    public void setMergeJarFiles(boolean doMergeJarFiles)
    {
    	this.result.mergeJarFiles(doMergeJarFiles);
    }
    
    /**
     * Get the class used for web xml merging.
     * 
     * @return the Web XML Merger
     */
    public WebXmlMerger getWebXmlMerger()
    {
        try
        {
            return this.result.getWebXmlMerger();
        }
        catch (Exception ex)
        {
            throw new CargoException("Exception getting web xml merger", ex);
        }
    }
}
