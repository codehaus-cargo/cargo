/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.daemon.properties.PropertyTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Start request for a container.
 */
public class StartRequest
{
    /**
     * The parameters.
     */
    private PropertyTable parameters;

    /**
     * The files.
     */
    private Map<String, FileItem> files;

    /**
     * Tells if this request needs to be saved.
     */
    private boolean save = false;

    /**
     * Parses the servlet request.
     * 
     * @param request The servlet request.
     * @return the StartRequest
     */
    public StartRequest parse(HttpServletRequest request)
    {
        ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());

        parameters = new PropertyTable();
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
                        if (item.getString() != null && !item.getString().isEmpty())
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

        if (value == null || value.isEmpty())
        {
            if (required)
            {
                throw new CargoDaemonException("Parameter " + name + " is required.");
            }
        }

        return value;
    }

    /**
     * @return The parameters of the request.
     */
    public PropertyTable getParameters()
    {
        return parameters;
    }

    /**
     * Set the parameters of the request.
     * @param parameters The parameters of the request.
     */
    public void setParameters(PropertyTable parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Gets the Properties associated with a key name.
     * 
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the properties
     */
    @SuppressWarnings("unchecked")
    public PropertyTable getProperties(String name, boolean required)
    {
        PropertyTable result = new PropertyTable();

        String value = getParameter(name, required);

        if (value != null && !value.isEmpty())
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
     * 
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the list of properties.
     */
    public List<PropertyTable> getPropertiesList(String name, boolean required)
    {
        List<PropertyTable> result = new ArrayList<PropertyTable>();

        String value = getParameter(name, required);

        if (value != null && !value.isEmpty())
        {
            try
            {
                JSONArray jsonArray = (JSONArray) JSONValue.parse(value);

                ListIterator iterator = jsonArray.listIterator();
                while (iterator.hasNext())
                {
                    JSONObject jsonObject = (JSONObject) iterator.next();

                    PropertyTable properties = new PropertyTable();
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
     * Gets list of string values.
     * 
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the list of string values.
     */
    public List<String> getStringList(String name, boolean required)
    {
        List<String> result = new ArrayList<String>();

        String value = getParameter(name, required);

        if (value != null && !value.isEmpty())
        {
            try
            {
                JSONArray jsonArray = (JSONArray) JSONValue.parse(value);

                result.addAll(jsonArray);
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
     * 
     * @param name The key name.
     * @param required If required {@code true}, otherwise {@code false}
     * @return the inputstream of the file
     */
    public InputStream getFile(String name, boolean required)
    {
        try
        {
            InputStream inputStream = null;

            if (files != null)
            {
                FileItem item = files.get(name);
                if (item != null)
                {
                    inputStream = item.getInputStream();
                }
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

    /**
     * @return if this request needs to be saved
     */
    public boolean isSave()
    {
        return save;
    }

    /**
     * Sets the save flag.
     * 
     * @param save True if request needs to be saved
     */
    public void setSave(boolean save)
    {
        this.save = save;
    }
}
