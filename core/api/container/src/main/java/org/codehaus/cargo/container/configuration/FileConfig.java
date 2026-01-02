/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.nio.charset.Charset;

/**
 * Contains data about configuration files that should be used for the container. This option only
 * works with standalone local containers.
 * <ul>
 * <li>The <code>toDir</code> and <code>toFile</code> are used to tell Cargo where to install the
 * file in reference to the container's <code>home</code> location.</li>
 * <li><code>toDir</code> and <code>toFile</code> can be used independently or together. If
 * <code>toDir</code> is <code>null</code> and <code>toFile</code> is <code>foo</code>, the
 * destination of the file will be <code>${cargo.home}/foo</code>.</li>
 * <li>If <code>toDir</code> is <code>foo</code> and the <code>toFile</code> is <code>null</code>,
 * the destination of the new file will be
 * <code>${cargo.home}/foo/<i>original file's name</i>}</code>.</li>
 * <li>If <code>toDir</code> is <code>foo</code> and <code>toFile</code> is <code>bar</code>, the
 * destination of the new file will be <code>${cargo.home}/foo/bar</code>.</li>
 * </ul>
 */
public class FileConfig
{

    /**
     * The file to be used.
     */
    private String file;

    /**
     * The name and location of where the file should be saved in reference to the containers home.
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
     * If the file should be considered a configuration file. If true then token replacement will
     * occur on the file.
     */
    private boolean configfile;

    /**
     * The character encoding to use when token filtering is performed.
     */
    private String encoding;

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
     * @param file The file to use
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * Sets the destination file name.
     * 
     * @param tofile The destination file name
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
        this.overwrite = Boolean.parseBoolean(overwrite);
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
        this.configfile = Boolean.parseBoolean(configfile);
    }

    /**
     * Set if the destination should be considered a configuration file
     * @param configfile Set to true if file is a configfile
     */
    public void setConfigfile(boolean configfile)
    {
        this.configfile = configfile;
    }

    /**
     * Gets the character encoding to use when token filtering is performed.
     * 
     * @return The character encoding to use when token filtering is performed or {@code null}/empty
     *         if the platform's default encoding should be used.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * Gets the character encoding to use when token filtering is performed, as a Charset.
     * 
     * @return The character encoding to use when token filtering is performed or {@code null}/empty
     *         if the platform's default encoding should be used.
     */
    public Charset getEncodingAsCharset()
    {
        if (encoding == null)
        {
            return null;
        }
        else
        {
            return Charset.forName(encoding);
        }
    }

    /**
     * Sets the character encoding to use when token filtering is performed.
     * 
     * @param encoding The character encoding to use when token filtering is performed, may be
     *            {@code null} or empty to use the platform's default encoding.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

}
