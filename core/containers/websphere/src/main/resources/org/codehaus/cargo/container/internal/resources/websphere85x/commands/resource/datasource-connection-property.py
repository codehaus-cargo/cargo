propertySet = AdminConfig.getid("/JDBCProvider:%s/DataSource:%s/J2EEResourcePropertySet:/" % ('@cargo.datasource.id@JdbcProvider', '@cargo.datasource.id@'))
createJ2EEResourceProperty(propSet, '@cargo.datasource.properties.name@', 'java.lang.String', '@cargo.datasource.properties.value@')
