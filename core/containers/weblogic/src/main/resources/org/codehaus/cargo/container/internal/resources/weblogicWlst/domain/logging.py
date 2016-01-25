# Configure logs
cd('/Servers/@cargo.weblogic.server@')
create('@cargo.weblogic.server@', 'Log')
cd('Log/@cargo.weblogic.server@')
cmo.setRotationType('@cargo.weblogic.logging.rotation.type@')
cmo.setRedirectStderrToServerLogEnabled(true)
cmo.setRedirectStdoutToServerLogEnabled(true)
cmo.setLoggerSeverity('@cargo.weblogic.logging@')
cmo.setLogFileSeverity('@cargo.weblogic.logging@')
