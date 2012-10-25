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
package org.codehaus.cargo.daemon.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.cargo.daemon.CargoDaemonException;
import org.codehaus.cargo.daemon.properties.Properties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Start request for a container.
 *
 * @version $Id$
 */
public class StartRequest
{
    /**
     * The parameters.
     */
    private Map<String, String> parameters;

    /**
     * The files.
     */
    private Map<String, FileItem> files;

    /**
     * File upload helper.
     */
    private final ServletFileUpload servletFileUpload =
        new ServletFileUpload(new DiskFileItemFactory());

    /**
     * Parses the servlet request.
     *
     * @param request The servlet request.
     * @return the StartRequest
     */
    @SuppressWarnings("unchecked")
    public StartRequest parse(HttpServletRequest request)
    {
        parameters = new HashMap<String, String>();
        files = new HashMap<String, FileItem>();

        if (ServletFileUpload.isMultipartContent(request))
        {
            List<FileItem> fileItems;
            try
            {
                fileItems = servletFileUpload.parseRequest(request);

                for (FileItem item : fileItems)
                {
                    String fieldName = item.getFieldName();
                    if (item.isFormField())
                    {
                        if (item.getString() != null && item.getString().length() > 0)
                        {
                            parameters.put(fieldName, item.getString());
                        }
                    }
                    else
                    {
                        files.put(fieldName, item);
                    }
                }
            }
            catch (FileUploadException e)
            {
                throw new CargoDaemonException(e);
            }
        }

        return this;
    }

    /**
     * Gets a parameters from the request.
     *
     * @param name The key name
     * @param required If required {@code true}, otherwise {@code false}
     * @return the value for the key name
     */
    public String getParameter(String name, boolean required)
    {
        String value = parameters.get(name);

        if (value == null || value.length() == 0)
        {
            if (required)
            {
                throw new CargoDaemonException("Parameter " + name + " is required.");
            }
        }

        return value;
    }

    /**
     * Gets the Properties associated with a key name.
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the properties
     */
    @SuppressWarnings("unchecked")
    public Properties getProperties(String name, boolean required)
    {
        Properties result = new Properties();

        String value = getParameter(name, required);

        if (value.length() > 0)
        {
            try
            {
                JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
                result.putAll(jsonObject);
            }
            catch (Throwable t)
            {
                throw new CargoDaemonException("Parameter " + name + " is not a JSON array", t);
            }
        }

        return result;
    }

    /**
     * Gets a list of Properties associated with a key name.
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the list of properties.
     */
    public List<Properties> getPropertiesList(String name, boolean required)
    {
        List<Properties> result = new ArrayList<Properties>();

        String value = getParameter(name, required);

        if (value != null && value.length() > 0)
        {
            try
            {
                JSONArray jsonArray = (JSONArray) JSONValue.parse(value);

                ListIterator iterator = jsonArray.listIterator();
                while (iterator.hasNext())
                {
                    JSONObject jsonObject = (JSONObject) iterator.next();

                    Properties properties = new Properties();
                    properties.putAll(jsonObject);

                    result.add(properties);
                }
            }
            catch (Throwable t)
            {
                throw new CargoDaemonException("Parameter " + name + " is not a JSON array", t);
            }
        }

        return result;
    }

    /**
     * Gets the inputstream of a file with key name {@code name}.
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the inputstream of the file
     */
    public InputStream getFile(String name, boolean required)
    {
        FileItem item = files.get(name);
        try
        {
            InputStream inputStream = null;

            if (item != null)
            {
                inputStream = item.getInputStream();
            }

            if (inputStream == null)
            {
                if (required)
                {
                    throw new CargoDaemonException("Parameter " + name + " is required.");
                }
            }

            return inputStream;
        }
        catch (IOException e)
        {
            throw new CargoDaemonException(e);
        }
    }

    /**
     * Cleans up the temporary data associated with this request.
     */
    public void cleanup()
    {
        try
        {
            for (FileItem item : files.values())
            {
                item.delete();
            }

            files.clear();
            parameters.clear();
        }
        catch (Throwable t)
        {
            return;
        }
    }
}
