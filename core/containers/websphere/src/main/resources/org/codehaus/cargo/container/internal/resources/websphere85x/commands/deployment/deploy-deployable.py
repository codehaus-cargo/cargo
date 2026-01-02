"""
  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.

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
