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
package org.codehaus.cargo.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.cargo.daemon.properties.PropertyTable;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Keeps track of handles in memory, with support to load from and save to disk.
 * 
 * @version $Id: HandleDatabase.java $
 */
public class HandleDatabase
{
    /**
     * The list of handles.
     */
    private Map<String, Handle> handles = new ConcurrentHashMap<String, Handle>();

    /**
     * Loads the handles database from disk
     * 
     * @param inStream the input stream containing the handle records
     * @throws IOException if errors occurs
     */
    public void load(InputStream inStream) throws IOException
    {
        Properties properties = new Properties();

        properties.load(inStream);

        for (Map.Entry<Object, Object> property : properties.entrySet())
        {
            String handleId = (String) property.getKey();
            String value = (String) property.getValue();

            PropertyTable handleProperties = new PropertyTable();
            JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
            if (jsonObject != null)
            {
                handleProperties.putAll(jsonObject);
            }

            Handle handle = new Handle();
            handle.setId(handleId);
            handle.setProperties(handleProperties);
            handles.put(handleId, handle);
        }
    }

    /**
     * Stores handle database to disk
     * @param outStream Output stream to write data to.
     * @throws IOException if error occurs
     */
    public void store(OutputStream outStream) throws IOException
    {
        Properties properties = new Properties();

        for (Map.Entry<String, Handle> handle : handles.entrySet())
        {
            String handleId = handle.getKey();
            PropertyTable handleProperties = handle.getValue().getProperties();

            JSONObject value = new JSONObject();
            value.putAll(handleProperties);

            properties.put(handleId, value.toJSONString());
        }

        properties.store(outStream, null);
    }

    /**
     * Gets the handle object by handle id.
     * 
     * @param handleId The handle id.
     * @return the handle object.
     */
    public Handle get(String handleId)
    {
        return handles.get(handleId);
    }

    /**
     * Puts the handle object in database (in memory).
     * 
     * @param handleId The handle id.
     * @param handle The handle object
     */
    public void put(String handleId, Handle handle)
    {
        handles.put(handleId, handle);
    }

    /**
     * Removes the handle from the database.
     * 
     * @param handleId The handle id.
     */
    public void remove(String handleId)
    {
        handles.remove(handleId);
    }

    /**
     * @return The entry set of the handle database.
     */
    public Set<Map.Entry<String, Handle>> entrySet()
    {
        return handles.entrySet();
    }
}
