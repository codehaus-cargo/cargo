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

import junit.framework.TestCase;

/**
 * @author Alexander Brill <alexander.brill@nhst.no>
 *
 */
public class Tomcat4xStandaloneLocalConfigurationTest extends TestCase {

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
	 * Test method for {@link org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration#createResourceTokenValue()}.
	 */
	public void testCreateResourceTokenValue() {
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          auth=\"Container\"\n" +
			"/>\n" +
			"<ResourceParams name=\"myDataSource\">\n" +
			"  <parameter>\n" +
			"    <name>password</name>\n" +
			"    <value>pass</value>\n" +
			"  </parameter>\n" +
			"  <parameter>\n" +
			"    <name>username</name>\n" +
			"    <value>foo</value>\n" +
			"  </parameter>\n" +
			"</ResourceParams>\n";
		
		Tomcat4xStandaloneLocalConfiguration conf = new Tomcat4xStandaloneLocalConfiguration("foo");
		Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
		resource.setParameter("password" , "pass");
		resource.setParameter("username", "foo");
		
		conf.addResource(resource);
				
		assertEquals("Resource string not correct", expected, conf.createResourceTokenValue());		
		
	}

}
