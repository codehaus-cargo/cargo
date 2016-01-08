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
createJ2EEResourceProperty(propSet, 'databaseName', 'java.lang.String', '@cargo.datasource.id@')