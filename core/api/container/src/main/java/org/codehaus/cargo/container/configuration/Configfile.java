/* 
 * ========================================================================
 * 
 * Copyright 2008 Vincent Massol.
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

package org.codehaus.cargo.container.configuration;

/**
 * Contains data about configuration files that should be used for the container.
 * This option only works with StandaloneLocal containers.
 * 
 * The toDir and toFile are used to tell Cargo where to install the file in reference to the
 * containers home location.
 * 
 * toDir and toFile can be used independently or together.
 * If toDir is undefined/null and toFile is foo, the destination of the file
 * will be ${cargo.home}/foo
 * 
 * If toDir is foo and the toFile is undefined/null, the destination of the
 * new file will be ${cargo.home}/foo/${original file's name}
 * 
 * If toDir is foo and toFile is bar, the destination of the new file will
 * be ${cargo.home}/foo/bar
 * 
 * @version $Id:$
 */
public class Configfile 
{

   /**
     * The file to be used.
     */
    private String file;

    /**
     * The name and location of where the file should be saved in reference to
     * the containers home.
     */
    private String tofile;

    /**
     * The name of the directory where the file should be saved.
     */
    private String todir;

    /**
     * Constructor.
     */
    public Configfile()
    {
        this.file = null;
        this.tofile = null;
    }

    /**
     * Returns the name of the file to be used.
     * 
     * @return The file to be used
     */
    public String getFile()
    {
        return file;
    }

    /**
     * Returns the name of the destination file.
     * 
     * @return The name of the destination file
     */
    public String getToFile()
    {
        return tofile;
    }

    /**
     * Returns the name of the destination directory.
     * 
     * @return The destination directory
     */
    public String getToDir()
    {
        return todir;
    }

    /**
     * Sets the file to be used.
     * 
     * @param file
     *            The file to use
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * Sets the destination file name.
     * 
     * @param tofile
     *            The destination file name
     */
    public void setToFile(String tofile)
    {
        this.tofile = tofile;
    }

    /**
     * Sets the destination directory to use.
     * @param todir The destination directory
     */
    public void setToDir(String todir)
    {
        this.todir = todir;
    }
    
}
