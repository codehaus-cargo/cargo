/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
 * @version $Id$
 */
public class FileConfig 
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
     * If the file should be overwritten if the destination file exists.
     */
    private boolean overwrite;
    
    /**
     * If the file should be considered a configuration file.
     * If true then token replacement will occur on the file.
     */
    private boolean configfile;
    
    /**
     * Constructor.
     */
    public FileConfig()
    {
        this.file = null;
        this.tofile = null;
        this.overwrite = true;
        this.configfile = false;
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
     * Return true if the file should overwrite an existing file.
     * @return if the file should be overwritten
     */
    public boolean getOverwrite()
    {
        return overwrite;
    }
    
    /**
     * Returns true if the file is marked as a configuration file
     * @return If the file is a config file or not
     */
    public boolean getConfigfile()
    {
        return configfile;
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
    
    /**
     * Set if the destination file should be overwritten.
     * @param overwrite Set to true if files should overwrite
     */
    public void setOverwrite(String overwrite)
    {
        this.overwrite = Boolean.valueOf(overwrite).booleanValue();
    }
    
    /**
     * Set if the destination file should be overwritten.
     * @param overwrite Set to true if files should overwrite
     */
    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }
    
    /**
     * Set if the destination should be considered a configuration file
     * @param configfile Set to true if file is a configfile
     */
    public void setConfigfile(String configfile)
    {
        this.configfile = Boolean.valueOf(configfile).booleanValue(); 
    }
    
    /**
     * Set if the destination should be considered a configuration file
     * @param configfile Set to true if file is a configfile
     */
    public void setConfigfile(boolean configfile)
    {
        this.configfile = configfile;
    }
}
