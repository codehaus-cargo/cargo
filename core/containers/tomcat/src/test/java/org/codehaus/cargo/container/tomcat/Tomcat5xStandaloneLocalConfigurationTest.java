/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.resource.Resource;
import org.codehaus.cargo.container.tomcat.Tomcat5xStandaloneLocalConfiguration;

import junit.framework.TestCase;

/**
 * @author Alexander Brill <alexander.brill@nhst.no>
 *
 */
public class Tomcat5xStandaloneLocalConfigurationTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link org.codehaus.cargo.container.tomcat.Tomcat5xStandaloneLocalConfiguration#createResourceTokenValue()}.
	 */
	public void testCreateResourceTokenValue() {
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"pass\"\n" +
			"          username=\"foo\"\n" +
			"/>\n";
		
		Tomcat5xStandaloneLocalConfiguration conf = new Tomcat5xStandaloneLocalConfiguration("foo");
		Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
        resource.setParameter("password", "pass");
		resource.setParameter("username", "foo");
		
		conf.addResource(resource);
		
		assertEquals("Resource string not correct", expected, conf.createResourceTokenValue());		
	}
	
	public void testCreateMultipleResourceTokenValues() {
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"pass\"\n" +
			"          username=\"foo\"\n" +
			"/>\n" +
			"<Resource name=\"otherDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"bar\"\n" +
			"          username=\"gazonk\"\n" +
			"/>\n";
		
		Tomcat5xStandaloneLocalConfiguration conf = new Tomcat5xStandaloneLocalConfiguration("foo");

		Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
        resource.setParameter("password", "pass");
		resource.setParameter("username", "foo");


		Resource resource2 = new Resource("otherDataSource", "javax.sql.DataSource");
	    resource2.setParameter("password", "bar");
		resource2.setParameter("username", "gazonk");
		
		conf.addResource(resource);
		conf.addResource(resource2);
		
		assertEquals("Resource string not correct", expected, conf.createResourceTokenValue());				
	}

}
