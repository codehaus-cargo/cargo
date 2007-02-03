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
package org.codehaus.cargo.module.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.cargo.module.JarArchive;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.util.JarUtils;
import org.codehaus.cargo.util.DefaultFileHandler;

import org.xml.sax.SAXException;

/**
 * Class for merging two War Archives into each other.
 *
 * @version $Id: $
 */
public class WarArchiveMerger implements MergeProcessor
{
    /**
     * Subclass representing the merged WAR file.
     */
    public class MergedWarArchive implements WarArchive
    {
        /**
         * Class to store merging operations.
         *
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
                InputStream is = (InputStream) this.next.performMerge();

                // If we get back nothing, then none of the source files
                // had this resource to do the merge.

                if (is == null)
                {
                    return;
                }

                File outputFile = new File(assembleDir.getAbsolutePath() + File.separator
                    + this.resource);
                FileOutputStream fos = null;

                try
                {
                    fos = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];

                    int count;

                    while ((count = is.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, count);
                    }
                }
                catch (Exception e)
                {
                    throw new MergeException("Problem executing merge", e);
                }

                finally
                {
                    try
                    {
                        if (is != null)
                        {
                            is.close();
                        }
                        if (fos != null)
                        {
                            fos.close();
                        }
                    }
                    catch (Exception e)
                    {
                        throw new MergeException("Problem when closing files used in merge", e);
                    }

                }

            }
        }

        /**
         * War files making up this merged war.
         */
        private List warFiles;

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
            this.mergeProcessors = new ArrayList();
        }

        /**
         * @return the first war file in the merge list
         */
        protected DefaultWarArchive firstWarFile()
        {
            return (DefaultWarArchive) this.warFiles.get(0);
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
        void add(DefaultWarArchive warFile)
        {
            this.warFiles.add(warFile);
        }

        /**
         * Get the web XML merger.
         *
         * @return the WebXml merger
         * @throws IOException on an IO Exception
         * @throws SAXException on a SAX Parse Exception
         * @throws ParserConfigurationException on Parser config exception
         */
        public WebXmlMerger getWebXmlMerger() throws IOException, SAXException,
            ParserConfigurationException
        {
            if (this.webXmlMerger == null)
            {
                this.webXmlMerger = new WebXmlMerger(firstWarFile().getWebXml());
            }

            return this.webXmlMerger;
        }

        /**
         * {@inheritDoc}
         * @see org.codehaus.cargo.module.webapp.WarArchive#getWebXml()
         */
        public WebXml getWebXml() throws IOException, SAXException, ParserConfigurationException
        {
            if (this.mergedWebXml == null)
            {
                WebXmlMerger wxm = getWebXmlMerger();

                for (int i = 1; i < this.warFiles.size(); i++)
                {
                    WarArchive wa = (WarArchive) this.warFiles.get(i);
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
        protected void executeMergeProcessors(File assembleDir) throws MergeException,
            IOException
        {

            for (Iterator i = this.mergeProcessors.iterator(); i.hasNext();)
            {
                ArchiveResourceMerger processor = (ArchiveResourceMerger) i.next();

                for (int j = 0; j < this.warFiles.size(); j++)
                {
                    WarArchive wa = (WarArchive) this.warFiles.get(j);

                    processor.addMergeItem(wa);
                }

                processor.execute(assembleDir);
            }

        }

        /**
         * {@inheritDoc}
         * @see org.codehaus.cargo.module.webapp.WarArchive#store(java.io.File)
         */
        public void store(File warFile) throws MergeException, IOException, SAXException,
            ParserConfigurationException
        {
            DefaultFileHandler fileHandler = new DefaultFileHandler();

            // 1: Merge together the web XML items
            WebXml mergedWebXml = getWebXml();

            // 2. Expand everything in order somewhere temporary
            String assembleDir = fileHandler.createUniqueTmpDirectory();

            expandToPath(assembleDir);

            // (over)write the web-inf configs
            WebXmlIo.writeAll(mergedWebXml, fileHandler.append(
                new File(assembleDir).getAbsolutePath(), File.separator + "WEB-INF"));

            executeMergeProcessors(new File(assembleDir));

            JarUtils jarUtils = new JarUtils();

            // Create a jar file
            jarUtils.createJarFromDirectory(assembleDir, warFile);

            // Delete temp directory.
            fileHandler.delete(assembleDir);
        }

        /**
         * {@inheritDoc}
         * @see org.codehaus.cargo.module.JarArchive#containsClass(java.lang.String)
         */
        public boolean containsClass(String theClassName) throws IOException
        {
            for (Iterator i = this.warFiles.iterator(); i.hasNext();)
            {
                WarArchive wa = (WarArchive) i.next();
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
                WarArchive wa = (WarArchive) i.next();
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
                WarArchive wa = (WarArchive) i.next();
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
                WarArchive wa = (WarArchive) i.next();
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
            for (int i = 0; i < this.warFiles.size(); i++)
            {
                WarArchive wa = (WarArchive) this.warFiles.get(i);
                wa.expandToPath(path);
            }
        }
    }

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
        this.result.addProcessor(path, merger);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#addMergeItem(java.lang.Object)
     */
    public void addMergeItem(Object mergeItem) throws MergeException
    {
        if (!(mergeItem instanceof DefaultWarArchive))
        {
            throw new MergeException(
                "WarArchiveMerger cannot merge things that are not WarArchives");
        }
        this.result.add((DefaultWarArchive) mergeItem);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.MergeProcessor#performMerge()
     */
    public Object performMerge()
    {
        WarArchive merge = this.result;
        this.result = new MergedWarArchive();
        return merge;
    }

    /**
     * Get the class used for web xml merging.
     * 
     * @return the Web XML Merger
     * @throws IOException on an IO Exception
     * @throws SAXException on a SAX Parse Exception
     * @throws ParserConfigurationException on Parser config exception
     */
    public WebXmlMerger getWebXmlMerger() throws IOException, SAXException,
        ParserConfigurationException
    {
        return this.result.getWebXmlMerger();
    }
}
