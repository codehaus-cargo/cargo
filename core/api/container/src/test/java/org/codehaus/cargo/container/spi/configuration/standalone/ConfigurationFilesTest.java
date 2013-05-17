/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.spi.configuration.standalone;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * Unit tests for configuration files.
 * 
 * @version $Id$
 */
public class ConfigurationFilesTest extends TestCase
{

    /**
     * Configuration to test.
     */
    private TestableAbstractStandaloneConfiguration configuration;

    /**
     * Config file directory.
     */
    private File configFileDirectory;

    /**
     * Mock {@link AbstractStandaloneLocalConfiguration} implementation.
     */
    public class TestableAbstractStandaloneConfiguration extends
        AbstractStandaloneLocalConfiguration
    {
        /**
         * {@inheritDoc}
         * @param dir Configuration directory.
         */
        public TestableAbstractStandaloneConfiguration(String dir)
        {
            super(dir);
        }

        /**
         * {@inheritDoc}
         * @param container Local container.
         */
        @Override
        protected void doConfigure(LocalContainer container)
        {
            configureFiles(getFilterChain(), container);
        }

        /**
         * {@inheritDoc}
         * @return Mock {@link ConfigurationCapability}.
         */
        public ConfigurationCapability getCapability()
        {
            return new ConfigurationCapability()
            {
                /**
                 * {@inheritDoc}
                 * @return <code>false</code>.
                 */
                public boolean supportsProperty(String propertyName)
                {
                    return false;
                }

                /**
                 * {@inheritDoc}
                 * @return {@link Collections#emptyMap()}
                 */
                public Map<String, Boolean> getProperties()
                {
                    return Collections.emptyMap();
                }
            };
        }
    }

    /**
     * Creates the test directory. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        File testDir = File.createTempFile("cargo-config-file-test", null);
        // Delete and then recreate temporary directory since File doesn't support creating a
        // temporary directory
        testDir.delete();
        testDir.mkdir();
        testDir.deleteOnExit();

        File confHome = createDirectory(testDir, "home");
        configFileDirectory = createDirectory(testDir, "files");

        configuration = new TestableAbstractStandaloneConfiguration(getAbsolutePath(confHome));
    }

    /**
     * Sets all the files to delete on exit. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        // set all the files to delete on exit
        File home = new File(configuration.getHome());
        for (int i = 0; i < home.listFiles().length; i++)
        {
            File file = home.listFiles()[i];
            file.deleteOnExit();
        }

        this.configFileDirectory = null;
        this.configuration = null;
    }

    /**
     * Test file copy.
     * @param fileConfig File configuration.
     * @param expectedFilePath Expected result file path.
     * @param expectedFileContents Expected result file contents.
     * @throws Exception If anything goes wrong.
     */
    protected void testCopy(FileConfig fileConfig, String expectedFilePath,
        String expectedFileContents) throws Exception
    {
        configuration.setFileProperty(fileConfig);
        configuration.doConfigure(null);

        File copiedFile = new File(configuration.getHome() + "/" + expectedFilePath);
        assertTrue("Cannot find the expected copied file", copiedFile.exists());

        assertEquals(expectedFileContents, readFile(copiedFile));
    }

    /**
     * Test simple file copy.
     * @throws Exception If anything goes wrong.
     */
    public void testSimpleCopyFile() throws Exception
    {
        // simplest test for file copy
        String fileName = "simpleCopy1";
        String fileContents = "@foo@";
        File file1 = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file1));

        testCopy(configFile, fileName, "@foo@");
    }

    /**
     * Test file copy with replacement tokens.
     * @throws Exception If anything goes wrong.
     */
    public void testCopyWithTokens() throws Exception
    {
        // test that token replacement for "@foo@" results in "bar"
        String fileName = "simpleCopyWT1";
        String fileContents = "@token1@ ";
        File file1 = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file1));

        // set the property so that "@foo@" is replaced with "bar"
        configuration.setProperty("token1", "value1");
        // not set as a configuration file yet so token replacement shouldn't happen
        testCopy(configFile, fileName, "@token1@ ");

        configFile.setConfigfile("true");
        testCopy(configFile, fileName, "value1 ");
    }

    /**
     * Test file copy with overwrite.
     * @throws Exception If anything goes wrong.
     */
    public void testOverwite() throws Exception
    {
        File configHome = new File(configuration.getHome());
        assertTrue("Could not find a proper configuration home.",
            configHome.exists() && configHome.isDirectory());
        File existingFile = createFile(configHome, "existingfile", "helloworld");
        // make sure the file exists and contains the proper information.
        assertEquals("helloworld", readFile(existingFile));

        String fileName = "simpleCopyWithOverwrite";
        String fileContents = "goodbye";
        File file1 = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file1));
        configFile.setToFile("existingfile");
        configFile.setOverwrite("true");

        testCopy(configFile, "existingfile", "goodbye");
        assertEquals("goodbye", readFile(existingFile));
    }

    /**
     * Test file copy with overwrite and replacement tokens.
     * @throws Exception If anything goes wrong.
     */
    public void testOverwriteWithTokens() throws Exception
    {
        File configHome = new File(configuration.getHome());
        assertTrue("Could not find a proper configuration home.",
            configHome.exists() && configHome.isDirectory());
        File existingFile = createFile(configHome, "existingfile2", "helloworld");
        // make sure the file exists and contains the proper information.
        assertEquals("helloworld", readFile(existingFile));

        String fileName = "simpleCopyWithOverwrite";
        String fileContents = "goodbye@token1@.";
        File file1 = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file1));
        configFile.setToFile("existingfile2");
        configFile.setOverwrite("true");
        configFile.setConfigfile("true");

        configuration.setProperty("token1", " everyone");

        testCopy(configFile, "existingfile2", "goodbye everyone.");
        assertEquals("goodbye everyone.", readFile(existingFile));
    }

    /**
     * Test copy.
     * @throws Exception If anything goes wrong.
     */
    public void testCopy() throws Exception
    {
        String fileName = "file";
        String fileContents = "helloworld";
        File file1 = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file1));

        // test with a null tofile and todir
        testCopy(configFile, fileName, fileContents);

        // test setting file name
        configFile.setToFile("file1");
        testCopy(configFile, "file1", fileContents);

        // test setting file name to include a directory
        configFile.setToFile("dir1/file2");
        testCopy(configFile, "dir1/file2", fileContents);

        // test with null tofile but with a toDir
        configFile.setToFile(null);
        configFile.setToDir("dir2");
        testCopy(configFile, "dir2/file", fileContents);

        // test with both tofile and todir
        configFile.setToFile("file3");
        configFile.setToDir("dir3");
        testCopy(configFile, "dir3/file3", fileContents);

        configFile.setToFile("/dir4/file4");
        configFile.setToDir("/dir5");
        testCopy(configFile, "dir5/dir4/file4", fileContents);
    }

    /**
     * Test copy directory.
     * @throws Exception If anything goes wrong.
     */
    public void testCopyDirectory() throws Exception
    {
        String fileName1 = "file1";
        String fileName2 = "file2";
        String fileContents1 = "contents1";
        String fileContents2 = "contents2";
        createFile(configFileDirectory, fileName1, fileContents1);
        createFile(configFileDirectory, fileName2, fileContents2);

        // test that copying to the root directory works
        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(configFileDirectory));

        configuration.setFileProperty(configFile);
        configuration.doConfigure(null);

        File copiedFile1 = new File(configuration.getHome() + "/file1");
        assertTrue("Cannot find the expected copied file", copiedFile1.exists());
        assertEquals(fileContents1, readFile(copiedFile1));

        File copiedFile2 = new File(configuration.getHome() + "/file2");
        assertTrue("Cannot find the expected copied file", copiedFile2.exists());
        assertEquals(fileContents2, readFile(copiedFile2));

        // test that copying to a specified directory works
        configFile.setToDir("dir1");
        configuration.setFileProperty(configFile);
        configuration.doConfigure(null);

        copiedFile1 = new File(configuration.getHome() + "/dir1/file1");
        assertTrue("Cannot find the expected copied file", copiedFile1.exists());
        assertEquals(fileContents1, readFile(copiedFile1));

        copiedFile2 = new File(configuration.getHome() + "/dir1/file2");
        assertTrue("Cannot find the expected copied file", copiedFile2.exists());
        assertEquals(fileContents2, readFile(copiedFile2));
    }

    /**
     * Test configuration file property.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigFileProperty() throws Exception
    {
        File configHome = new File(configuration.getHome());
        assertTrue("Could not find a proper configuration home.",
            configHome.exists() && configHome.isDirectory());
        File existingFile = createFile(configHome, "existingfile", "helloworld");
        // make sure the file exists and contains the proper information.
        assertEquals("helloworld", readFile(existingFile));

        String fileName = "testConfigfile";
        String fileContents = "Hello @message@ ";
        File file = createFile(configFileDirectory, fileName, fileContents);

        FileConfig configFile = new FileConfig();
        configFile.setFile(getAbsolutePath(file));

        configFile.setToFile("existingfile");
        // if using the setConfigFilePropery then its should always be set
        // to overwrite and that its a configfile regardless of the what the
        // FileConfig is set as.
        configFile.setOverwrite(false);
        configFile.setConfigfile(false);

        configuration.setProperty("message", "world");
        configuration.setConfigFileProperty(configFile);
        configuration.doConfigure(null);

        File copiedFile = new File(configuration.getHome() + "/existingfile");
        assertTrue("Cannot find the expected copied file", copiedFile.exists());
        assertEquals("Hello world ", readFile(copiedFile));
    }

    /**
     * Create a directory.
     * @param directoryName Directory name.
     * @return Created directory.
     */
    protected File createDirecory(String directoryName)
    {
        return createDirectory(null, directoryName);
    }

    /**
     * Create a directory.
     * @param parent Parent directory.
     * @param directoryName Directory name.
     * @return Created directory.
     */
    protected File createDirectory(File parent, String directoryName)
    {
        File tempDirectory = new File(parent, directoryName);
        tempDirectory.mkdirs();
        tempDirectory.deleteOnExit();
        return tempDirectory;
    }

    /**
     * Create a file.
     * @param parent Parent directory.
     * @param filename File name.
     * @param fileContents File contents.
     * @return File created in <code>parent</code> with the given <code>filename</code> and
     * <code>fileContents</code>.
     * @throws Exception If anything goes wrong.
     */
    protected File createFile(File parent, String filename, String fileContents) throws Exception
    {
        File file = new File(parent, filename);
        file.deleteOnExit();

        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(fileContents.getBytes("UTF-8"));
        outputStream.close();

        return file;
    }

    /**
     * Read a file.
     * @param file Name of file to read.
     * @return Contents of <code>file</code>.
     */
    protected String readFile(File file)
    {
        return configuration.getFileHandler().readTextFile(getAbsolutePath(file), "UTF-8");
    }

    /**
     * Get the absolute path of a file.
     * @param file Name of file.
     * @return Absolute path of <code>file</code>.
     */
    protected String getAbsolutePath(File file)
    {
        String absolutePath = file.getAbsolutePath();
        absolutePath = absolutePath.replace('\\', '/');
        if (!absolutePath.startsWith("/"))
        {
            absolutePath = "/" + absolutePath;
        }
        return absolutePath;
    }
}
