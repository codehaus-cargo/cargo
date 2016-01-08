dsList = AdminConfig.list('DataSource').splitlines()
for ds in dsList:
    # Get the database name for this data source
    try: propSet = AdminConfig.showAttribute(ds, 'propertySet')
    except AdminException, why:
        print 'Error getting propertySet:'
        print why
    else:
        propList = AdminConfig.list('J2EEResourceProperty', propSet).splitlines()
        for prop in propList:
            if AdminConfig.showAttribute(prop, 'name') == 'databaseName':
                if AdminConfig.showAttribute(prop, 'value') == '@cargo.datasource.id@':
                    createJ2EEResourceProperty(propSet, '@cargo.datasource.properties.name@', 'java.lang.String', '@cargo.datasource.properties.value@')