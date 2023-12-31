"""
  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.

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
mail=create('@cargo.resource.id@','MailSession')
mail.setJNDIName('@cargo.resource.name@')

properties = java.util.Properties();

if len('@cargo.mail.from@') > 0:
    properties.put('mail.from','@cargo.mail.from@');

properties.put('mail.transport.protocol','smtp');
properties.put('mail.smtp.host','@cargo.mail.smtp.host@');
properties.put('mail.smtp.port','@cargo.mail.smtp.port@');
mail.setProperties(properties)

cd('/')
assign('MailSession', '@cargo.resource.id@', 'Target', '@cargo.weblogic.server@')
