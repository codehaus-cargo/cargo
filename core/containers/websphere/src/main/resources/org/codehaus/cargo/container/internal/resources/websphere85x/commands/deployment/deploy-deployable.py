filename = '@cargo.deployable.path.absolute@'
servers = [{'nodename':'@cargo.websphere.node@','servername':'@cargo.websphere.server@'}]

arglist = [@cargo.deployable.websphere.arguments@]

installApplication(filename, servers,[],arglist)

# Set classloader policy
dep = AdminConfig.getid("/Deployment:@cargo.deployable.id@/")
depObject = AdminConfig.showAttribute(dep, "deployedObject")
AdminConfig.modify(depObject, [['warClassLoaderPolicy', '@cargo.websphere.war.classloader.policy@']])

# Set classloader mode
classloader = AdminConfig.showAttribute(depObject, "classloader")
AdminConfig.modify(classloader, [['mode', '@cargo.websphere.classloader.mode@']])
modules = AdminConfig.showAttribute(depObject, "modules")
arrayModules = modules[1:len(modules)-1].split(" ")
for module in arrayModules:
    if module.find('WebModuleDeployment') != -1:
        AdminConfig.modify(module, [['classloaderMode', '@cargo.websphere.classloader.mode@']])