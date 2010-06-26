/* 
 * ========================================================================
 * 
 * Copyright 2009 Vincent Massol.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.spi.configuration.StandaloneConfigurationTest.TestableAbstractStandaloneConfiguration;
import org.codehaus.cargo.util.VFSFileHandler;

import junit.framework.TestCase;

public class ConfigurationFilesTest extends TestCase 
{

	TestableAbstractStandaloneConfiguration configuration;
	File configFileDirectory;
	
	
	public class TestableAbstractStandaloneConfiguration extends
			AbstractStandaloneLocalConfiguration {
		public TestableAbstractStandaloneConfiguration(String dir) {
			super(dir);
		}

		@Override
        protected void doConfigure(LocalContainer container) {
			configureFiles(getFilterChain());
		}

		public ConfigurationCapability getCapability() {
			return new ConfigurationCapability() {
				public boolean supportsProperty(String propertyName) {
					return false;
				}

				public Map getProperties() {
					return new HashMap();
				}
			};
		}
	}


	@Override
    protected void setUp() throws Exception
	{
		File testDir = File.createTempFile("cargo-config-file-test", null);
		// Delete and then recreate temporary directory since File doesn't support creating a temporary directory
		testDir.delete();
		testDir.mkdir();
		testDir.deleteOnExit();
		
		File confHome = createDirectory(testDir, "home");
		configFileDirectory = createDirectory (testDir, "files");
		
		configuration = new TestableAbstractStandaloneConfiguration(getAbsolutePath(confHome));
	}
	
	@Override
    protected void tearDown() throws Exception {
		// set all the files to delete on exit
		File home = new File(configuration.getHome());
		for (int i = 0 ; i < home.listFiles().length; i ++)
		{
			File file = home.listFiles()[i];
			file.deleteOnExit();
		}
		
		this.configFileDirectory = null;
		this.configuration = null;
	}
	
	public void testCopy(FileConfig fileConfig, String expectedFilePath, String expectedFileContents) throws Exception
	{
		configuration.setFileProperty(fileConfig);
		configuration.doConfigure(null);
		
		File copiedFile = new File(configuration.getHome() + "/" + expectedFilePath);
		assertTrue("Cannot find the expected copied file", copiedFile.exists());
		
		assertEquals(expectedFileContents, readFile(copiedFile));
	}
	
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

	public void testCopyWithTokens() throws Exception
	{	
		//test that token replacement for "@foo@" results in "bar"
		String fileName = "simpleCopyWT1";
		String fileContents = "@token1@ ";
		File file1 = createFile(configFileDirectory, fileName, fileContents);
		
		FileConfig configFile = new FileConfig();
		configFile.setFile(getAbsolutePath(file1));
		
		//set the property so that "@foo@" is replaced with "bar"
		configuration.setProperty("token1", "value1");
		//not set as a configuration file yet so token replacement shouldn't happen
		testCopy(configFile, fileName, "@token1@ ");
		
		configFile.setConfigfile("true");
		testCopy(configFile, fileName, "value1 ");
	}
	
	public void testOverwite() throws Exception
	{
		File configHome = new File(configuration.getHome());
		assertTrue("Could not find a proper configuration home.",configHome.exists() && configHome.isDirectory());
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
	
	public void testOverwriteWithTokens() throws Exception
	{
		File configHome = new File(configuration.getHome());
		assertTrue("Could not find a proper configuration home.",configHome.exists() && configHome.isDirectory());
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
	
	
	public void testCopyDirectory() throws Exception
	{
		String fileName1 = "file1";
		String fileName2 = "file2";
		String fileContents1 = "contents1";
		String fileContents2 = "contents2";
		File file1 = createFile(configFileDirectory, fileName1, fileContents1);
		File file2 = createFile(configFileDirectory, fileName2, fileContents2);
	
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

	public void testConfigFileProperty() throws Exception
	{
		File configHome = new File(configuration.getHome());
		assertTrue("Could not find a proper configuration home.",configHome.exists() && configHome.isDirectory());
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

	
	protected File createDirecory(String directoryName)
	{
		return createDirectory(null, directoryName);
	}
	
	protected File createDirectory(File parent, String directoryName)
	{
		File tempDirectory = new File(parent, directoryName);
		tempDirectory.mkdirs();
		tempDirectory.deleteOnExit();
		return tempDirectory;
	}
	
	protected File createFile(File parent, String filename, String fileContents) throws Exception
	{
		File file = new File(parent, filename);
		file.deleteOnExit();
		
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(fileContents.getBytes());
		outputStream.close();
		
		return file;
	}
	
	protected String readFile(File file)
	{
		return configuration.getFileHandler().readTextFile(getAbsolutePath(file));
	}
	
	protected String getAbsolutePath(File file)
	{
		String absolutePath = file.getAbsolutePath();
		absolutePath = absolutePath.replace('\\', '/');
		if (!absolutePath.startsWith("/")) {
			absolutePath = "/" + absolutePath;
		}
		return absolutePath;
	}
}
