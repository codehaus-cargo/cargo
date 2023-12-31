/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.util.CargoException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * JSON marshaller for WildFly remote deployer.
 */
public class WildFlyRemoteDeploymentJsonMarshaller
{

    /**
     * JSON parser.
     */
    private JSONParser parser;

    /**
     * Configuration.
     */
    private RuntimeConfiguration configuration;

    /**
     * Constructor.
     * @param configuration Runtime configuration.
     */
    public WildFlyRemoteDeploymentJsonMarshaller(RuntimeConfiguration configuration)
    {
        parser = new JSONParser();
        this.configuration = configuration;
    }

    /**
     * @param response HTTP response body.
     * @return Value of BYTES_VALUE field in response.
     */
    public String unmarshallAddContentResponse(HttpResult response)
    {
        String responseBody = response.getResponseBody();
        if (responseBody == null || responseBody.isEmpty())
        {
            throw new CargoException("Response is empty!");
        }

        JSONObject obj;
        try
        {
            obj = (JSONObject) parser.parse(responseBody);
        }
        catch (ParseException e)
        {
            throw new CargoException("Exception during response parsing.", e);
        }

        JSONObject result = (JSONObject) obj.get("result");

        String bytesValue = (String) result.get("BYTES_VALUE");

        return bytesValue;
    }

    /**
     * @param deployable Deployable.
     * @param bytesValue Bytes value.
     * @return JSON request body for deploying deployable.
     */
    @SuppressWarnings("unchecked")
    public String marshallDeployRequest(Deployable deployable, String bytesValue)
    {
        JSONObject bytesValueObject = new JSONObject();
        bytesValueObject.put("BYTES_VALUE", bytesValue);

        JSONObject hashObject = new JSONObject();
        hashObject.put("hash", bytesValueObject);

        JSONObject deploymentObject = new JSONObject();
        deploymentObject.put("deployment", getDeployableFilename(deployable));

        JSONObject deployRequest = new JSONObject();
        deployRequest.put("content", wrapInArray(hashObject));
        deployRequest.put("address", wrapInArray(deploymentObject));
        deployRequest.put("operation", "add");
        deployRequest.put("enabled", "true");

        // JSON library escapes slash with backslash. This is unwanted feature
        // as WildFly needs exact hash value of uploaded content.
        String jsonString = deployRequest.toJSONString();
        return jsonString.replace("\\/", "/");
    }

    /**
     * @param deployable Deployable.
     * @return JSON request body for undeploying deployable.
     */
    @SuppressWarnings("unchecked")
    public String marshallUndeployRequest(Deployable deployable)
    {
        JSONObject deploymentObject = new JSONObject();
        deploymentObject.put("deployment", getDeployableFilename(deployable));

        JSONObject deployRequest = new JSONObject();
        deployRequest.put("address", wrapInArray(deploymentObject));
        deployRequest.put("operation", "undeploy");

        return deployRequest.toJSONString();
    }

    /**
     * @param deployable Deployable.
     * @return JSON request body for removing deployable.
     */
    @SuppressWarnings("unchecked")
    public String marshallRemoveRequest(Deployable deployable)
    {
        JSONObject deploymentObject = new JSONObject();
        deploymentObject.put("deployment", getDeployableFilename(deployable));

        JSONObject deployRequest = new JSONObject();
        deployRequest.put("address", wrapInArray(deploymentObject));
        deployRequest.put("operation", "remove");

        return deployRequest.toJSONString();
    }

    /**
     * @param deployable Deployable.
     * @return Deployable file name, which takes into account the WAR context, including the case
     * where it would be the root WAR. Moreover, when the JBoss or WildFly WAR file has the context
     * root set in the <code>jboss-web.xml</code> file, it will
     * <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1577">return the original WAR
     * file name</a>.
     */
    public String getDeployableFilename(Deployable deployable)
    {
        if (deployable instanceof JBossWAR)
        {
            JBossWAR jbossWar = (JBossWAR) deployable;
            if (jbossWar.containsJBossWebContext())
            {
                jbossWar.informJBossWebContext(this.configuration.getLogger());
                if ("true".equalsIgnoreCase(this.configuration.getPropertyValue(
                    JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME)))
                {
                    // CARGO-1577: When the JBoss or WildFly WAR file has the context root set
                    //             in the jboss-web.xml file, keep the original WAR file name
                    return jbossWar.getFileHandler().getName(jbossWar.getFile());
                }
            }
        }

        return deployable.getFilename();
    }

    /**
     * @param jsonObjects JSON objects to be wrapped.
     * @return Array wrapping JSON objects.
     */
    @SuppressWarnings("unchecked")
    private JSONArray wrapInArray(JSONObject... jsonObjects)
    {
        JSONArray array = new JSONArray();

        for (JSONObject object : jsonObjects)
        {
            array.add(object);
        }

        return array;
    }
}
