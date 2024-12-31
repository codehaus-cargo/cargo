"""
  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
"""

authAlias = '@cargo.websphere.node@/@cargo.datasource.username@'
jdbcProviderName = '@cargo.datasource.id@JdbcProvider'
serverId = getServerId('@cargo.websphere.node@', '@cargo.websphere.server@')

# create JdbcProvider
jdbcProvider = createJdbcProvider(serverId, jdbcProviderName, '@cargo.datasource.driver.classpath@', '', '@cargo.datasource.driver@', '')

# create JAAS entry
if (getJAAS(authAlias) is None):
    cell = AdminConfig.list("Cell")
    cellName = AdminConfig.showAttribute(cell, "name")
    sec = AdminConfig.getid("/Cell:" + cellName + "/Security:/")
    alias_attr = ["alias", authAlias]
    desc_attr = ["description", "authentication information"]
    userid_attr = ["userId", '@cargo.datasource.username@']
    password_attr = ["password", '@cargo.datasource.password@']
    attrs = [alias_attr, desc_attr, userid_attr, password_attr]
    AdminConfig.create("JAASAuthData", sec, attrs)

# create DS
dataSourceId = createDataSource(jdbcProvider, '@cargo.datasource.id@', '', '@cargo.datasource.jndi@', '150', authAlias, 'com.ibm.websphere.rsadapter.GenericDataStoreHelper')

# set custom properties to the DS
propSet = AdminConfig.list('J2EEResourcePropertySet', dataSourceId)
createJ2EEResourceProperty(propSet, 'URL', 'java.lang.String', '@cargo.datasource.url@')
