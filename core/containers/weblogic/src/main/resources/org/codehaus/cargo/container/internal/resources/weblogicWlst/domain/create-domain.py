print "Loading domain template."

readTemplate(r'@cargo.weblogic.home@/common/templates/wls/wls.jar')

print "Configuring domain and resources."

cd('/')
cd('Servers/AdminServer')
cmo.setName('@cargo.weblogic.server@')
set('ListenPort', @cargo.servlet.port@)

cd('/')
cd('Security/base_domain/User/weblogic')
cmo.setName('@cargo.weblogic.administrator.user@')
cmo.setPassword('@cargo.weblogic.administrator.password@')

# Configure logs
cd('/Servers/@cargo.weblogic.server@')
create('@cargo.weblogic.server@', 'Log')
cd('Log/@cargo.weblogic.server@')
cmo.setRotationType('@cargo.weblogic.logging.rotation.type@')
cmo.setRedirectStderrToServerLogEnabled(true)
cmo.setRedirectStdoutToServerLogEnabled(true)

cd('/')
setOption('OverwriteDomain', 'true')
cd('/')