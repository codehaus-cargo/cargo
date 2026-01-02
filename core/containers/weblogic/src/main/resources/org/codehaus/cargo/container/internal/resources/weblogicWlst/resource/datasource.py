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

cd('/')
create('@cargo.datasource.id@','JDBCSystemResource')

cd('JDBCSystemResource/@cargo.datasource.id@/JdbcResource/@cargo.datasource.id@')
create('myJdbcDriverParams','JDBCDriverParams')

cd('JDBCDriverParams/NO_NAME_0')
set('DriverName','@cargo.datasource.driver@')
if len('@cargo.datasource.url@') > 0:
    set('URL','@cargo.datasource.url@')
if len('@cargo.datasource.password@') > 0:
    set('PasswordEncrypted', '@cargo.datasource.password@')

create('myProps','Properties')
cd('Properties/NO_NAME_0')
create('user', 'Property')
cd('Property/user')
cmo.setValue('@cargo.datasource.username@')

cd('/JDBCSystemResource/@cargo.datasource.id@/JdbcResource/@cargo.datasource.id@')
create('myJdbcDataSourceParams','JDBCDataSourceParams')

cd('JDBCDataSourceParams/NO_NAME_0')
set('JNDIName','@cargo.datasource.jndi@')
if "@cargo.datasource.type@" == "javax.sql.XADataSource":
    set('GlobalTransactionsProtocol','TwoPhaseCommit')
elif "@cargo.datasource.transactionsupport@" == "XA_TRANSACTION":
    set('GlobalTransactionsProtocol','EmulateTwoPhaseCommit')
else:
    set('GlobalTransactionsProtocol','None')

cd('/')
assign('JDBCSystemResource', '@cargo.datasource.id@', 'Target', '@cargo.weblogic.server@')
