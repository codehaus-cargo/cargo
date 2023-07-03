/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.Logger;

/**
 * File operations that are performed in Cargo. All file operations must use this class.
 */
public class DefaultFileHandler extends LoggedObject implements FileHandler
{
    /**
     * Default file buffer size.
     */
    public static final int FILE_BUFFER_SIZE = 256 * 1024;

    /**
     * Counter for creating unique temp directories.
     */
    private static int uniqueNameCounter = -1;

    /**
     * Sanitize a given name to turn it into a safe file name, removing for example leading or
     * trailing slashes, as well as intermediate parent path jumps.
     * @param filename name to sanitize
     * @param logger Logger to log when sanitization happens (optional)
     * @return sanitized name
     */
    public static String sanitizeFilename(String filename, Logger logger)
    {
        String sanitizedFilename = filename.replace('\\', '/');

        if (sanitizedFilename.startsWith("/"))
        {
            if (logger != null)
            {
                logger.debug("File name [" + filename
                    + "] has trailing slashes, removing for the sanitized file name",
                        DefaultFileHandler.class.getName());
            }
            sanitizedFilename = sanitizedFilename.replaceAll("^\\/+", "");
        }

        if (sanitizedFilename.endsWith("/"))
        {
            if (logger != null)
            {
                logger.debug("File name [" + filename
                    + "] has ending slashes, removing for the sanitized file name",
                        DefaultFileHandler.class.getName());
            }
            sanitizedFilename = sanitizedFilename.replaceAll("\\/+$", "");
        }

        while (sanitizedFilename.contains("/../"))
        {
            if (logger != null)
            {
                logger.debug("File name [" + filename
                    + "] has intermediate /../, replacing with single /",
                        DefaultFileHandler.class.getName());
            }
            sanitizedFilename = sanitizedFilename.replace("/../", "/");
        }

        while (sanitizedFilename.contains("//"))
        {
            if (logger != null)
            {
                logger.debug("File name [" + filename
                    + "] has intermediate //, replacing with single /",
                        DefaultFileHandler.class.getName());
            }
            sanitizedFilename = sanitizedFilename.replace("//", "/");
        }

        return sanitizedFilename.trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyFile(String source, String target)
    {
        this.copyFile(source, target, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyFile(String source, String target, boolean overwrite)
    {
        File sourceFile = new File(source);
        if (!sourceFile.isFile())
        {
            throw new CargoException("Source file [" + source + "] is not a file");
        }
        File targetFile = new File(target);
        if (targetFile.isFile() && !overwrite)
        {
            getLogger().debug("Skipping copy of existing binary file [" + target + "]",
                this.getClass().getName());
        }
        else
        {
            if (!targetFile.getParentFile().exists())
            {
                this.mkdirs(targetFile.getParentFile().getAbsolutePath());
            }
            if (targetFile.isDirectory())
            {
                targetFile = new File(this.append(target, getName(source)));
            }

            try (InputStream in = new FileInputStream(sourceFile);
                FileOutputStream out = new FileOutputStream(targetFile))
            {
                this.copy(in, out);
            }
            catch (IOException e)
            {
                throw new CargoException("Failed to copy source file [" + source + "] to ["
                    + targetFile + "]", e);
            }

            long size = targetFile.length();
            String unit = "bytes";
            if (size > 1024)
            {
                size = size / 1024;
                unit = "KB";
            }
            else if (size > 1024)
            {
                size = size / 1024;
                unit = "MB";
            }
            getLogger().debug("Copied binary file [" + source + "] to [" + target + "] (" + size
                + " " + unit + ")", this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyFile(
        String source, String target, Map<String, String> replacements, Charset encoding)
    {
        try (BufferedReader fileReader =
                new BufferedReader(this.newReader(this.getInputStream(source), encoding));
                BufferedWriter out = new BufferedWriter(this.newWriter(target, encoding)))
        {
            String line;
            while ((line = fileReader.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    out.newLine();
                }
                else
                {
                    if (replacements != null)
                    {
                        for (Map.Entry<String, String> replacement : replacements.entrySet())
                        {
                            String replacementKey = "@" + replacement.getKey() + "@";
                            line = line.replace(replacementKey, replacement.getValue());
                        }
                    }
                    out.write(line);
                    out.newLine();
                }
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to copy source file [" + source + "] to [" + target
                    + "] with replacements", e);
        }

        long size = getSize(target);
        String unit = "bytes";
        if (size > 1024)
        {
            size = size / 1024;
            unit = "KB";
        }
        else if (size > 1024)
        {
            size = size / 1024;
            unit = "MB";
        }
        getLogger().debug("Copied text file [" + source + "] to [" + target + "] (" + size + " "
            + unit + "), encoding " + encoding, this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDirectory(String source, String target)
    {
        this.copyDirectory(source, target, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDirectory(String source, String target, List<String> excludes)
    {
        File sourceDirectory = new File(source);
        if (!sourceDirectory.isDirectory())
        {
            throw new CargoException("Source [" + source + "] is not a directory");
        }

        File targetDirectory = new File(target);
        if (!targetDirectory.isDirectory())
        {
            targetDirectory.mkdirs();
        }
        if (!targetDirectory.isDirectory())
        {
            throw new CargoException("Target directory [" + target + "] cannot be created");
        }

        for (File sourceDirectoryContent : sourceDirectory.listFiles())
        {
            String sourcePath = sourceDirectoryContent.getAbsolutePath();
            String subtarget = this.append(target, sourceDirectoryContent.getName());

            boolean included = true;
            if (excludes != null)
            {
                for (String exclude : excludes)
                {
                    if (exclude.endsWith("/**"))
                    {
                        if (sourceDirectoryContent.isDirectory())
                        {
                            if (sourceDirectoryContent.getName().equals(
                                exclude.substring(0, exclude.length() - 3)))
                            {
                                // Content of the directory should be ignored,
                                // nevertheless an (empty) target directory should be kept
                                this.mkdirs(subtarget);

                                included = false;
                                break;
                            }
                        }
                    }
                    else if (exclude.startsWith("**/"))
                    {
                        if (sourceDirectoryContent.isFile())
                        {
                            if (sourceDirectoryContent.getName().endsWith(exclude.substring(3)))
                            {
                                included = false;
                                break;
                            }
                        }
                    }
                    else if (exclude.contains("*") || exclude.contains("/"))
                    {
                        throw new CargoException("Unsupported exclusion filter: " + exclude);
                    }
                }
            }
            if (included)
            {
                if (sourceDirectoryContent.isDirectory())
                {
                    // For subdirectories, only add file exclusions into the list
                    List<String> updatedExcludes = null;
                    if (excludes != null)
                    {
                        for (String exclude : excludes)
                        {
                            if (exclude.startsWith("**/"))
                            {
                                if (updatedExcludes == null)
                                {
                                    updatedExcludes = new ArrayList<String>();
                                    updatedExcludes.add(exclude);
                                }
                            }
                        }
                    }

                    this.copyDirectory(sourcePath, subtarget, updatedExcludes);
                }
                else
                {
                    this.copyFile(sourcePath, subtarget);
                }
            }
        }

        getLogger().debug("Copied directory [" + source + "] to [" + target + "] with exclusions ["
            + excludes + "]", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyDirectory(
        String source, String target, Map<String, String> replacements, Charset encoding)
    {
        File sourceDirectory = new File(source);
        if (!sourceDirectory.isDirectory())
        {
            throw new CargoException("Source [" + source + "] is not a directory");
        }

        File targetDirectory = new File(target);
        if (!targetDirectory.isDirectory())
        {
            targetDirectory.mkdirs();
        }
        if (!targetDirectory.isDirectory())
        {
            throw new CargoException("Target directory [" + target + "] cannot be created");
        }

        for (File sourceDirectoryContent : sourceDirectory.listFiles())
        {
            File targetFile = new File(targetDirectory, sourceDirectoryContent.getName());
            if (sourceDirectoryContent.isFile())
            {
                if (replacements == null)
                {
                    this.copyFile(
                        sourceDirectoryContent.getAbsolutePath(), targetFile.getAbsolutePath());
                }
                else
                {
                    this.copyFile(sourceDirectoryContent.getAbsolutePath(),
                        targetFile.getAbsolutePath(), replacements, encoding);
                }
            }
            else
            {
                this.copyDirectory(sourceDirectoryContent.getAbsolutePath(),
                    targetFile.getAbsolutePath(), replacements, encoding);
            }
        }

        getLogger().debug("Copied directory [" + source + "] to [" + target
            + "] with replacements [" + replacements + "]", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void explode(String war, String exploded)
    {
        if (exists(exploded))
        {
            delete(exploded);
        }

        Path explodedPath = new File(exploded).toPath();
        try (JarFile archive = new JarFile(new File(war).getAbsoluteFile()))
        {
            Enumeration e = archive.entries();
            while (e.hasMoreElements())
            {
                JarEntry j = (JarEntry) e.nextElement();
                String dst = this.append(exploded,
                    DefaultFileHandler.sanitizeFilename(j.getName(), getLogger()));

                if (j.isDirectory())
                {
                    this.mkdirs(dst);
                    continue;
                }

                this.mkdirs(getParent(dst));

                try (InputStream in = archive.getInputStream(j);
                    FileOutputStream out = new FileOutputStream(dst))
                {
                    this.copy(in, out);
                }
            }
        }
        catch (IOException e)
        {
            try
            {
                delete(exploded);
            }
            catch (Exception ignored)
            {
                // Ignored
            }

            throw new CargoException(
                "Failed to extract file [" + war + "] to [" + exploded + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createDirectory(String parentDir, String name)
    {
        File dir = new File(parentDir, name);
        this.mkdirs(dir.getAbsolutePath());
        if (!dir.isDirectory() || !dir.exists())
        {
            throw new CargoException("Couldn't create directory " + dir.getAbsolutePath());
        }
        return dir.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}. The default buffer size is {@link DefaultFileHandler#FILE_BUFFER_SIZE}.
     */
    @Override
    public void copy(InputStream in, OutputStream out)
    {
        this.copy(in, out, DefaultFileHandler.FILE_BUFFER_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceInFile(String file, Map<String, String> replacements, Charset encoding)
        throws CargoException
    {
        replaceInFile(file, replacements, encoding, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceInFile(String file, Map<String, String> replacements, Charset encoding,
        boolean ignoreNonExistingProperties) throws CargoException
    {
        String fileContents = readTextFile(file, encoding);

        for (Map.Entry<String, String> replacement : replacements.entrySet())
        {
            if (!fileContents.contains(replacement.getKey()))
            {
                String message = "File " + file + " does not contain replacement key "
                    + replacement.getKey();

                if (ignoreNonExistingProperties)
                {
                    getLogger().debug(message, this.getClass().getName());
                    continue;
                }
                else
                {
                    throw new CargoException(message);
                }
            }

            fileContents = fileContents.replace(replacement.getKey(), replacement.getValue());
        }

        writeTextFile(file, fileContents, encoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceInXmlFile(XmlReplacement... xmlReplacements) throws CargoException
    {
        Map<String, Map<XmlReplacementDetails, String>> replacements =
            new HashMap<String, Map<XmlReplacementDetails, String>>();

        for (XmlReplacement xmlReplacement : xmlReplacements)
        {
            Map<XmlReplacementDetails, String> replacementDetails =
                replacements.get(xmlReplacement.getFile());
            if (replacementDetails == null)
            {
                replacementDetails = new HashMap<XmlReplacementDetails, String>();
                replacements.put(xmlReplacement.getFile(), replacementDetails);
            }

            XmlReplacementDetails xmlReplacementDetails = new XmlReplacementDetails(
                xmlReplacement.getXpathExpression(), xmlReplacement.getAttributeName(),
                    xmlReplacement.getReplacementBehavior());
            replacementDetails.put(xmlReplacementDetails, xmlReplacement.getValue());
        }

        for (Map.Entry<String, Map<XmlReplacementDetails, String>> replacement
            : replacements.entrySet())
        {
            replaceInXmlFile(replacement.getKey(), replacement.getValue());
        }
    }

    /**
     * Replaces using a map of XML replacements in a given file.
     * 
     * @param file File to replace in.
     * @param replacements Map containing XML replacements.
     * @throws CargoException If anything fails, most notably if one of the replacements does not
     * exist in the file.
     */
    private void replaceInXmlFile(String file, Map<XmlReplacementDetails, String> replacements)
        throws CargoException
    {
        XmlUtils domUtils = new XmlUtils(this);
        Document doc = domUtils.loadXmlFromFile(file);

        try
        {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            for (Map.Entry<XmlReplacementDetails, String> replacement : replacements.entrySet())
            {
                String expression = replacement.getKey().getXpathExpression();
                String attributeName = replacement.getKey().getAttributeName();

                XPathExpression xPathExpr = xPath.compile(expression);

                Node node = (Node) xPathExpr.evaluate(doc, XPathConstants.NODE);

                if (node == null)
                {
                    String message = "Node " + expression + " not found in file " + file;

                    XmlReplacement.ReplacementBehavior replacementBehavior =
                            replacement.getKey().getReplacementBehavior();
                    switch (replacementBehavior)
                    {
                        case IGNORE_IF_NON_EXISTING:
                            getLogger().debug(message, this.getClass().getName());
                            continue;

                        case THROW_EXCEPTION:
                            throw new CargoException(message);

                        case ADD_MISSING_NODES:
                            node = new MissingXmlElementAppender(doc, expression).append();
                            break;

                        default:
                            throw new IllegalStateException("Unknown ReplacementBehavior ["
                                + replacementBehavior + "]");
                    }
                }

                if (attributeName != null)
                {
                    Node attribute = node.getAttributes().getNamedItem(attributeName);

                    if (attribute == null)
                    {
                        ((Element) node).setAttribute(attributeName, replacement.getValue());
                    }
                    else
                    {
                        attribute.setNodeValue(replacement.getValue());
                    }
                }
                else
                {
                    node.setTextContent(replacement.getValue());
                }
            }
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot modify XML document " + file, e);
        }

        domUtils.saveXml(doc, file);

        getLogger().debug("Performed XML replacements in [" + file + "]",
            this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTmpPath(String name)
    {
        return new File(new File(System.getProperty("java.io.tmpdir"), "cargo"), name).getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            tmpDir = new File(this.getTmpPath(Integer.toString(uniqueNameCounter)));
        }
        while (tmpDir.exists());
        tmpDir.deleteOnExit();
        this.mkdirs(tmpDir.getAbsolutePath());

        getLogger().debug("Created unique temporary directory [" + tmpDir + "]",
            this.getClass().getName());

        return tmpDir.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

        getLogger().debug("Deleted file [" + path + "]", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
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
     */
    @Override
    public OutputStream getOutputStream(String file)
    {
        String parent = getParent(file);
        if (parent != null)
        {
            this.mkdirs(parent);
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
     */
    @Override
    public String append(String path, String suffixToAppend)
    {
        String result;
        if (!path.endsWith("/") && !path.endsWith("\\"))
        {
            if (path.contains("\\"))
            {
                result = path + "\\" + suffixToAppend;
            }
            else
            {
                result = path + "/" + suffixToAppend;
            }
        }
        else
        {
            result = path + suffixToAppend;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mkdirs(String path)
    {
        File pathFile = new File(path);
        boolean success = false;

        for (int i = 0; i < 3 && !success; i++)
        {
            // mkdirs() return false when the directory already exists so test for existence first
            if (pathFile.isFile())
            {
                throw new CargoException("Path [" + pathFile + "] is a file and not a directory");
            }
            else if (pathFile.isDirectory())
            {
                success = true;

                getLogger().debug("Directory [" + pathFile + "] exists",
                    this.getClass().getName());
            }
            else
            {
                getLogger().debug("Creating directory [" + pathFile + "] and parents",
                    this.getClass().getName());

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
     */
    @Override
    public String getParent(String path)
    {
        return new File(path).getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(String path)
    {
        // Security note: Uncontrolled data used in path expression not relevant, we don't output
        return new File(path).exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFile(String file)
    {
        String parent = getParent(file);
        if (!isDirectory(parent))
        {
            mkdirs(parent);
        }

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
     */
    @Override
    public boolean isDirectoryEmpty(String dir)
    {
        File directory = new File(dir);
        if (!directory.isDirectory())
        {
            throw new CargoException("Path [" + dir + "] does not exist or is not a directory");
        }
        return directory.list().length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(String file)
    {
        return new File(file).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public boolean isDirectory(String path)
    {
        return new File(path).isDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getChildren(String directory)
    {
        return getChildren(directory, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getChildren(String directory, List<String> filters)
    {
        List<String> results = new ArrayList<String>();

        // Note: we use listFiles() instead of list() because list() returns relative paths only
        // and we need to return full paths.
        File[] files = new File(directory).listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (filters != null)
            {
                for (String filter : filters)
                {
                    if (filter.contains("/"))
                    {
                        throw new CargoException("Unsupported file filter: " + filter);
                    }
                    else if (filter.startsWith("*"))
                    {
                        if (files[i].getName().endsWith(filter.substring(1)))
                        {
                            results.add(files[i].getPath());
                        }
                    }
                    else if (filter.contains("*"))
                    {
                        throw new CargoException("Unsupported file filter: " + filter);
                    }
                    else if (files[i].getName().equals(filter))
                    {
                        results.add(files[i].getPath());
                    }
                }
            }
            else
            {
                results.add(files[i].getPath());
            }
        }

        return results.toArray(new String[results.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public String readTextFile(String file, Charset encoding)
    {
        try (BufferedReader in =
            new BufferedReader(this.newReader(this.getInputStream(file), encoding)))
        {
            String str;
            StringBuilder out = new StringBuilder();
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
        catch (IOException e)
        {
            throw new CargoException("Failed to read text from file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTextFile(String file, String content, Charset encoding)
    {
        try (Writer writer = this.newWriter(file, encoding))
        {
            writer.write(content);
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot write file" + file, e);
        }

        getLogger().debug("Wrote text file [" + file + "], encoding " + encoding,
            this.getClass().getName());
    }

    /**
     * @param is The input stream to wrap, must not be {@code null}.
     * @param encoding The character encoding, may be {@code null}.
     * @return The reader, never {@code null}.
     * @throws IOException If the reader could not be opened.
     */
    private Reader newReader(InputStream is, Charset encoding) throws IOException
    {
        if (encoding == null)
        {
            return new InputStreamReader(is, StandardCharsets.UTF_8);
        }
        else
        {
            return new InputStreamReader(is, encoding);
        }
    }

    /**
     * @param file The file to open, must not be {@code null}.
     * @param encoding The character encoding, may be {@code null}.
     * @return The writer, never {@code null}.
     * @throws IOException If the writer could not be opened.
     */
    private Writer newWriter(String file, Charset encoding) throws IOException
    {
        String parent = getParent(file);
        if (!isDirectory(parent))
        {
            this.mkdirs(parent);
        }

        if (encoding == null)
        {
            return new OutputStreamWriter(getOutputStream(file), StandardCharsets.UTF_8);
        }
        else
        {
            return new OutputStreamWriter(getOutputStream(file), encoding);
        }
    }

}
