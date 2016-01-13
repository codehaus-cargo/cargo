setApplicationSecurity('@cargo.websphere.security.application@')

AdminTask.configureCSIInbound('[-messageLevelAuth Supported -clientCertAuth Supported -transportLayer Supported]')
AdminTask.configureCSIOutbound('[-messageLevelAuth Supported -clientCertAuth Never -transportLayer Supported]')

# Log size setting
serverId = getServerId('@cargo.websphere.node@', '@cargo.websphere.server@')
outLog = AdminConfig.showAttribute(serverId, 'outputStreamRedirect')
errLog = AdminConfig.showAttribute(serverId, 'errorStreamRedirect')
AdminConfig.modify(outLog, [['rolloverSize', '@cargo.websphere.logging.rollover@']])
AdminConfig.modify(errLog, [['rolloverSize', '@cargo.websphere.logging.rollover@']])
