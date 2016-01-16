# Configure server SSL
cd('/Servers/@cargo.weblogic.server@')

create('@cargo.weblogic.server@','SSL')
cd('SSL/@cargo.weblogic.server@')

cmo.setHostnameVerificationIgnored(@cargo.weblogic.ssl.verification.hostname.ignored@)
cmo.setTwoWaySSLEnabled(false)
cmo.setClientCertificateEnforced(false)

if '@cargo.weblogic.ssl.verification.hostname.class@' != 'None':
    cmo.setHostnameVerifier('@cargo.weblogic.ssl.verification.hostname.class@')
    cmo.setHostnameVerificationIgnored(false)
