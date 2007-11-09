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
package org.codehaus.cargo.module.webapp.merge;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.FileUtils;
import org.codehaus.cargo.util.JarUtils;
import org.jdom.JDOMException;

/**
 * Subclass representing the merged WAR file.
 * @version $Id: $
 */
public class MergedWarArchive implements WarArchive
{    
    /**
     * War files making up this merged war, or type MergeWarFileDetails.
     */
    private List warFiles;

    /**
     * Extra JAR files to appear in WEB-INF/lib.
     */
    private List jarFiles;
    
    /**
     * Whether the JAR files contained WEB-INF/lib should be merged.
     */
    private boolean mergeJarFiles = true;
    
    /**
     * The merged web xml, once generated.
     */
    private WebXml mergedWebXml;

    /**
     * Additional processors to apply.
     */
    private List mergeProcessors;

    /**
     * The Web XML Merger class.
     */
    private WebXmlMerger webXmlMerger;

    /**
     * Constructor.
     */
    MergedWarArchive()
    {
        this.warFiles = new ArrayList();
        this.jarFiles = new ArrayList();
        this.mergeProcessors = new ArrayList();
    }

    /**
     * @return the first war file in the merge list
     */
    protected WarArchive firstWarFile()
    {
        return ((MergeWarFileDetails) this.warFiles.get(0)).getWarFile();
    }

    /**
     * @param path in the path to merge to
     * @param merger in the processor to add
     */
    public void addProcessor(String path, MergeProcessor merger)
    {
        this.mergeProcessors.add(new ArchiveResourceMerger(path, merger));
    }

    /**
     * @param warFile in a warfile to add to the merge
     */
    void add(WarArchive warFile)
    {
        this.warFiles.add(new MergeWarFileDetails(warFile));
    }

    /**
     * @param jarFile is a jar file to add to the merge.
     */
    void addJar(File jarFile)
    {
         this.jarFiles.add(jarFile);
    }
    
    /**
     * Get the web XML merger.
     *
     * @return the WebXml merger
     * @throws IOException on an IO Exception
     * @throws JDOMException on a XML Parse Exception
     */
    public WebXmlMerger getWebXmlMerger() throws IOException, 
        JDOMException
    {
        if (this.webXmlMerger == null)
        {
            this.webXmlMerger = new WebXmlMerger(firstWarFile().getWebXml());
        }

        return this.webXmlMerger;
    }

    /**
     * {@inheritDoc}
     * @throws JDOMException 
     * @see org.codehaus.cargo.module.webapp.WarArchive#getWebXml()
     */
    public WebXml getWebXml() throws IOException, 
        JDOMException
    {
        if (this.mergedWebXml == null)
        {
            WebXmlMerger wxm = getWebXmlMerger();

            for (int i = 1; i < this.warFiles.size(); i++)
            {
                MergeWarFileDetails details = (MergeWarFileDetails) this.warFiles.get(i);
                WarArchive wa = details.getWarFile();
                wxm.merge(wa.getWebXml());
            }
            this.mergedWebXml = firstWarFile().getWebXml();
        }
        return this.mergedWebXml;
    }

    /**
     * @param assembleDir in the directory to output the merge data to
     * @throws MergeException when there is a problem
     * @throws IOException if an IO exception
     */
    protected void executeMergeProcessors(File assembleDir) throws MergeException, IOException
    {

        for (Iterator i = this.mergeProcessors.iterator(); i.hasNext();)
        {
            ArchiveResourceMerger processor = (ArchiveResourceMerger) i.next();

            for (int j = 0; j < this.warFiles.size(); j++)
            {
                MergeWarFileDetails details = (MergeWarFileDetails) this.warFiles.get(j);
                WarArchive wa = details.getWarFile();

                processor.addMergeItem(wa);
            }

            processor.execute(assembleDir);
        }

    }

    /**
     * The actual merging takes place on the store() method, which writes combined
     * archive out into the new location.
     * {@inheritDoc}
     * @throws JDOMException 
     * @see org.codehaus.cargo.module.webapp.WarArchive#store(java.io.File)
     */
    public void store(File warFile) throws MergeException, IOException, 
        JDOMException
    {
        DefaultFileHandler fileHandler = new DefaultFileHandler();

        // 1: Merge together the web XML items
        WebXml mergedWebXml = getWebXml();

        // 2. Expand everything in order somewhere temporary
        String assembleDir = fileHandler.createUniqueTmpDirectory();

        expandToPath(assembleDir);

        if( !mergeJarFiles )
        {
        	File f = new File(assembleDir);
            File webInfLib = new File(f, "WEB-INF/lib");
            File[] files = webInfLib.listFiles();
            for(int i=0; i<files.length;i++)
            {
            	if( !files[i].isDirectory() && files[i].getName().toLowerCase().endsWith(".jar") )
            	{
            		fileHandler.delete( files[i].getAbsolutePath() );
            	}
            }
        }
        
        copyJars(assembleDir);
        
        // (over)write the web-inf configs
        WebXmlIo.writeAll(mergedWebXml, fileHandler.append(new File(assembleDir)
            .getAbsolutePath(), File.separator + "WEB-INF"));

        executeMergeProcessors(new File(assembleDir));

        JarUtils jarUtils = new JarUtils();

        // Create a jar file
        jarUtils.createJarFromDirectory(assembleDir, warFile);

        // Delete temp directory.
        fileHandler.delete(assembleDir);
    }

    /**
     * @param assembleDir directory to copy JAR files to
     */
    private void copyJars(String assembleDir)
    {
      FileHandler fileHandler = new DefaultFileHandler();
      
      File f = new File(assembleDir);
      File webInfLib = new File(f, "WEB-INF/lib");
      webInfLib.mkdirs();
      for (Iterator i = this.jarFiles.iterator(); i.hasNext();)
      {
        File sourceFile = (File)i.next();
        fileHandler.copyFile(sourceFile.getAbsolutePath(), new File(webInfLib, sourceFile.getName()).getAbsolutePath());        
      }      
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#containsClass(java.lang.String)
     */
    public boolean containsClass(String theClassName) throws IOException
    {
        for (Iterator i = this.warFiles.iterator(); i.hasNext();)
        {
            MergeWarFileDetails details = (MergeWarFileDetails) i.next();
            WarArchive wa = details.getWarFile();

            if (wa.containsClass(theClassName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#findResource(java.lang.String)
     */
    public String findResource(String theName) throws IOException
    {
        for (Iterator i = this.warFiles.iterator(); i.hasNext();)
        {
            MergeWarFileDetails details = (MergeWarFileDetails) i.next();
            WarArchive wa = details.getWarFile();

            String res = wa.findResource(theName);
            if (res != null)
            {
                return res;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#getResource(java.lang.String)
     */
    public InputStream getResource(String thePath) throws IOException
    {
        for (Iterator i = this.warFiles.iterator(); i.hasNext();)
        {
            MergeWarFileDetails details = (MergeWarFileDetails) i.next();
            WarArchive wa = details.getWarFile();
            InputStream is = wa.getResource(thePath);
            if (is != null)
            {
                return is;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#getResources(java.lang.String)
     */
    public List getResources(String thePath) throws IOException
    {
        List results = new ArrayList();
        for (Iterator i = this.warFiles.iterator(); i.hasNext();)
        {
            MergeWarFileDetails details = (MergeWarFileDetails) i.next();
            WarArchive wa = details.getWarFile();
            results.addAll(wa.getResources(thePath));
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#expandToPath(String)
     */
    public void expandToPath(String path) throws IOException
    {
        expandToPath(path, null);
    }

    /** 
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.JarArchive#expandToPath(java.lang.String, java.io.FileFilter)
     */
    public void expandToPath(String path, FileFilter filter) throws IOException
    {
        for (int i = 0; i < this.warFiles.size(); i++)
        {
            MergeWarFileDetails details = (MergeWarFileDetails) this.warFiles.get(i);
            WarArchive wa = details.getWarFile();

            wa.expandToPath(path, filter);
        }
    }

    /**
     * Control whether to also merge the JAR files.
     * 
     * @param mergeJarFiles true if we do (default)
     */
    public void mergeJarFiles(boolean mergeJarFiles) {
		this.mergeJarFiles = mergeJarFiles;
	}
}
