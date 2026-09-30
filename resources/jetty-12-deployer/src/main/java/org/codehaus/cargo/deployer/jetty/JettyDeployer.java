/*
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.cargo.deployer.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Contains the common deployment logic used by the Jetty Cargo deployer
 * servlets.<br>
 * <br>
 * This class deliberately has no dependency on either the
 * {@link javax.servlet.Servlet} API or the
 * {@link jakarta.servlet.Servlet} API. The servlet-specific classes adapt
 * HTTP requests and responses to the abstractions defined here.
 */
public class JettyDeployer
{
    /**
     * The directory where deployed WAR files are stored.
     */
    private final File webAppDirectory;

    /**
     * The maximum time to wait for a newly deployed web application.
     */
    private final long timeout;

    /**
     * Provides the Jetty-specific operations required by this deployer.
     */
    private final Context context;

    /**
     * Provides the request operations needed by the common deployer.
     */
    public interface Request
    {
        /**
         * Returns a request parameter.
         *
         * @param name parameter name
         * @return parameter value
         */
        String getParameter(String name);

        /**
         * Returns the request input stream.
         *
         * @return request input stream
         * @throws IOException if the stream cannot be obtained
         */
        InputStream getInputStream() throws IOException;

        /**
         * Returns the servlet path.
         *
         * @return servlet path
         */
        String getServletPath();
    }

    /**
     * Provides the response operations needed by the common deployer.
     */
    public interface Response
    {
        /**
         * Sends a successful response message.
         *
         * @param message response message
         * @throws IOException if the response cannot be written
         */
        void sendMessage(String message) throws IOException;

        /**
         * Sends an error response message.
         *
         * @param message response message
         * @throws IOException if the response cannot be written
         */
        void sendError(String message) throws IOException;
    }

    /**
     * Creates a deployer.
     *
     * @param context Jetty context adapter
     * @param webAppDirectory directory containing deployed WAR files
     * @param timeout deployment timeout in milliseconds
     */
    public JettyDeployer(
        Context context, File webAppDirectory, long timeout)
    {
        this.context = context;
        this.webAppDirectory = webAppDirectory;
        this.timeout = timeout;
    }

    /**
     * Handles a GET request.
     *
     * @param request request adapter
     * @param response response adapter
     * @throws IOException if the response cannot be written
     */
    public void doGet(Request request, Response response)
        throws IOException
    {
        String contextPath = request.getParameter("path");
        String warURL = request.getParameter("war");
        String command = request.getServletPath();

        if ("/deploy".equals(command))
        {
            deploy(response, contextPath, warURL);
        }
        else if ("/undeploy".equals(command))
        {
            undeploy(response, contextPath);
        }
        else
        {
            response.sendError("Command " + command + " is unknown");
        }
    }

    /**
     * Handles a PUT request.
     *
     * @param request request adapter
     * @param response response adapter
     * @throws IOException if the response cannot be written
     */
    public void doPut(Request request, Response response)
        throws IOException
    {
        String command = request.getServletPath();

        if ("/deploy".equals(command))
        {
            String contextPath = request.getParameter("path");
            deployArchive(request, response, contextPath);
        }
        else
        {
            response.sendError(
                "Command " + command + " is not recognized with PUT");
        }
    }

    /**
     * Deploys a WAR referenced by a URL.
     *
     * @param response response adapter
     * @param contextPath context path
     * @param warURL WAR URL
     * @throws IOException if the WAR cannot be copied
     */
    protected void deploy(
        Response response, String contextPath, String warURL)
        throws IOException
    {
        String deploymentContext = contextPath;

        if (deploymentContext == null)
        {
            File file = new File(warURL);
            String fileName = file.getName();

            if (fileName.endsWith(".war"))
            {
                fileName = fileName.substring(
                    0, fileName.lastIndexOf(".war"));
            }

            deploymentContext = "/" + fileName;
        }

        if (!deploymentContext.startsWith("/"))
        {
            response.sendError(
                "The path does not start with a forward slash");
            return;
        }

        if (contextExists(deploymentContext))
        {
            response.sendError(
                "An application is already deployed at this context: "
                    + deploymentContext);
            return;
        }

        File webappDestination = new File(
            webAppDirectory,
            getWebAppFilename(deploymentContext) + ".war");

        URI uri;

        try
        {
            uri = new URI(warURL);
        }
        catch (URISyntaxException e)
        {
            response.sendError("Cannot parse URL " + warURL);
            return;
        }

        File webappSource = new File(uri);
        Files.copy(webappSource.toPath(), webappDestination.toPath(),
               StandardCopyOption.REPLACE_EXISTING);

        try
        {
            context.deploy(deploymentContext, webappDestination);
        }
        catch (Exception e)
        {
            response.sendError(
                "Unexpected error when trying to start the webapp with "
                    + "context " + deploymentContext);
            return;
        }

        response.sendMessage(
            "Webapp deployed at context " + deploymentContext);
    }

    /**
     * Deploys a WAR received as the request body.
     *
     * @param request request adapter
     * @param response response adapter
     * @param contextPath context path
     * @throws IOException if the WAR cannot be written
     */
    protected void deployArchive(
        Request request, Response response, String contextPath)
        throws IOException
    {
        if (contextPath == null)
        {
            response.sendError("The path variable is not set");
            return;
        }

        if (!contextPath.startsWith("/"))
        {
            response.sendError("The path variable must start with /");
            return;
        }

        if (contextExists(contextPath))
        {
            response.sendError(
                "The webapp context path is already in use");
            return;
        }

        File webappFile = new File(
            webAppDirectory,
            getWebAppFilename(contextPath) + ".war");

        try (
            InputStream input = request.getInputStream();
            OutputStream output = new FileOutputStream(webappFile))
        {
            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, count);
            }
        }

        try
        {
            context.deploy(contextPath, webappFile);
        }
        catch (Exception e)
        {
            response.sendError(
                "Unexpected error when trying to start the webapp with "
                    + "context " + contextPath);
            return;
        }

        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline)
        {
            Object contextHandler =
                context.getContextHandler(contextPath);

            if (contextHandler != null)
            {
                response.sendMessage(
                    "Webapp deployed at context " + contextPath);
                return;
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                response.sendError(
                    "Got interrupted when trying to start the webapp");
                return;
            }
        }

        response.sendError(
            "Unexpected error when trying to start the webapp");
    }

    /**
     * Undeploys a web application.
     *
     * @param response response adapter
     * @param contextPath context path
     * @throws IOException if the response cannot be written
     */
    protected void undeploy(
        Response response, String contextPath)
        throws IOException
    {
        if (contextPath == null || !contextPath.startsWith("/"))
        {
            response.sendError(
                "Path must start with a forward slash");
            return;
        }

        Object handler = context.getContextHandler(contextPath);

        if (handler == null)
        {
            response.sendError(
                "Could not find handler for the context "
                    + contextPath);
            return;
        }

        String webAppLocation;

        try
        {
            webAppLocation = context.undeploy(handler);
        }
        catch (Exception e)
        {
            response.sendError(
                "Could not stop context handler " + contextPath);
            return;
        }

        File webAppFile;

        try
        {
            webAppFile = new File(new URI(webAppLocation));
        }
        catch (URISyntaxException e)
        {
            webAppFile = new File(webAppLocation);
        }

        if (!webAppFile.exists())
        {
            response.sendError(
                "Can't find a valid file for the context "
                    + contextPath + ": " + webAppLocation);
        }
        else if (!isInside(webAppFile, webAppDirectory))
        {
            response.sendMessage(
                "Webapp with context " + contextPath
                    + " has been undeployed but not removed from "
                    + "the filesystem");
        }
        else
        {
            boolean deleted;

            if (webAppFile.isFile())
            {
                deleted = webAppFile.delete();
            }
            else
            {
                deleteDirectory(webAppFile);
                deleted = !webAppFile.exists();
            }

            if (deleted)
            {
                response.sendMessage(
                    "Webapp with context " + contextPath
                        + " has been undeployed and removed from "
                        + "the filesystem");
            }
            else
            {
                response.sendError(
                    "Webapp with context " + contextPath
                        + " has been undeployed but it couldn't be "
                        + "removed from the filesystem");
            }
        }
    }

    /**
     * Checks whether a context is already deployed.
     *
     * @param contextPath context path
     * @return {@code true} if the context exists
     */
    private boolean contextExists(String contextPath)
    {
        return context.getContextHandler(contextPath) != null;
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param directory directory to delete
     */
    private void deleteDirectory(File directory)
    {
        File[] children = directory.listFiles();

        if (children != null)
        {
            for (File child : children)
            {
                if (child.isDirectory())
                {
                    deleteDirectory(child);
                }
                else
                {
                    child.delete();
                }
            }
        }

        directory.delete();
    }

    /**
     * Converts a context path into the filename used for its WAR.
     *
     * @param context context path
     * @return WAR filename without extension
     */
    protected String getWebAppFilename(String context)
    {
        String webappFileName = context;

        if (webappFileName == null
            || webappFileName.trim().isEmpty()
            || webappFileName.matches("/+"))
        {
            webappFileName = "ROOT";
        }
        else
        {
            webappFileName = webappFileName.replace('\\', '/');
            webappFileName = webappFileName.replaceAll("^\\/+", "");
            webappFileName = webappFileName.replaceAll("\\/+$", "");
            webappFileName = webappFileName.replaceAll("\\W", "-");
        }

        return webappFileName;
    }

    /**
     * Checks whether a file is located below a directory.
     *
     * @param file file to check
     * @param directory parent directory
     * @return {@code true} if the file is inside the directory
     */
    private boolean isInside(File file, File directory)
    {
        try
        {
            return file.getCanonicalFile().toPath()
                .startsWith(directory.getCanonicalFile().toPath());
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * Provides the Jetty-specific context operations required by the deployer.
     */
    public interface Context
    {
        /**
         * Finds a deployed context by its context path.
         *
         * @param contextPath context path
         * @return context handler, or {@code null} if none exists
         */
        Object getContextHandler(String contextPath);

        /**
         * Deploys a WAR at the specified context path.
         *
         * @param contextPath context path
         * @param warFile WAR file
         * @throws Exception if deployment fails
         */
        void deploy(String contextPath, File warFile)
            throws Exception;

        /**
         * Stops and removes a deployed context.
         *
         * @param contextHandler context handler
         * @return location of the deployed WAR
         * @throws Exception if undeployment fails
         */
        String undeploy(Object contextHandler)
            throws Exception;
    }
}
