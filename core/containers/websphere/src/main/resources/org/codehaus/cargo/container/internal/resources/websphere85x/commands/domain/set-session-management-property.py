serverId = AdminConfig.list('Server')
webContainer = AdminConfig.list('WebContainer', serverId)
sessionManager = AdminConfig.list('SessionManager', webContainer)

services = AdminConfig.list('Service', sessionManager).splitlines()
attr = [['name', '@cargo.websphere.property.name@'],['value', '@cargo.websphere.property.value@']]
for service in services:
    AdminConfig.create('Property', service, attr)
