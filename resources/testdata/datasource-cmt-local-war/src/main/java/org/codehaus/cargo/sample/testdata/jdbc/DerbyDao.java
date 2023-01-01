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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Class used to interact with the Apache Derby DAO. We use this in our test Servlet used to verify
 * that jdbc connectors are working.
 *
 * @see org.codehaus.cargo.sample.testdata.jdbc.TestServlet
 */
public class DerbyDao extends JdbcDaoSupport
{
    /**
     * Creates the <code>Person</code> table.
     */
    public void createTable()
    {
        this.getJdbcTemplate().update(
            "CREATE TABLE Person("
                + "ID int generated by default as identity (start with 1) not null,"
                + "FIRSTNAME VARCHAR(20) NOT NULL," + "LASTNAME VARCHAR(20) NOT NULL,"
                + "PRIMARY KEY (ID)" + ")");
    }

    /**
     * Drops the <code>Person</code> table.
     */
    public void dropTable()
    {
        this.getJdbcTemplate().update("DROP TABLE Person" + "");
    }

    /**
     * Creates a person.
     * @param firstName First name.
     * @param lastName Last name.
     */
    public void create(String firstName, String lastName)
    {
        this.getJdbcTemplate().update("INSERT INTO PERSON (FIRSTNAME, LASTNAME) VALUES(?,?)",
            new Object[] {firstName, lastName});
    }

    /**
     * @return All {@link Person} objects in the <code>Person</code> database.
     */
    public List selectAll()
    {
        return this.getJdbcTemplate().query("select FIRSTNAME, LASTNAME from PERSON",
            new PersonRowMapper());
    }

    /**
     * Maps database rows from the <code>Person</code> table as {@link Person} objects.
     * {@inheritDoc}
     */
    class PersonRowMapper implements RowMapper
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Object mapRow(ResultSet rs, int line) throws SQLException
        {
            PersonResultSetExtractor extractor = new PersonResultSetExtractor();
            return extractor.extractData(rs);
        }

        /**
         * Extracts database rows from the <code>Person</code> table as {@link Person} objects.
         * {@inheritDoc}
         */
        class PersonResultSetExtractor implements ResultSetExtractor
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object extractData(ResultSet rs) throws SQLException
            {
                Person person = new Person();
                person.setFirstName(rs.getString(1));
                person.setLastName(rs.getString(2));
                return person;
            }

        }
    }
}
