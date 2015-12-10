cd('/')
app=create('@cargo.deployable.id@','AppDeployment')
app.setSourcePath(r'@cargo.deployable.path.absolute@')

cd('/')
assign('AppDeployment', '@cargo.deployable.id@', 'Target', '@cargo.weblogic.server@')