/*
 * ========================================================================
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
package org.codehaus.cargo.deployer.jetty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.ClassMatcher;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet is used to control deploy, undeploy, redeploy, start, and stop a web application
 * within the jetty server.
 */
public class DeployerServlet extends HttpServlet
{

    /**
     * Empty class matcher.
     */
    private static final ClassMatcher EMPTY_CLASS_MATCHER = new ClassMatcher();

    /**
     * The context.
     */
    private WebAppContext context;

    /**
     * The Jetty logger.
     */
    private Logger logger;

    /**
     * The ContectHandlerCollection for the server.
     */
    private ContextHandlerCollection chc;

    /**
     * The location of the server's webapp directory
     */
    private File webAppDirectory;

    /**
     * Timeout when deploying.
     */
    private long timeout;

    /**
     * Initialize the DeployerServlet and obtain a reference to the server in which it is deployed.
     * This gives the servlet access to the server internals which allows for deployment control.
     * @throws Exception If any exception occurs.
     */
    public DeployerServlet() throws Exception
    {
        this.context = (WebAppContext)
            ((WebAppClassLoader) this.getClass().getClassLoader()).getContext();

        this.logger = LoggerFactory.getLogger(this.getClass());

        // The org.eclipse.jetty.server.Server class doesn't have any getter for the Jetty
        // configuration home, so we need to read existing system properties
        String configHome = System.getProperty("config.home");
        if (configHome == null)
        {
            configHome = System.getProperty("jetty.base");
        }
        if (configHome == null)
        {
            configHome = System.getProperty("jetty.home");
        }
        if (configHome == null)
        {
            throw new IllegalStateException("Cannot find the Jetty configuration home");
        }
        this.webAppDirectory = new File(configHome, "webapps");

        // TODO there could potentially be more than one context handler collection and there is
        // also the chance that a web application can be deployed under the same context. These
        // situations should be looked after but are currently not.
        Server server = this.context.getServer();
        Handler[] handles = server.getChildHandlers();
        for (Handler handle : handles)
        {
            if (handle instanceof ContextHandlerCollection)
            {
                chc = (ContextHandlerCollection) handle;
                break;
            }
        }

        this.logger.debug(
            "Started the Codehaus Cargo Jetty deployer servlet with context " + this.context);

        // Due to weird bugs with Jetty 9.4.x webapp class loader, we need to access some classes
        // in the constructor to avoid ClassNotFoundExceptions when accessing later.
        Class dummy;
        dummy = ContextHandler.class;
        dummy = Thread.class;
        dummy = PrintWriter.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            // CARGO-1400: Make the timeout for JettyRemoteDeployer configurable
            String timeoutProperty = System.getProperty("cargo.jetty.deployer.timeout");
            if (timeoutProperty != null)
            {
                this.timeout = Long.parseLong(timeoutProperty);
            }
            else
            {
                this.timeout = Long.parseLong(config.getInitParameter("timeout"));
            }
            if (this.timeout < 1)
            {
                throw new IllegalArgumentException("Timeout is smaller than 1: " + this.timeout);
            }
        }
        catch (Exception e)
        {
            throw new ServletException("Cannot parse the [timeout] servlet parameter", e);
        }
    }

    /**
     * Performs the http GET method.
     * @param request The http request
     * @param response The http response
     * @throws ServletException If a servlet exception occurs
     * @throws IOException If an io exception occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        ClassMatcher serverClasses = this.context.getServerClassMatcher();

        try
        {
            this.context.setServerClassMatcher(DeployerServlet.EMPTY_CLASS_MATCHER);

            String contextPath = request.getParameter("path");
            String warURL = request.getParameter("war");

            String command = request.getServletPath();

            if (command.equals("/deploy"))
            {
                deploy(response, contextPath, warURL);
            }
            else if (command.equals("/undeploy"))
            {
                undeploy(response, contextPath);
            }
            else
            {
                response.sendError(400, "Command " + command + " is unknown");
            }
        }
        finally
        {
            this.context.setServerClassMatcher(serverClasses);
        }
    }

    /**
     * Performs the http PUT method.
     * @param request The http request
     * @param response The http response
     * @throws ServletException If a servlet exception occurs
     * @throws IOException If an io exception occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        ClassMatcher serverClasses = this.context.getServerClassMatcher();

        try
        {
            this.context.setServerClassMatcher(DeployerServlet.EMPTY_CLASS_MATCHER);

            String command = request.getServletPath();
            if (command.equals("/deploy"))
            {
                String contextPath = request.getParameter("path");
                deployArchive(request, response, contextPath);
            }
            else
            {
                sendError(response, "Command " + command + " is not recognized with PUT");
            }
        }
        finally
        {
            this.context.setServerClassMatcher(serverClasses);
        }
    }

    /**
     * Deploys the archive to the server.
     * @param request The http request
     * @param response The http response
     * @param contextPath The context path for the deployed archive
     * @throws IOException If an io exception occurs
     */
    protected void deployArchive(HttpServletRequest request, HttpServletResponse response,
            String contextPath) throws IOException
    {
        this.logger.debug("Remotely deploying a remote web archive with context " + contextPath);

        if (contextPath == null)
        {
            sendError(response, "The path variable is not set");
        }
        else if (!contextPath.startsWith("/"))
        {
            sendError(response, "The path variable must start with /");
        }
        else if (getContextHandler(contextPath) != null)
        {
            sendError(response, "The webapp context path is already in use");
        }
        else
        {
            this.logger.debug("trying to get the remote web archive for context " + contextPath);

            File webappFile = new File(
                this.webAppDirectory, getWebAppFilename(contextPath) + ".war");

            InputStream inputStream = new BufferedInputStream(request.getInputStream());
            OutputStream outputStream =
                new BufferedOutputStream(new FileOutputStream(webappFile), 8096);

            // transfer the data across
            int i = inputStream.read();
            while (i != -1)
            {
                outputStream.write(i);
                i = inputStream.read();
            }

            // close and flush readers
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // CARGO-1122: Just wait for Jetty to deploy the application by itself
            long timeout = System.currentTimeMillis() + this.timeout;
            while (System.currentTimeMillis() < timeout)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    sendError(response, "Got interrupted when trying to start the webapp");
                    return;
                }

                Handler contextHandler = (Handler) getContextHandler(contextPath);
                if (contextHandler != null && contextHandler.isStarted())
                {
                    sendMessage(response, "Webapp deployed at context " + contextPath);
                    return;
                }
            }

            sendError(response, "Unexpected error when trying to start the webapp");
        }
    }

    /**
     * Returns the file if it exists for the specified context path. If the file does not exist
     * then it will return null.
     * @param contextPath The context path for the web app
     * @return The file associated with the context path
     */
    protected File getFile(String contextPath)
    {
        String fileName = getWebAppFilename(contextPath);

        File webappArchive = new File(webAppDirectory, fileName + ".war");
        File webappDirectory = new File(webAppDirectory, fileName);

        File returnedFile = null;

        if (webappArchive.exists())
        {
            returnedFile = webappArchive;
        }
        else if (webappDirectory.exists())
        {
            returnedFile = webappDirectory;
        }

        return returnedFile;
    }

    /**
     * Sends the given message back in the http response.
     * @param response The http response
     * @param message The message to be send
     * @throws IOException If an io exception occurs
     */
    protected void sendMessage(HttpServletResponse response, String message) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.println("OK - " + message);
    }

    /**
     * Sends the given error message back in the http response.
     * @param response The http response
     * @param message The error message to be send
     * @throws IOException If an io exception occurs
     */
    protected void sendError(HttpServletResponse response, String message) throws IOException
    {
        PrintWriter writer = response.getWriter();
        writer.println("Error - " + message);
    }

    /**
     * Deploy the war to the given context path.
     * @param response The http response
     * @param contextPath The context path to use
     * @param warURL The location of the war
     * @throws IOException If an io exception occurs
     */
    protected void deploy(HttpServletResponse response, String contextPath, String warURL)
        throws IOException
    {
        String context = contextPath;
        boolean error = false;

        // if the contextPath is null, then we should use the context from the
        // war's name
        if (context == null)
        {
            // Security note: Uncontrolled data used in path expression not relevant,
            //                we don't write any data
            File file = new File(warURL);
            String fileName = file.getName();
            if (fileName.endsWith(".war"))
            {
                fileName = fileName.substring(0, fileName.lastIndexOf(".war"));
            }
            // need to add a forward slash to the beginning
            context = "/" + fileName;
        }

        // check to make sure that another application is not already deployed
        // to the same context
        if (getContextHandler(context) != null)
        {
            sendError(response, "An application is already deployed at this context: " + context);
            error = true;
        }
        else if (!context.startsWith("/"))
        {
            sendError(response, "The path does not start with a forward slash");
            error = true;
        }

        if (error)
        {
            return;
        }
        else
        {
            File webappDest = new File(webAppDirectory, getWebAppFilename(context) + ".war");

            URI uri = null;
            try
            {
                uri = new URI(warURL);
            }
            catch (URISyntaxException e)
            {
                String errorMessage = "Cannot parse URL " + warURL;
                sendError(response, errorMessage);
                this.logger.warn(errorMessage, e);
                return;
            }

            // Security note: Uncontrolled data used in path expression not avoidable,
            //                we need to read the source file requested
            File webappSource = new File(uri);

            FileInputStream fileInputStream = new FileInputStream(webappSource);
            FileOutputStream fileOutputStream = new FileOutputStream(webappDest);

            int i = fileInputStream.read();
            while (i != -1)
            {
                fileOutputStream.write(i);
                i = fileInputStream.read();
            }

            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

            WebAppContext webappcontext = new WebAppContext();
            webappcontext.setContextPath(context);
            webappcontext.setWar(webappDest.getPath());
            chc.addHandler(webappcontext);
            try
            {
                webappcontext.start();
            }
            catch (Exception e)
            {
                String errorMessage =
                    "Unexpected error when trying to start the with context " + contextPath;
                sendError(response, errorMessage);
                this.logger.warn(errorMessage, e);
                return;
            }
        }

        sendMessage(response, "Webapp deployed at context " + contextPath);
    }

    /**
     * Undeploy the webapp with the given context path.
     * @param response The http response
     * @param contextPath The context path
     * @throws IOException If an IO exception occurs
     */
    protected void undeploy(HttpServletResponse response, String contextPath) throws IOException
    {
        boolean error = false;
        if (!contextPath.startsWith("/"))
        {
            sendError(response, "Path must start with a forward slash");
            error = true;
        }
        Object handler = getContextHandler(contextPath);
        if (handler == null)
        {
            sendError(response, "Could not find handler for the context " + contextPath);
            error = true;
        }
        try
        {
            ((ContextHandler) handler).stop();
        }
        catch (Exception e)
        {
            String errorMessage = "Could not stop context handler " + contextPath;
            sendError(response, errorMessage);
            this.logger.warn(errorMessage, e);
            error = true;
        }

        if (error)
        {
            return;
        }
        else
        {
            chc.removeHandler((ContextHandler) handler);

            // NOTE THIS ACTUALLY DELETES THE FILE FROM THE FILESYSTEM
            // PLEASE BE VERY CAREFUL WHEN MAKING CHANGES HERE
            String webAppLocation = getWebAppLocation((WebAppContext) handler);
            File webAppFile = new File(webAppLocation);

            if (!webAppFile.exists())
            {
                sendError(response, "Can't find a valid file for the context " + contextPath);
            }
            else if (!webAppFile.getPath().startsWith(webAppDirectory.getPath()))
            {
                sendMessage(response, "Webapp with context " + contextPath
                    + " has been undeployed but not removed from the filesystem");
            }
            else
            {
                boolean deleted = false;
                if (webAppFile.isFile())
                {
                    deleted = webAppFile.delete();
                }
                else
                {
                    // we are dealing with a directory, which is a pain to
                    // delete so we have to call a separate method
                    deleteDirectory(webAppFile);
                    deleted = !webAppFile.exists();
                }

                if (deleted)
                {
                    sendMessage(response, "Webapp with context " + contextPath
                        + " has been undeployed and removed from the filesystem");
                }
                else
                {
                    sendError(response, "Webapp with context " + contextPath
                        + " has been undeployed but it couldn't be removed from the filesystem");
                }
            }
        }
    }

    /**
     * Recursively delete the webapp directory.
     * @param webAppFile The file to delete
     */
    protected void deleteDirectory(File webAppFile)
    {
        if (webAppFile.isDirectory())
        {
            File[] children = webAppFile.listFiles();
            for (File element : children)
            {
                deleteDirectory(element);
            }
            webAppFile.delete();
        }
        else
        {
            webAppFile.delete();
        }
    }

    /**
     * Returns the webapp file name for a given context.
     * @param context The webapp context
     * @return The file name (without extension) for the provided webapp context, filtering out
     * all non-word characters
     */
    protected String getWebAppFilename(String context)
    {
        String webappFileName = context;
        if (webappFileName == null || webappFileName.trim().isEmpty()
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
     * Returns the file location for the specified webapp.
     * @param webapp The webapp
     * @return The location of the webapp
     */
    protected String getWebAppLocation(WebAppContext webapp)
    {
        // getWar will return either the file location or a URL based on how the
        // webapp was deployed
        String location = webapp.getWar();

        // manually comparing strings since easier than trying to do it with the
        // java.net.URL methods
        if (location.startsWith("jar:"))
        {
            location = location.substring("jar:".length());
            // sometime it will end with !/ we need to remove this
            if (location.endsWith("!/"))
            {
                location = location.substring(0, location.length() - "!/".length());
            }
        }
        if (location.startsWith("file:"))
        {
            location = location.substring("file:".length());
        }

        return location;
    }

    /**
     * Returns the context handler for the given context.<br><b>Note</b>: We need to return an
     * <code>Object</code> in order to avoid
     * <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1049">bug CARGO-1049</a>.
     * @param context The webapp context
     * @return The context handler, of type
     * <code>org.eclipse.jetty.server.handler.ContextHandler</code>.
     */
    protected Object getContextHandler(String context)
    {
        // Note: this is very inefficient, but I think its the only way that will work. It would
        // have been nice if they used a map and a context could have been used to retrieve the
        // handler.
        Handler[] handlers = chc.getHandlers();
        for (Handler handler : handlers)
        {
            if (handler instanceof ContextHandler)
            {
                if (((ContextHandler) handler).getContextPath().equals(context))
                {
                    return handler;
                }
            }
        }

        // return null if no instance was found
        return null;
    }

}
