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
package org.codehaus.cargo.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.util.FileUtils;

/**
 * File operations that are performed in Cargo. All file operations must use this class.
 * 
 * @version $Id$
 */
public class DefaultFileHandler implements FileHandler
{
    /**
     * Counter for creating unique temp directories.
     */
    private static int uniqueNameCounter = -1;

    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * Ant helper API to manipulate files.
     */
    private FileUtils fileUtils;

    /**
     * Initializations.
     */
    public DefaultFileHandler()
    {
        this.antUtils = new AntUtils();
        this.fileUtils = FileUtils.newFileUtils();
    }

    /**
     * @return the Ant utility class
     */
    private AntUtils getAntUtils()
    {
        return this.antUtils;
    }

    /**
     * @return the File utility class
     */
    private FileUtils getFileUtils()
    {
        return this.fileUtils;
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#copyFile(String, String)
     */
    public void copyFile(String source, String target)
    {
        copyFile(source, target, false);
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#copyFile(String, String, boolean)
     */
    public void copyFile(String source, String target, boolean overwrite)
    {
        try
        {
            getFileUtils().copyFile(new File(source).getAbsolutePath(),
                new File(target).getAbsolutePath(), null, overwrite);
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to copy source file [" + source + "] to ["
                + target + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see FileHander#copyFile(String, String, FilterChain)
     */
    public void copyFile(String source, String target, FilterChain filterChain)
    {
        copyFile(source, target, filterChain, null);
    }

    /**
     * {@inheritDoc}
     * @see FileHander#copyFile(String, String, FilterChain, String)
     */
    public void copyFile(String source, String target, FilterChain filterChain, String encoding)
    {
        try
        {
            InputStream fileIS = new FileInputStream(source);

            BufferedReader in = null;
            BufferedWriter out = null;
            try
            {
                ChainReaderHelper helper = new ChainReaderHelper();
                helper.setBufferSize(8192);
                helper.setPrimaryReader(new BufferedReader(newReader(fileIS, encoding)));
                Vector<FilterChain> filterChains = new Vector<FilterChain>();
                filterChains.add(filterChain);
                helper.setFilterChains(filterChains);
                in = new BufferedReader(helper.getAssembledReader());

                out = new BufferedWriter(newWriter(target, encoding));

                String line;
                while ((line = in.readLine()) != null)
                {
                    if (line.length() == 0)
                    {
                        out.newLine();
                    }
                    else
                    {
                        out.write(line);
                        out.newLine();
                    }
                }
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to copy source file [" + source + "] to [" + target
                    + "] with FilterChain", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see FileHandler#copyDirectory(String, String)
     */
    public void copyDirectory(String source, String target)
    {
        copyDirectory(source, target, new ArrayList<String>());
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#copyDirectory(String, String)
     */
    public void copyDirectory(String source, String target, List<String> excludes)
    {
        try
        {
            Copy copyTask = (Copy) getAntUtils().createAntTask("copy");
            copyTask.setTodir(new File(target));

            FileSet fileSet = new FileSet();
            fileSet.setDir(new File(source));

            // Exclude files marked by the user to be excluded
            for (String excludeName : excludes)
            {
                fileSet.createExclude().setName(excludeName);
            }

            copyTask.addFileset(fileSet);
            copyTask.setFailOnError(true);
            copyTask.setIncludeEmptyDirs(true);
            copyTask.setOverwrite(true);

            copyTask.execute();
        }
        catch (BuildException e)
        {
            throw new CargoException("Failed to copy source directory [" + source + "] to ["
                + target + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#createDirectory(String, String)
     */
    public String createDirectory(String parentDir, String name)
    {
        File dir = new File(parentDir, name);
        mkdirs(dir.getAbsolutePath());
        if (!dir.isDirectory() || !dir.exists())
        {
            throw new CargoException("Couldn't create directory " + dir.getAbsolutePath());
        }
        return dir.getPath();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#copy(java.io.InputStream, java.io.OutputStream, int)
     */
    public void copy(InputStream in, OutputStream out, int bufSize)
    {
        try
        {
            byte[] buf = new byte[bufSize];
            int length;
            while ((length = in.read(buf)) != -1)
            {
                out.write(buf, 0, length);
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to copy input stream [" + in.toString()
                + "] to output stream [" + out.toString() + "]", e);
        }
    }

    /**
     * {@inheritDoc}. The default buffer size if 1024.
     * @see FileHandler#copy(java.io.InputStream, java.io.OutputStream, int)
     */
    public void copy(InputStream in, OutputStream out)
    {
        copy(in, out, 1024);
    }

    /**
     * {@inheritDoc}.
     * @see FileHandler#replaceInFile(String, Map)
     */
    public void replaceInFile(String file, Map<String, String> replacements) throws CargoException
    {
        replaceInFile(file, replacements, null);
    }

    /**
     * {@inheritDoc}.
     * @see FileHandler#replaceInFile(String, Map, String)
     */
    public void replaceInFile(String file, Map<String, String> replacements, String encoding)
        throws CargoException
    {
        String fileContents = readTextFile(file, encoding);

        for (Map.Entry<String, String> replacement : replacements.entrySet())
        {
            if (!fileContents.contains(replacement.getKey()))
            {
                throw new CargoException("File " + file + " does not contain replacement key "
                    + replacement.getKey());
            }

            fileContents = fileContents.replace(replacement.getKey(), replacement.getValue());
        }

        try
        {
            Writer fw = newWriter(file, encoding);
            try
            {
                fw.write(fileContents);
            }
            finally
            {
                fw.close();
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot write file " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getTmpPath(String)
     */
    public String getTmpPath(String name)
    {
        return new File(new File(System.getProperty("java.io.tmpdir"), "cargo"), name).getPath();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.FileHandler#createUniqueTmpDirectory()
     */
    public synchronized String createUniqueTmpDirectory()
    {
        if (uniqueNameCounter == -1)
        {
            uniqueNameCounter = new Random().nextInt() & 0xffff;
        }
        File tmpDir;
        do
        {
            uniqueNameCounter++;
            tmpDir = new File(new File(System.getProperty("java.io.tmpdir")),
                "cargo/" + Integer.toString(uniqueNameCounter));
        }
        while (tmpDir.exists());
        tmpDir.deleteOnExit();
        mkdirs(tmpDir.getAbsolutePath());

        return tmpDir.getPath();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.FileHandler#delete(String)
     */
    public void delete(String path)
    {
        File pathAsFile = new File(path);
        if (pathAsFile.isDirectory())
        {
            File[] children = pathAsFile.listFiles();
            for (File element : children)
            {
                delete(element.getPath());
            }
        }
        pathAsFile.delete();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.FileHandler#getSize(String)
     */
    public long getSize(String file)
    {
        File fileObject = new File(file).getAbsoluteFile();
        if (!fileObject.isFile())
        {
            throw new CargoException("File [" + file + "] is not a file");
        }
        return fileObject.length();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.FileHandler#getInputStream(String)
     */
    public InputStream getInputStream(String file)
    {
        InputStream is;
        try
        {
            is = new FileInputStream(new File(file).getAbsoluteFile());
        }
        catch (FileNotFoundException e)
        {
            throw new CargoException("Failed to find file [" + file + "]", e);
        }
        return is;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.FileHandler#getOutputStream(String)
     */
    public OutputStream getOutputStream(String file)
    {
        String parent = getParent(file);
        if (parent != null)
        {
            mkdirs(parent);
        }

        OutputStream os;
        try
        {
            os = new FileOutputStream(file);
        }
        catch (FileNotFoundException e)
        {
            throw new CargoException("Failed to open output stream for file [" + file + "]", e);
        }
        return os;
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#append(String, String)
     */
    public String append(String path, String suffixToAppend)
    {
        String result;
        if (!path.endsWith("/") && !path.endsWith("\\"))
        {
            result = path + "/" + suffixToAppend;
        }
        else
        {
            result = path + suffixToAppend;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#mkdirs(String)
     */
    public void mkdirs(String path)
    {
        File pathFile = new File(path);
        boolean success = false;

        for (int i = 0; i < 3 && !success; i++)
        {
            // mkdirs() return false when the directory already exists so test for existence first
            if (pathFile.exists())
            {
                success = true;
            }
            else
            {
                success = pathFile.mkdirs();
            }
        }

        if (!success)
        {
            throw new CargoException("Failed to create folders for path [" + path + "]");
        }
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getParent(String)
     */
    public String getParent(String path)
    {
        return new File(path).getParent();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#exists(String)
     */
    public boolean exists(String path)
    {
        return new File(path).exists();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#createFile(String)
     */
    public void createFile(String file)
    {
        try
        {
            // If the file already exists, createNewFile() returns false but we ignore it as
            // we're just happy the file has been created in both cases.
            new File(file).createNewFile();
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to create file [" + file + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#isDirectoryEmpty(String)
     */
    public boolean isDirectoryEmpty(String dir)
    {
        return new File(dir).list().length == 0;
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getName(String)
     */
    public String getName(String file)
    {
        return new File(file).getName();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getURL(String)
     */
    public String getURL(String path)
    {
        URL result;
        try
        {
            result = new File(path).toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Failed to return URL for [" + path + "]", e);
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#isDirectory(String)
     */
    public boolean isDirectory(String path)
    {
        return new File(path).isDirectory();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getChildren(String)
     */
    public String[] getChildren(String directory)
    {
        String[] results;

        // Note: we use listFiles() instead of list() because list() returns relative paths only
        // and we need to return full paths.
        File[] files = new File(directory).listFiles();
        results = new String[files.length];
        for (int i = 0; i < files.length; i++)
        {
            results[i] = files[i].getPath();
        }

        return results;
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#getAbsolutePath(String)
     */
    public String getAbsolutePath(String path)
    {
        File file = new File(path);
        if (!file.isAbsolute())
        {
            file = new File(System.getProperty("user.dir"), file.getPath());
        }
        return file.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#readTextFile(String)
     */
    public String readTextFile(String file)
    {
        return readTextFile(file, null);
    }

    /**
     * {@inheritDoc}
     * @see FileHandler#readTextFile(String, String)
     */
    public String readTextFile(String file, String encoding)
    {
        BufferedReader in = null;
        StringBuilder out = new StringBuilder();
        try
        {
            try
            {
                in = new BufferedReader(newReader(getInputStream(file), encoding));
                String str;
                while ((str = in.readLine()) != null)
                {
                    if (out.length() > 0)
                    {
                        out.append(System.getProperty("line.separator"));
                    }
                    out.append(str);
                }
                return out.toString();
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to read text from file: " + file, e);
        }
    }

    /**
     * @param is The input stream to wrap, must not be {@code null}.
     * @param encoding The character encoding, may be {@code null}.
     * @return The reader, never {@code null}.
     * @throws IOException If the reader could not be opened.
     */
    private Reader newReader(InputStream is, String encoding) throws IOException
    {
        if (encoding == null || encoding.length() <= 0)
        {
            return new InputStreamReader(is);
        }
        return new InputStreamReader(is, encoding);
    }

    /**
     * @param file The file to open, must not be {@code null}.
     * @param encoding The character encoding, may be {@code null}.
     * @return The writer, never {@code null}.
     * @throws IOException If the writer could not be opened.
     */
    private Writer newWriter(String file, String encoding) throws IOException
    {
        if (encoding == null || encoding.length() <= 0)
        {
            return new FileWriter(file);
        }
        return new OutputStreamWriter(new FileOutputStream(file), encoding);
    }

}
