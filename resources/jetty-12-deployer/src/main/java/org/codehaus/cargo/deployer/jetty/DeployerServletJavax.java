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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee8.webapp.WebAppContext;

/**
 * Servlet used by Cargo to remotely deploy and undeploy web applications on
 * Jetty 12.1 Java EE 8 environments.
 *
 * @see javax.servlet.http.HttpServlet
 * @see org.eclipse.jetty.ee8.webapp.WebAppContext
 */
public class DeployerServletJavax extends HttpServlet
{
    /**
     * Common Jetty deployer.
     */
    private JettyDeployerJavax deployer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        WebAppContext context =
            (WebAppContext) WebAppContext.getCurrentWebAppContext();

        if (context == null)
        {
            throw new ServletException(
                "Cannot find the current Jetty WebAppContext");
        }

        this.deployer = new JettyDeployerJavax(context);

        try
        {
            this.deployer.initialize(
                config.getInitParameter("timeout"),
                config.getServletContext().getInitParameter("timeout"));
        }
        catch (Exception e)
        {
            throw new ServletException(
                "Cannot initialize the Jetty deployer", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException
    {
        this.deployer.getDeployer().doGet(
            new ServletRequest(request),
            new ServletResponse(response));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException
    {
        this.deployer.getDeployer().doPut(
            new ServletRequest(request),
            new ServletResponse(response));
    }

    /**
     * Adapts a Javax servlet request.
     */
    private static class ServletRequest
        implements JettyDeployer.Request
    {
        /**
         * Underlying servlet request.
         */
        private final HttpServletRequest request;

        /**
         * Creates a request adapter.
         *
         * @param request servlet request
         */
        ServletRequest(HttpServletRequest request)
        {
            this.request = request;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getParameter(String name)
        {
            return this.request.getParameter(name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream()
            throws IOException
        {
            return this.request.getInputStream();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getServletPath()
        {
            return this.request.getServletPath();
        }
    }

    /**
     * Adapts a Javax servlet response.
     */
    private static class ServletResponse
        implements JettyDeployer.Response
    {
        /**
         * Underlying servlet response.
         */
        private final HttpServletResponse response;

        /**
         * Creates a response adapter.
         *
         * @param response servlet response
         */
        ServletResponse(HttpServletResponse response)
        {
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void sendMessage(String message)
            throws IOException
        {
            this.response.getWriter().println("OK - " + message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void sendError(String message)
            throws IOException
        {
            this.response.getWriter().println("Error - " + message);
        }
    }
}
