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
package org.codehaus.cargo.sample.testdata.jdbc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Sample test Servlet used to verify that jdbc datasource is deployed.
 */
public class TestServlet extends HttpServlet
{
    /**
     * Spring Web application context.
     */
    private WebApplicationContext ctx;

    /**
     * Spring transaction manager.
     */
    private PlatformTransactionManager txManager;

    /**
     * Apache Derby DAO.
     */
    private DerbyDao dao;

    /**
     * Runs a simple query on the jdbc datasource, which creates a table and puts content in it.
     * {@inheritDoc}
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        def.setName("cargotest");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txManager.getTransaction(def);
        try
        {
            dao.create("Adrian", "Cole");
        }
        catch (RuntimeException ex)
        {
            txManager.rollback(status);
            throw ex;
        }
        txManager.commit(status);
        if (dao.selectAll().size() != 1)
        {
            throw new RuntimeException("Commit didn't work");
        }
        PrintWriter out = response.getWriter();
        out.print("all good!");
        out.close();
    }

    /**
     * Connects to the jdbc datasource.
     * {@inheritDoc}
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        ctx =
            WebApplicationContextUtils.getRequiredWebApplicationContext(config
                .getServletContext());
        dao = (DerbyDao) ctx.getBean("personDao");
        txManager = (PlatformTransactionManager) ctx.getBean("transactionManager");
        dao.createTable();
    }

    /**
     * Drops the temporary table.
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        dao.dropTable();
        super.destroy();
    }
}
