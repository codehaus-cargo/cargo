"""
   Copyright IBM Corp. 1996, 2012

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


"""
Introduction
------------
wsadminlib is a library to simplify configuring WebSphere.
It intends to hide the complexities of the wsadmin scripting
interface behind task-oriented methods.

These methods are invoked when connected to one dmgr or standalone server.
If you need multiple cells, you must configure them one at a time.

Typical wsadminlib usage would be to write a script that uses these
methods, then invoke it with 'wsadmin -lang jython -f yourscript'.

At the top of your script, you should load wsadminlib by
using something like this:

execfile('wsadminlib.py')

This is necessary rather than using "import" because this
code needs access to the AdminConfig, AdminTask, etc.
global variables that wsadmin creates in your main module,
and won't work if loaded as a separate module using import.

Note: when developing new methods herein, you can test them
quickly by starting wsadmin.sh -lang jython, and issuing
execfile('wsadminlib.py') every time you save this file.


Coding Standards
----------------
1. Don't change existing method signatures without talking to existing users.
   Why? Other consumers are using these methods in production.

2. No tabs.
   Why? Consistent indentation. Not everyone uses your editor.

3. Always return rc or throw an exception on failure.
   Never fail silently or with warning message only.
   Why?  Identify the failure as soon as possible to speed debug.

4. Make your methods look like the rest (ie, follow python style).
   Why? Varying styles is distracting. Code for future programmers.

5. Use method sop() for logging.
   Why? Consistent output, sexy timestamp, easy future enhancements.

"""
_modules = [
            'sys',
            'time',
            're',
            'glob',
            'os',
            'os.path',
            'getopt',
            'traceback',
           ]

# A lot of modules aren't available in WAS 602.
# Log an import failure, but continue on so that scripts can
# still call functions that don't use these modules.

for module in _modules:
  try:
    locals()[module] = __import__(module, {}, {}, [])
  except ImportError:
    print 'Error importing %s.' % module

# Provide access to wsadminlib methods when accessed as an import.
# This is benign if wsadminlib is opened with execfile().
# Supports both connected and disconnected operations.
# (ie, works when wsadmin is connected to a running server,
# and works when wsadmin is not connected to a server.)
try:
    AdminConfig = sys._getframe(1).f_locals['AdminConfig']
    AdminApp = sys._getframe(1).f_locals['AdminApp']
    AdminControl = sys._getframe(1).f_locals['AdminControl']
    AdminTask = sys._getframe(1).f_locals['AdminTask']
except:
    print "Warning: Caught exception accessing Admin objects. Continuing."

# Define False, True
(False,True)=(0,1)

##############################################################################
# Really basic stuff for messing around with configuration objects

def _splitlist(s):
    """Given a string of the form [item item item], return a list of strings, one per item.
    WARNING: does not yet work right when an item has spaces.  I believe in that case we'll be
    given a string like '[item1 "item2 with spaces" item3]'.
    """
    if s[0] != '[' or s[-1] != ']':
        raise "Invalid string: %s" % s
    # Remove outer brackets and strip whitespace
    itemstr = s[1:-1].strip()
    if itemstr == '':
        itemarray = []
    else:
        itemarray = itemstr.split(' ')
    return itemarray

def _splitlines(s):
  rv = [s]
  if '\r' in s:
    rv = s.split('\r\n')
  elif '\n' in s:
    rv = s.split('\n')
  if rv[-1] == '':
    rv = rv[:-1]
  return rv


def getObjectAttribute(objectid, attributename):
    """Return the value of the named attribute of the config object with the given ID.
    If there's no such attribute, returns None.
    If the attribute value looks like a list, converts it to a real python list.
    TODO: handle nested "lists"
    """
    #sop("getObjectAttribute:","AdminConfig.showAttribute(%s, %s)" % ( repr(objectid), repr(attributename) ))
    result = AdminConfig.showAttribute(objectid, attributename)
    if result != None and result.startswith("[") and result.endswith("]"):
        # List looks like "[value1 value2 value3]"
        result = _splitlist(result)
    return result

def setObjectAttributes(objectid, **settings):
    """Set some attributes on an object.
    Usage: setObjectAttributes(YourObjectsConfigId, attrname1=attrvalue1, attrname2=attrvalue2)
    for 0 or more attributes."""
    m = "setObjectAttributes:"
    #sop(m,"ENTRY(%s,%s)" % (objectid, repr(settings)))
    attrlist = []
    for key in settings.keys():
        #sop(m,"Setting %s=%s" % (key,settings[key]))
        attrlist.append( [ key, settings[key] ] )
    #sop(m,"Calling AdminConfig.modify(%s,%s)" % (repr(objectid),repr(attrlist)))
    AdminConfig.modify(objectid, attrlist)

def getObjectsOfType(typename, scope = None):
    """Return a python list of objectids of all objects of the given type in the given scope
    (another object ID, e.g. a node's object id to limit the response to objects in that node)
    Leaving scope default to None gets everything in the Cell with that type.
    ALWAYS RETURNS A LIST EVEN IF ONLY ONE OBJECT.
    """
    m = "getObjectsOfType:"
    if scope:
        #sop(m, "AdminConfig.list(%s, %s)" % ( repr(typename), repr(scope) ) )
        return _splitlines(AdminConfig.list(typename, scope))
    else:
        #sop(m, "AdminConfig.list(%s)" % ( repr(typename) ) )
        return _splitlines(AdminConfig.list(typename))

def getCfgItemId (scope, clusterName, nodeName, serverName, objectType, item):
    """Returns the config ID for the specified item of the specified type at the specified scope."""

    if (scope == "cell"):
        cellName = getCellName()
        cfgItemId = AdminConfig.getid("/Cell:"+cellName+"/"+objectType+":"+item)
    elif (scope == "node"):
        cfgItemId = AdminConfig.getid("/Node:"+nodeName+"/"+objectType+":"+item)
    elif (scope == "cluster"):
        cfgItemId = AdminConfig.getid("/ServerCluster:"+clusterName+"/"+objectType+":"+item)
    elif (scope == "server"):
        cfgItemId = AdminConfig.getid("/Node:"+nodeName+"/Server:"+serverName+"/"+objectType+":"+item)
    #endIf
    return cfgItemId

def isDefined(varname):
    """Return true if the variable with the given name is defined (bound) either locally
    or globally."""

    # This seems like it ought to work, but it doesn't always
    #return varname in locals().keys() or varname in globals().keys()

    # So try eval'ing the variable name and look for NameError
    try:
        x = eval(varname)
        return 1
    except NameError:
        return 0

############################################################
# wsadmin stuff
def isConnected():
    """Return true (1) if we're connected to a server.
    Return false (0) if we're running with conntype=NONE, in
    which case lots of things don't work so that's good to know"""

    # If you try to use AdminControl and we're not connected,
    # it fails and we can tell by catching the exception.
    try:
        conntype = AdminControl.getType()
        # Note: for some reason, if you put any other
        # except clauses in front of this one, this one will
        # fail to catch the exception.  Probably a bug somewhere.
    except:
        #print "Apparently not connected"
        return 0
    #print "conntype=%s" % conntype
    return 1

def getConnType():
    """return the connection type in use, e.g. "IPC", "SOAP",
    "NONE", ..."""
    # if not connected, getType fails but we know the conntype
    # is NONE
    if not isConnected():
        return "NONE"
    return AdminControl.getType()

############################################################
# cluster-related methods

def getServerClusterByName( name ):
    """Return the config object id for the named server cluster
    TODO: get rid of either this or getClusterId"""
    return getObjectByName( 'ServerCluster', name )

def getClusterId(clustername):
    """Return the config object id for the named server cluster.
    TODO: get rid of either this or getServerClusterByName"""
    return getServerClusterByName(clustername)

def createCluster( cellname, clustername, createReplicationDomain = False, nodeScopedRouting = False ):
    """Create a new cluster without a cluster member. Return its id.
    If createReplicationDomain is True, create a replication domain for it,
    and if nodeScopedRouting is True, enable node-scoped routing
    optimization within the cluster."""
    m = "createCluster:"
    sop(m,"Entry. cellname=%s clustername=%s createReplicationDomain=%s nodeScopedRouting=%s" % ( cellname, clustername, createReplicationDomain, nodeScopedRouting ))

    # Check input.
    if (False != createReplicationDomain and True != createReplicationDomain):
        raise m + " Error. createReplicationDomain must be True or False. createReplicationDomain=%s" % repr(createReplicationDomain)
    if (False != nodeScopedRouting and True != nodeScopedRouting):
        raise m + " Error. nodeScopedRouting must be True or False. nodeScopedRouting=%s" % repr(nodeScopedRouting)

    # Convert to a string value.
    preferLocal = nodeScopedRouting and 'true' or 'false'

    if createReplicationDomain == True:
        sop(m,'Calling AdminTask.createCluster([-clusterConfig [-clusterName %s -preferLocal %s] -replicationDomain [-createDomain true]]' % (clustername, preferLocal))
        return AdminTask.createCluster('[-clusterConfig [-clusterName %s -preferLocal %s] -replicationDomain [-createDomain true]]' % (clustername, preferLocal))
    else:
        sop(m,'Calling AdminTask.createCluster([-clusterConfig [-clusterName %s -preferLocal %s]]' % (clustername, preferLocal))
        return AdminTask.createCluster('[-clusterConfig [-clusterName %s -preferLocal %s]]' % (clustername, preferLocal))

def createServerInCluster( clustername, nodename, servername, sessionReplication = False):
    """Create a new server in a cluster, return its id.
    Turn on session replication if sessionReplication is True"""
    m = "createServerInCluster:"
    sop(m,"Entry. clustername=%s nodename=%s servername=%s sessionReplication=%s" % ( clustername, nodename, servername, sessionReplication ))
    if sessionReplication == True:
        sop(m,'Calling AdminTask.createClusterMember([-clusterName %s -memberConfig[-memberNode %s -memberName %s -memberWeight 2 -replicatorEntry true]])' % (clustername,nodename,servername))
        AdminTask.createClusterMember('[-clusterName %s -memberConfig[-memberNode %s -memberName %s -memberWeight 2 -replicatorEntry true]]' % (clustername,nodename,servername))
    else:
        sop(m,'Calling AdminTask.createClusterMember([-clusterName %s -memberConfig[-memberNode %s -memberName %s -memberWeight 2]])' % (clustername,nodename,servername))
        AdminTask.createClusterMember('[-clusterName %s -memberConfig[-memberNode %s -memberName %s -memberWeight 2]]' % (clustername,nodename,servername))

def deleteServerClusterByName( name ):
    """Delete the named server cluster"""
    m = "deleteServerClusterByName:"
    sid = getServerClusterByName( name )
    if not sid:
        raise m + " Could not find cluster %s to delete" % name
    stopCluster( name )
    sop(m,"remove cluster: %s" % name)
    AdminConfig.remove( sid )

def listServerClusters():
    """Return list of names of server clusters"""
    cluster_ids = _splitlines(AdminConfig.list( 'ServerCluster' ))
    cellname = getCellName()
    result = []
    for cluster_id in cluster_ids:
        result.append(AdminConfig.showAttribute(cluster_id,"name"))
    return result

def stopAllServerClusters():
    """Stop all server clusters"""
    clusternames = listServerClusters()
    for name in clusternames:
        stopCluster( name )

def startAllServerClusters():
    """Start all server clusters"""
    clusternames = listServerClusters()
    for name in clusternames:
        startCluster( name )

def deleteAllServerClusters():
    """Delete all server clusters (including their servers)"""
    m = "deleteAllServerClusters:"
    stopAllServerClusters()
    clusternames = listServerClusters()
    for name in clusternames:
        sop(m,"Delete cluster: %s" % name)
        id = getClusterId(name)
        AdminConfig.remove( id )

def stopCluster( clustername ):
    """Stop the named server cluster"""
    m = "stopCluster:"
    sop(m,"Stop cluster %s" % clustername)
    cellname = getCellName()    # e.g. 'poir1Cell01'
    cluster = AdminControl.completeObjectName( 'cell=%s,type=Cluster,name=%s,*' % ( cellname, clustername ) )
    if cluster == '':
        return None
    state = AdminControl.getAttribute( cluster, 'state' )
    if state != 'websphere.cluster.partial.stop' and state != 'websphere.cluster.stopped':
        AdminControl.invoke( cluster, 'stop' )
    # Wait for it to stop
    maxwait = 300  # wait about 5 minutes at most
    count = 0
    sop(m,"wait for cluster %s to stop" % clustername)
    while state != 'websphere.cluster.stopped':
        time.sleep( 30 )
        state = AdminControl.getAttribute( cluster, 'state' )
        sop(m,"state of %s: %s" % ( clustername, state ))
        count += 1
        if count > ( maxwait / 30 ):
            sop(m,"Giving up")
            break

def startCluster( clustername ):
    """Start the named server cluster"""
    m = "startCluster:"
    sop(m,"Start cluster %s" % clustername)
    cellname = getCellName()    # e.g. 'poir1Cell01'
    cluster = AdminControl.completeObjectName( 'cell=%s,type=Cluster,name=%s,*' % ( cellname, clustername ) )
    state = AdminControl.getAttribute( cluster, 'state' )
    if state != 'websphere.cluster.partial.start' and state != 'websphere.cluster.running':
        AdminControl.invoke( cluster, 'start' )
    # Wait for it to start
    maxwait = 300  # wait about 5 minutes at most
    count = 0
    sop(m,"wait for cluster %s to start" % clustername)
    while state != 'websphere.cluster.running':
        time.sleep( 30 )
        state = AdminControl.getAttribute( cluster, 'state' )
        sop(m,"state of %s: %s" % ( clustername, state ))
        count += 1
        if count > ( maxwait / 30 ):
            sop(m,"Giving up")
            break

def listServersInCluster(clusterName):
    """Return a list of all servers (members) that are in the specified cluster"""
    m = "listServersInCluster:"
    sop(m,"clusterName = %s" % clusterName)
    clusterId = AdminConfig.getid("/ServerCluster:" + clusterName + "/")
    clusterMembers = _splitlines(AdminConfig.list("ClusterMember", clusterId ))
    return clusterMembers

def startAllServersInCluster(clusterName):
    """Start all servers (members) that are in the specified cluster"""
    m = "startAllServersInCluster:"
    sop(m,"clusterName = %s" % clusterName)
    clusterMembers = listServersInCluster(clusterName)
    for clusterMember in clusterMembers:
        nodeName = AdminConfig.showAttribute( clusterMember, "nodeName" )
        serverName = AdminConfig.showAttribute( clusterMember, "memberName" )
        sop(m, "Starting Server %s on Node %s" % (serverName, nodeName ))
        startServer(nodeName, serverName)

def stopAllServersInCluster(clusterName):
    """Stop all servers (members) that are in the specified cluster"""
    m = "stopAllServersInCluster:"
    sop(m,"clusterName = %s" % clusterName)
    clusterMembers = listServersInCluster(clusterName)
    for clusterMember in clusterMembers:
        nodeName = AdminConfig.showAttribute( clusterMember, "nodeName" )
        serverName = AdminConfig.showAttribute( clusterMember, "memberName" )
        sop(m, "Stoping Server %s on Node %s" % (serverName, nodeName ))
        stopServer(nodeName, serverName)

def startAllListenerPortsInCluster(clusterName):
    """Start all Listener Ports that are defined in each server in a cluster."""
    m = "startAllListenerPortsInCluster:"
    sop(m,"clusterName = %s" % clusterName)
    clusterMembers = listServersInCluster(clusterName)
    for clusterMember in clusterMembers:
        nodeName = AdminConfig.showAttribute( clusterMember, "nodeName" )
        serverName = AdminConfig.showAttribute( clusterMember, "memberName" )
        sop(m, "Starting ListenerPorts on Server %s on Node %s" % (serverName, nodeName))
        lPorts = listListenerPortsOnServer( nodeName, serverName )
        for lPort in lPorts:
            sop(m, "Checking ListenerPort %s" % (lPort))
            state = AdminControl.getAttribute(lPort, 'started')
            if state == 'false':
                sop(m, "Starting ListenerPort %s" % (lPort))
                AdminControl.invoke(lPort, 'start')

def stopAllListenerPortsInCluster(clusterName):
    """Stop all Listener Ports that are defined in each server in a cluster."""
    m = "stopAllListenerPortsInCluster:"
    sop(m,"clusterName = %s" % clusterName)
    clusterMembers = listServersInCluster(clusterName)
    for clusterMember in clusterMembers:
        nodeName = AdminConfig.showAttribute( clusterMember, "nodeName" )
        serverName = AdminConfig.showAttribute( clusterMember, "memberName" )
        sop(m, "Stoping ListenerPorts on Server %s on Node %s" % (serverName, nodeName))
        lPorts = listListenerPortsOnServer( nodeName, serverName )
        for lPort in lPorts:
            sop(m, "Checking ListenerPort %s" % (lPort))
            state = AdminControl.getAttribute(lPort, 'started')
            if state == 'true':
                sop(m, "Stoping ListenerPort %s" % (lPort))
                AdminControl.invoke(lPort, 'stop')

def setInitialStateOfAllListenerPortsInCluster(clusterName, state):
    """Set the initial state of all Listener Ports that are defined in each server in a cluster."""
    # state is STOP or START
    m = "setInitialStateOfAllListenerPortsInCluster:"
    sop(m, "clusterName = %s, state = %s" % (clusterName, state) )
    serverIDList = getServerIDsForClusters([clusterName])
    if not serverIDList:
        raise m + " Error: Could not find any servers in the cluster. clusterName=%s" % (clusterName,)
    for (serverID, nodeName, serverName) in serverIDList:
        sop(m, "Setting Initial State of ListenerPorts on Server %s on Node %s to %s" % (serverName, nodeName, state))
        lPorts = getObjectsOfType('ListenerPort', serverID)
        if not lPorts:
            raise m + " Error: Could not find any ListenerPorts in the server. nodeName=%s serverName=%s" % (nodeName, serverName)
        for lPort in lPorts:
            sop(m, "Setting ListenerPort %s initial state to %s" % (lPort, state))
            stateManagement = AdminConfig.showAttribute( lPort, "stateManagement" )
            AdminConfig.modify( stateManagement, [['initialState', state]] )

############################################################
# generic_server_cluster - related methods

def getGenericServerClusterIdByName( name ):
    """Return the config object id for the named server GenericServerCluster"""
    return getObjectByName( 'GenericServerCluster', name )

def createGenericServerCluster( clustername, protocol ):
    """Create a new generic server cluster without a cluster member. Return its id"""
    m = "createGenericServerCluster:"
    cellname = getCellName()
    sop(m,"Creating GSC. clustername=" + clustername + " protocol=" + protocol)
    return AdminConfig.create( 'GenericServerCluster', getCellId(cellname), [['name', clustername], ['protocol', protocol]] )

def listGenericServerClusters():
    """Return list of names of generic server clusters"""
    return getObjectNameList( 'GenericServerCluster' )

def deleteAllGenericServerClusters():
    """Delete all generic server clusters"""
    deleteAllObjectsByName( 'GenericServerCluster' )

############################################################
# generic_server_endpoint - related methods

def getGenericServerEndpointIdByName( name ):
    """Return the config object id for the named GenericServerEndpoint."""
    return getObjectByName( 'GenericServerEndpoint', name )

def createGenericServerEndpoint( generic_server_cluster, host, port, weight ):
    """Create a new GenericServerEndpoint under the specified GenericServerCluster. Return its id"""
    return AdminConfig.create( 'GenericServerEndpoint', generic_server_cluster, [['host', host], ['port', port], ['weight', weight]] )

def deleteAllGenericServerEndpoints():
    """Deletes all GenericServerEndpoints."""
    deleteAllObjectsByName( 'GenericServerEndpoint' )

def listGenericServerEndpoints():
    """Return list of names of generic server endpoints"""
    return getObjectNameList( 'GenericServerEndpoint' )

############################################################
# URIGroup - related methods

def getURIGroupIdByName( name ):
    """Return the config object id for the named URIGroup."""
    return getObjectByName( 'URIGroup', name )

def createURIGroup( name, pattern ):
    """Create a new URIGroup. Return its id"""
    m = "createURIGroup:"
    sop(m,"name=%s pattern=%s" % ( name, pattern, ))
    cellname = getCellName()
    return AdminConfig.create( 'URIGroup', getCellId(cellname), [['name', name], ['URIPattern', pattern]] )

def listURIGroups():
    """Returns list of names of all URIGroup objects"""
    return getObjectNameList( 'URIGroup' )

def deleteAllURIGroups():
    """Deletes all URIGroups."""
    deleteAllObjectsByName( 'URIGroup' )

############################################################
# server-related methods

#-------------------------------------------------------------------------------
# check if base or nd environment
#-------------------------------------------------------------------------------

def whatEnv():
    """Returns 'nd' if connected to a dmgr, 'base' if connected to
    an unmanaged server, and 'other' if connected to something else
    (which shouldn't happen but could)"""
    m = "whatEnv:"

    # Simpler version - should work whether connected or not
    servers = getObjectsOfType('Server')
    for server in servers:
        servertype = getObjectAttribute(server, 'serverType')
        if servertype == 'DEPLOYMENT_MANAGER':
            return 'nd'  # we have a deployment manager
    return 'base'  # no deployment manager, must be base

def createServer( nodename, servername, templatename = None, extraArgs = None ):
    """Create new app server, return its object id.
    Specify a templatename of 'DeveloperServer' for a development server.
    Specify extraArgs as a String in the format '-arg1 value1 -arg2 value2'.
    These will be appended to the AdminTask.createApplicationServer call."""

    # Append specified args to servername arg
    args = ['-name %s' % (servername)]
    if templatename:
        args.append('-templateName %s' % (templatename))
    if extraArgs:
        args.append(extraArgs)

    AdminTask.createApplicationServer( nodename, '[%s]' % ( ' '.join(args) ) )

    return getServerByNodeAndName( nodename, servername )

def getServerByNodeAndName( nodename, servername ):
    """Return config id for a server"""
    return getObjectByNodeAndName( nodename, 'Server', servername )

def getWebserverByNodeAndName( nodename, servername ):
    """Return config id for a webserver"""
    m = "getWebserverByNodeAndName: "
    sop(m,"Entry. nodename=%s servername=%s" % ( nodename, servername ))

    # For some unknown reason, this method does not find attribute 'name'. So do it ugly...
    # return getObjectByNodeAndName( nodename, 'WebServer', servername )

    node_id = getNodeId(nodename)
    sop(m,"node_id=%s" % ( node_id ))

    search_string = "%s/servers/%s|server.xml#WebServer" % ( nodename, servername )
    sop(m,"search_string=%s" % ( search_string ))

    server_id_list = _splitlines(AdminConfig.list( "WebServer", node_id ))
    for server_id in server_id_list:
        sop(m,"server_id=%s" % ( server_id ))
        if -1 != server_id.find(search_string):
            sop(m,"Exit. Found webserver.")
            return server_id
    sop(m,"Exit. Did not find webserver.")
    return None

def getApplicationServerByNodeAndName( nodename, servername ):
    """Return config id for an application server"""
    server_id = getServerByNodeAndName(nodename,servername)
    component_ids = AdminConfig.showAttribute(server_id, 'components')[1:-1].split(' ')
    for id in component_ids:
        i = id.find('#ApplicationServer')
        if i != -1:
            return id
    return None


def getServerIDsForClusters (clusterList):
    """This functions returns the config ID, node name, and server name for the servers in the specified
    clusters.

    Input:

      - clusterList - A list of cluster names.  The config IDs for all cluster
        members in all of the clusters in the list will be returned.  Only unique
        config IDs will be returned.

    Output:

      - A list of lists, where each element in the list is a list which consists of the following items:
          - the server's config ID
          - the server's node name
          - the server's name

        If the specified clusters do not exist or if the specified clusters do not contain
        any members, an empty list will be returned.
    """

    m = 'getServerIDsForClusters:'
    sop(m, 'Entering function')

    serverIDList = []

    # Verify that clusterList is indeed a list
    if type(clusterList) != type([]):
        sop(m, 'clusterList is not a list; raising exception')
        raise TypeError('getServerIDsForClusters only accepts a list as input')

    sop(m, 'Calling AdminConfig.list to get the list of clusters')
    clusters = _splitlines(AdminConfig.list ('ServerCluster'))
    sop(m, 'Got list of clusters')

    for inClusterName in clusterList:
        for cluster in clusters:

            sop(m, 'Calling AdminConfig.showAttribute to get the cluster name')
            thisClusterName = AdminConfig.showAttribute(cluster, 'name')
            sop(m, 'Got cluster name')

            if thisClusterName == inClusterName:

                sop(m, 'Calling AdminConfig.showAttribute to get the list of members for cluster %s' % thisClusterName)
                members = _splitlist(AdminConfig.showAttribute(cluster, 'members'))
                sop(m, 'Got list of members for cluster %s' % thisClusterName)

                for member in members:

                    sop(m, 'Calling AdminConfig.showAttribute to get the server name for the cluster member')
                    serverName = AdminConfig.showAttribute(member, 'memberName')
                    sop(m, 'Got the server name ("%s") for the cluster member' % serverName)

                    sop(m, 'Calling AdminConfig.showAttribute to get the node name for cluster member %s' % serverName)
                    nodeName = AdminConfig.showAttribute(member, 'nodeName')
                    sop(m, 'Got the node name ("%s") for cluster member %s' % (nodeName, serverName))

                    sop(m, 'Calling getServerId() with nodeName=%s and serverName=%s' % (nodeName, serverName))
                    serverID = getServerId(nodeName, serverName)
                    sop(m, 'Returned from getServerId().  Returned serverID = %s' % serverID)

                    if serverID != None:
                        dup = 'false'
                        for currentServerID in serverIDList:
                            if currentServerID == serverID:
                                dup = 'true'
                                break
                            #endif
                        #endfor
                        if dup == 'false':
                            serverIDList.append( (serverID, nodeName, serverName) )
                            sop(m, 'Added config ID for server %s on node %s to output list' % (serverName, nodeName))
                        #endif
                    #endif
                #endfor
            #endif
        #endfor
    #endfor

    sop(m, 'Exiting function')
    return serverIDList
#enddef


def getServerIDsForAllAppServers ():
    """This functions returns the config ID, node name, and server name for all application servers
       in the cell.

    Input:

      - None

    Output:

      - A list of lists, where each element in the list is a list which consists of the following items:
          - the server's config ID
          - the server's node name
          - the server's name

        If there are no application servers in the cell, an empty list will be returned.
    """

    m = 'getServerIDsForAllAppServers:'
    sop(m, 'Entering function')

    serverIDList = []

    sop(m, 'Calling AdminConfig.list to get the config ID for the cell.')
    cell = AdminConfig.list("Cell")
    sop(m, 'Got the config ID for the cell.')

    sop(m, 'Calling AdminConfig.list to get the list of nodes.')
    nodes = _splitlines(AdminConfig.list('Node', cell))
    sop(m, 'Got the list of nodes.')

    for node in nodes:

        sop(m, 'Calling AdminConfig.showAttribute to get the node name')
        nodeName = AdminConfig.showAttribute(node, 'name')
        sop(m, 'Got the node name ("%s")' % nodeName)

        sop(m, 'Calling AdminConfig.list to get the list of servers.')
        servers = _splitlines(AdminConfig.list('Server', node))
        sop(m, 'Got the list of servers')

        for server in servers:

            sop(m, 'Calling AdminConfig.showAttribute to get the server name')
            serverName = AdminConfig.showAttribute(server, 'name')
            sop(m, 'Got server name ("%s")' % serverName)

            sop(m, 'Calling AdminConfig.showAttribute to get the server type')
            serverType = AdminConfig.showAttribute(server, 'serverType')
            sop(m, 'Got server type. Server type for server %s = %s.' % (serverName, serverType))

            if serverType == 'APPLICATION_SERVER':
                serverIDList.append( (server, nodeName, serverName) )
                sop(m, 'Added config ID for server %s on node %s to output list' % (serverName, nodeName))
            #endif
        #endfor
    #endfor

    sop(m, 'Exiting function')
    return serverIDList
#enddef

def deleteServerByNodeAndName( nodename, servername ):
    """Delete the named server - raises exception on error"""
    m = "deleteServerByNodeAndName:"
    sop(m,"Entry. nodename=%s servername=%s" % ( nodename, servername ))
    sid = getServerByNodeAndName( nodename, servername )
    if sid is None:
        raise m + " Error: Could not find server. nodename=%s servername=%s" % (nodename,servername)
    AdminTask.deleteServer( '[-serverName %s -nodeName %s ]' % ( servername, nodename ) )
    sop(m, "Exit.")

def deleteServersOfType( typeToDelete ):
    """Delete all servers of the given type.
    Typical type values are 'APPLICATION_SERVER' and 'PROXY_SERVER'.
    Raises exception on error (passes through exception from stopServer())"""
    # Go through one node at a time - can't figure out any way to
    # find out what node a server is in from the Server or ServerEntry
    # object
    m = "deleteServersOfType:"
    nodes = _splitlines(AdminConfig.list( 'Node' ))
    cellname = getCellName()
    for node_id in nodes:
        nodename = getNodeName(node_id)
        serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))
        for serverEntry in serverEntries:
            sName = AdminConfig.showAttribute( serverEntry, "serverName" )
            sType = AdminConfig.showAttribute( serverEntry, "serverType" )
            if sType == typeToDelete:
                sid = AdminConfig.getid( "/Cell:%s/Node:%s/Server:%s/" % ( cellname, nodename, sName ) )
                sop(m,"Deleting server %s with sid %s" % ( sName, sid ))
                stopServer( nodename, sName )

                AdminTask.deleteServer( '[-serverName %s -nodeName %s ]' % ( sName, nodename ) )
            else:
                sop(m,"Not deleting server %s on node %s - has type %s instead of %s" % (sName,nodename,sType,typeToDelete))

def deleteAllApplicationServers():
    """Delete all application servers - raises exception on error"""
    deleteServersOfType( 'APPLICATION_SERVER' )

def deleteAllProxyServers():
    """Delete all proxy servers - raises exception on error"""
    deleteServersOfType( "PROXY_SERVER" )

def listAllServersProxiesLast():
    """return a list of all servers, EXCEPT node agents or
    deployment managers, as a list of lists, with all proxies at the end of the list.
    E.g. [['nodename','proxyname'], ['nodename','proxyname']].
    Typical usage:
    for (nodename,servername) in listAllServers():
        callSomething(nodename,servername)
        """
    m = "listAllServersProxiesLast:"
    all = listServersOfType(None)
    proxies = []
    result = []
    for (nodename,servername) in all:
        stype = getServerType(nodename,servername)
        # sometimes, dmgr has no type... who knows why
        if stype != None and stype == 'PROXY_SERVER':
            #sop(m,"Saving proxy in proxies %s %s" % ( nodename,servername ))
            proxies.append( [nodename,servername] )
        else:
            if stype != None and stype != 'DEPLOYMENT_MANAGER' and stype != 'NODE_AGENT':
                #sop(m,"Saving non-proxy in result %s %s" % ( nodename,servername ))
                result.append( [nodename,servername] )

    for (nodename,servername) in proxies:
        #stype = getServerType(nodename,servername)
        #sop(m,"listAllServersProxiesLast: Adding proxy to result: nodename=%s/servername=%s: stype=%s" % (nodename,servername,stype))
        result.append( [nodename,servername] )

    return result

def listAllAppServers():
    """return a list of all servers, EXCEPT node agents,
    deployment managers or webservers as a list of lists.
    E.g. [['nodename','proxyname'], ['nodename','proxyname']].
    Typical usage:
    for (nodename,servername) in listAllAppServers():
        callSomething(nodename,servername)
        """
    m = "listAllAppServers:"
    all = listServersOfType(None)
    result = []
    for (nodename,servername) in all:
        stype = getServerType(nodename,servername)
        # sometimes, dmgr has no type... who knows why
        if stype != None and stype != 'DEPLOYMENT_MANAGER' and stype != 'NODE_AGENT' and stype != 'WEB_SERVER':
            #sop(m,"%s/%s: %s" % (nodename,servername,stype))
            result.append( [nodename,servername] )
    return result

def listAllServers():
    """return a list of all servers, EXCEPT node agents or
    deployment managers, as a list of lists.
    E.g. [['nodename','proxyname'], ['nodename','proxyname']].
    Typical usage:
    for (nodename,servername) in listAllServers():
        callSomething(nodename,servername)
        """
    m = "listAllServers:"
    all = listServersOfType(None)
    result = []
    for (nodename,servername) in all:
        stype = getServerType(nodename,servername)
        # sometimes, dmgr has no type... who knows why
        if stype != None and stype != 'DEPLOYMENT_MANAGER' and stype != 'NODE_AGENT':
            #sop(m,"%s/%s: %s" % (nodename,servername,stype))
            result.append( [nodename,servername] )
    return result

def listServersOfType(typename):
    """return a list of servers of a given type as a list of lists.
    E.g. [['nodename','proxyname'], ['nodename','proxyname']].
    Typical usage:
    for (nodename,servername) in listServersOfType('PROXY_SERVER'):
        callSomething(nodename,servername)
    Set typename=None to return all servers.
        """
    # Go through one node at a time - can't figure out any way to
    # find out what node a server is in from the Server or ServerEntry
    # object
    result = []
    node_ids = _splitlines(AdminConfig.list( 'Node' ))
    cellname = getCellName()
    for node_id in node_ids:
        nodename = getNodeName(node_id)
        serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))
        for serverEntry in serverEntries:
            sName = AdminConfig.showAttribute( serverEntry, "serverName" )
            sType = AdminConfig.showAttribute( serverEntry, "serverType" )
            if typename == None or sType == typename:
                result.append([nodename, sName])
    return result

def getServerType(nodename,servername):
    """Get the type of the given server.
    E.g. 'APPLICATION_SERVER' or 'PROXY_SERVER'."""
    node_id = getNodeId(nodename)
    serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))
    for serverEntry in serverEntries:
        sName = AdminConfig.showAttribute( serverEntry, "serverName" )
        if sName == servername:
            return AdminConfig.showAttribute( serverEntry, "serverType" )
    return None

def listUnclusteredServers():
    """Return a list of app servers that don't belong to clusters, as a list of lists
    (see listServersOfType)"""
    allServers = listServersOfType('APPLICATION_SERVER')
    result = []
    for (nodename,servername) in allServers:
        server_id = getServerByNodeAndName(nodename,servername)
        clusterName = AdminConfig.showAttribute(server_id, 'clusterName')
        if clusterName == None:
            result.append([nodename,servername])
    return result

def startUnclusteredServers():
    """Start servers that aren't part of a cluster - raises exception on error"""
    serverlist = listUnclusteredServers()
    for (nodename,servername) in serverlist:
        startServer(nodename,servername)

def stopUnclusteredServers():
    """Stop servers that aren't part of a cluster - raises exception on error"""
    serverlist = listUnclusteredServers()
    for (nodename,servername) in serverlist:
        stopServer(nodename,servername)

def listProxyServers():
    """return a list of proxy servers as a list of lists.
    E.g. [['nodename','proxyname'], ['nodename','proxyname']].
    Typical usage:
    for (nodename,proxyname) in listProxyServers():
        callSomething(nodename,proxyname)
        """
    return listServersOfType("PROXY_SERVER")

def startAllProxyServers():
    """start proxies - raises exception on error"""
    proxylist = listProxyServers()
    for (nodename,proxyname) in proxylist:
        startServer(nodename,proxyname)

def stopAllProxyServers():
    """stop proxies - raises exception on error"""
    proxylist = listProxyServers()
    for (nodename,proxyname) in proxylist:
        stopServer(nodename,proxyname)

def stopAllServers(exceptfor=[]):
    """Stop every server, except node agent or deployment manager - raises exception on error
    If exceptfor is specified, it's a list of [nodename,servername]s to skip"""
    serverlist = listAllServers()
    for (nodename,servername) in serverlist:
        if [nodename,servername] not in exceptfor:
            stopServer(nodename,servername)

def startAllServers(exceptfor=[], waitAfterStartSeconds=0):
    """Start every server, except node agent or deployment manager - raises exception on error.
    If exceptfor is specified, it's a list of [nodename,servername]s to skip.
    Parameter waitAfterStartSeconds specifies time to wait between starting servers."""
    m = "startAllServers: "
    serverlist = listAllServersProxiesLast()
    for (nodename,servername) in serverlist:
        if [nodename,servername] not in exceptfor:
            startServer(nodename,servername)
            if waitAfterStartSeconds > 0:
                sop(m,"Sleeping %i seconds." % ( waitAfterStartSeconds ))
                time.sleep(waitAfterStartSeconds)

def isServerRunning(nodename,servername):
    """Returns a boolean to say if the server is running.
Not 100% accurate but should be close - relies on there only being an mbean for
    a server if it's running"""
    mbean = AdminControl.queryNames('type=Server,node=%s,name=%s,*' % (nodename,servername))
    if mbean:
        return True
    return False

def startWebServer( nodename, servername):
    """ Starts the specified webserver """
    m = "startWebServer: "
    sop(m,"Entry.")
    sop(m,"Got arguments: nodename=%s, servername=%s" % (nodename,servername))
    mbean = AdminControl.queryNames('WebSphere:type=WebServer,*')
    sop(m,mbean)
    cell = AdminControl.getCell()
    sop(m,cell)
    webServerUp = AdminControl.invoke(mbean,'start','[%s %s %s]' % (cell,nodename,servername),'[java.lang.String java.lang.String java.lang.String]')
    sop(m,webServerUp)

def stopWebServer( nodename, servername):
    """ Stops the specified webserver """
    m = "stopWebServer: "
    sop(m,"Entry.")
    sop(m,"Got arguments: nodename=%s, servername=%s" % (nodename,servername))
    mbean = AdminControl.queryNames('WebSphere:type=WebServer,*')
    sop(m,mbean)
    cell = AdminControl.getCell()
    sop(m,cell)
    webServerDown = AdminControl.invoke(mbean,'stop','[%s %s %s]' % (cell,nodename,servername),'[java.lang.String java.lang.String java.lang.String]')
    sop(m,webServerDown)

# Global variable defines extra time to wait for a server to start, in seconds.
waitForServerStartSecs = 300

def setWaitForServerStartSecs(val):
    """Sets global variable used to wait for servers to start, in seconds."""
    global waitForServerStartSecs
    waitForServerStartSecs = val

def getWaitForServerStartSecs():
    """Returns the global variable used to wait for servers to start, in seconds."""
    global waitForServerStartSecs
    return waitForServerStartSecs

def startServer( nodename, servername ):
    """Start the named server - raises exception on error.
    Uses global variable waitForServerStartSeconds"""
    # Check first if it's already running - if we try to start it
    # when it's running, we get an exception and sometimes the
    # try/except below doesn't catch it, I don't know why
    m = "startServer:"
    if isServerRunning(nodename,servername):
        sop(m,"server %s,%s is already running" % (nodename,servername))
    else:
        sop(m,"starting server %s,%s" % (nodename,servername))
        try:
            sop(m,"startServer(%s,%s)" % ( servername, nodename ))
            # optional the 3rd arg is seconds to wait for startup - the default
            # is 1200 (according to 6.1 infocenter) e.g. 20 minutes,
            # which you'd think would be enough for anybody...
            # But it actually doesn't seem to work that way, so put an explicit
            # and long wait time.
            # But if we put 3600, we get a SOAP timeout... going back
            # to 120 for now
            AdminControl.startServer( servername, nodename, 240)

            # Calculate the number of 15-second cycles to wait, minimum 1.
            global waitForServerStartSecs
            waitRetries = waitForServerStartSecs / 15
            if waitRetries < 1:
                waitRetries = 1
            retries = 0
            while not isServerRunning(nodename,servername) and retries < waitRetries:
                sop(m,"server %s,%s not running yet, waiting another 15 secs" % (nodename,servername))
                time.sleep(15)  # seconds
                retries += 1
            if not  isServerRunning(nodename,servername) :
                sop(m,"server %s,%s STILL not running, giving up" % (nodename,servername))
                raise Exception("SERVER FAILED TO START %s,%s" % (nodename,servername))

        except:
            # Fails if server already started - ignore it
            ( exception, parms, tback ) = sys.exc_info()
            if -1 != repr( parms ).find( "already running" ):
                return                      # ignore it
            # Some other error? scream and shout
            sop(m,"EXCEPTION STARTING SERVER %s" % servername)
            sop(m,"Exception=%s\nPARMS=%s" % ( str( exception ), repr( parms ) ))
            raise Exception("EXCEPTION STARTING SERVER %s: %s %s" % (servername, str(exception),str(parms)))

def stopServer( nodename, servername, immediate=False, terminate=False ):
    """Stop the named server - raises exception on error"""
    m = "stopServer:"
    if not isServerRunning(nodename,servername):
        sop(m,"server %s,%s is already stopped" % (nodename,servername))
    else:
        sop(m,"stopping server %s,%s immediate=%i terminate=%i" % (nodename,servername,immediate,terminate))
        try:
            if terminate:
                AdminControl.stopServer( servername, nodename, 'terminate' )
            elif immediate:
                AdminControl.stopServer( servername, nodename, 'immediate' )
            else:
                AdminControl.stopServer( servername, nodename )
            sop(m,"stop complete for server %s,%s" % (nodename,servername))
        except:
            # Fails if server not running - ignore it
            ( exception, parms, tback ) = sys.exc_info()
            if -1 != repr( parms ).find( "Unable to locate running server" ):
                return                      # ignore it
            # Some other error? scream and shout
            sop(m,"EXCEPTION STOPPING SERVER %s" % servername)
            sop(m,"Exception=%s\nPARMS=%s" % ( str( exception ), repr( parms ) ))
            raise Exception("EXCEPTION STOPPING SERVER %s: %s %s" % (servername, str(exception),str(parms)))

def listListenerPortsOnServer(nodeName, serverName):
    """List all of the Listener Ports on the specified Node/Server."""
    m = "listListenerPortsOnServer:"
    sop(m,"nodeName = %s, serverName = %s" % (nodeName, serverName))
    cellName = getCellName()    # e.g. 'xxxxCell01'
    lPorts = _splitlines(AdminControl.queryNames("type=ListenerPort,cell=%s,node=%s,process=%s,*" % (cellName, nodeName, serverName)))
    sop(m,"returning %s" % (lPorts))
    return lPorts

def getApplicationServerCustomProperty(nodename, servername, propname):
    """Return the VALUE of the specified custom property of the application server, or None if there is none by that name."""
    server = getApplicationServerByNodeAndName(nodename,servername)
    return getObjectCustomProperty(server,propname)

def setApplicationServerCustomProperty( nodename, servername, propname, propvalue ):
    """Sets the specified server setting custom property."""
    m = "setApplicationServerCustomProperty:"
    sop(m,"Entry. nodename=%s servername=%s propname=%s propvalue=%s" % ( repr(nodename), repr(servername), repr(propname), repr(propvalue), ))

    server_id = getApplicationServerByNodeAndName(nodename,servername)
    setCustomPropertyOnObject(server_id, propname, propvalue)
    sop(m,"Exit.")

def restartServer( nodename, servername, maxwaitseconds, ):
    """Restarts a server or proxy JVM

    This is useful to restart standalone servers after they have been configured.
    Raises an exception if the server is not already running.
    Waits up to the specified max number of seconds for the server to stop and restart.
    Returns True or False to indicate whether the server is running"""
    m = "restartServer: "
    sop(m,"Entry. nodename=%s servername=%s maxwaitseconds=%d" % (nodename, servername, maxwaitseconds, ))

    if not isServerRunning( nodename, servername ):
        raise m + "ERROR: Server is not already running. nodename=%s servername=%s" % (nodename, servername, )
    sop(m,"Server %s is running." % ( servername, ))

    # Get the server mbean
    serverObjectName = AdminControl.completeObjectName('type=Server,node=%s,process=%s,*' % ( nodename, servername ,))
    sop(m,"Invoking restart on server. serverObjectName=%s" % ( serverObjectName, ))

    # Restart the server.
    AdminControl.invoke(serverObjectName, 'restart')

    # Wait up to a max timeout if requested by the caller.
    elapsedtimeseconds = 0
    if maxwaitseconds > 0:
        sleeptimeseconds = 5

        # Phase 1 - Wait for server to stop (This can take 30 seconds on a reasonably fast linux intel box)
        isRunning = isServerRunning( nodename, servername )
        while isRunning and elapsedtimeseconds < maxwaitseconds:
            sop(m,"Waiting %d of %d seconds for %s to stop. isRunning=%s" % ( elapsedtimeseconds, maxwaitseconds, servername, isRunning, ))
            time.sleep( sleeptimeseconds )
            elapsedtimeseconds = elapsedtimeseconds + sleeptimeseconds
            isRunning = isServerRunning( nodename, servername )

        # Phase 2 - Wait for server to start (This can take another minute)
        while not isRunning and elapsedtimeseconds < maxwaitseconds:
            sop(m,"Waiting %d of %d seconds for %s to restart. isRunning=%s" % ( elapsedtimeseconds, maxwaitseconds, servername, isRunning, ))
            time.sleep( sleeptimeseconds )
            elapsedtimeseconds = elapsedtimeseconds + sleeptimeseconds
            isRunning = isServerRunning( nodename, servername )

    isRunning = isServerRunning( nodename, servername )
    sop(m,"Exit. nodename=%s servername=%s maxwaitseconds=%d elapsedtimeseconds=%d Returning isRunning=%s" % (nodename, servername, maxwaitseconds, elapsedtimeseconds, isRunning ))
    return isRunning

def extractConfigProperties( nodename, servername, propsfilename, ):
    """Converts the server configuration from xml files to a flat properties file"""
    m = "extractConfigProperties:"
    arglist = ['-propertiesFileName',propsfilename,'-configData','Node=%s:Server=%s' % ( nodename, servername )]
    sop(m,"Calling AdminTask.extractConfigProperties() with arglist=%s" % ( arglist ))
    return AdminTask.extractConfigProperties( arglist )

def applyConfigProperties( propsfilename, reportfilename ):
    """ Converts a flat properties config file into an xml configuration."""
    m = "applyConfigProperties:"
    argstring = "[-propertiesFileName %s -reportFileName %s]" % ( propsfilename, reportfilename )
    sop(m,"Calling AdminTask.applyConfigProperties() with argstring=%s" % ( argstring ))
    return AdminTask.applyConfigProperties( argstring )

############################################################
#
# WebSphere Process Server Support Functions.
#
# WebSphere Process Server supports Business Processes (BPEL) and Human Tasks.
# Some of these may been long running transactions (as in days/weeks/months).
# To support upgrades of applications with processes in place, they have
# introduced the concept of process templates, which can be versioned.
# To support multiple versions of the same template, they have introduced
# a selector, which is a 'valid from' date.
# These process templates will need to be stopped before they can be uninstalled
# even if the application server has been stopped.
############################################################

def smartQuote(stringToQuote):
    """Quote a string, allowing for strings that already are."""
    localCopy = stringToQuote
    if localCopy != None:
        # Check beginning
        if localCopy[0] != '"':
            localCopy = '"' + localCopy
        # Check end
        if localCopy[-1] != '"':
            localCopy = localCopy + '"'
    return localCopy

def listAllBusinessProcessTemplates():
    """List all of the Process Server Business Process Templates. There is no scope for this."""
    m = "listAllBusinessProcessTemplates:"
    lTemplates = _splitlines(AdminConfig.list("ProcessComponent"))
    sop(m,"returning templates = %s" % lTemplates)
    return lTemplates

def listAllBusinessProcessTemplatesForApplication(applicationName):
    """List all of the Process Server Business Process Templates for the specified EAR file/Application."""
    m = "listAllBusinessProcessTemplatesForApplication:"
    sop(m,"applicationName = %s" % applicationName)
    lApplicationTemplates = []
    lTemplates = listAllBusinessProcessTemplates()
    for template in lTemplates:
        sop(m, "Checking template %s:" % template)
        templ = str(template)
        if templ.find(applicationName) != -1:
            lApplicationTemplates.append(template)
    sop(m,"returning templates = %s" % lApplicationTemplates)
    return lApplicationTemplates

def stopAllBusinessProcessTemplatesForApplicationOnCluster(clusterName, applicationName, force = False):
    """Stop all of the Process Server Business Process Templates for the specified EAR file/Application."""
    m = "stopAllBusinessProcessTemplatesForApplicationOnCluster:"
    sop(m,"clusterName = %s, applicationName = %s, force = %s" % (clusterName, applicationName, force))
    clusterMembers = listServersInCluster(clusterName)
    for clusterMember in clusterMembers:
        nodeName = AdminConfig.showAttribute(clusterMember, "nodeName")
        serverName = AdminConfig.showAttribute(clusterMember, "memberName")
        stopAllBusinessProcessTemplatesForApplication(nodeName, serverName, applicationName, force)

def stopAllBusinessProcessTemplatesForApplication(nodeName, serverName, applicationName, force = False):
    """Stop all of the Process Server Business Process Templates for the specified EAR file/Application."""
    m = "stopAllBusinessProcessTemplatesForApplication:"
    sop(m,"nodeName = %s, serverName = %s, applicationName = %s, force = %s" % (nodeName, serverName, applicationName, force))
    processContainer = AdminControl.completeObjectName("name=ProcessContainer,mbeanIdentifier=ProcessContainer,type=ProcessContainer,node=%s,process=%s,*" % (nodeName, serverName))
    lTemplates = listAllBusinessProcessTemplatesForApplication(applicationName)
    sop(m,"Found Templates: %s" % lTemplates)
    for template in lTemplates:
        if force:
            sop(m, "Forceably stopping and deleting instances of process template %s on server %s on node %s:" % (template, serverName, nodeName))
        else:
            sop(m, "Stopping process template %s on server %s on node %s:" % (template, serverName, nodeName))
        sop(m, "Template details:\n%s\n" % AdminConfig.showall(template) )
        processContainer = AdminControl.completeObjectName("name=ProcessContainer,mbeanIdentifier=ProcessContainer,type=ProcessContainer,node=%s,process=%s,*" % (nodeName, serverName))
        # format of params: java.lang.String templateName, java.lang.Long validFrom
        templateName = AdminConfig.showAttribute(template, "name")
        validFrom = AdminConfig.showAttribute(template, "validFrom")
        # Params need to be quoted and space separated...
        params = "[" + smartQuote(templateName) + " " + smartQuote(validFrom) + "]"
        sop(m,"Params = %s" % params)
        if force:
            AdminControl.invoke(processContainer, "stopProcessTemplateAndDeleteInstancesForced", params)
        else:
            AdminControl.invoke(processContainer, "stopProcessTemplate", params)
        sop(m,"Changing template initial state to 'STOP'")
        stateManagement = AdminConfig.showAttribute( template, "stateManagement" )
        AdminConfig.modify( stateManagement, [['initialState', "STOP"]] )

############################################################
# proxy-related methods

def getProxyServerByNodeAndName( nodename, servername ):
    """return the config ID for the proxy server component of named proxy server"""
    m = "getProxyServerByNodeAndName:"
    sop(m,"Entry. nodename=%s servername=%s" % ( nodename, servername ))
    serverid = getServerByNodeAndName( nodename, servername )
    sop(m,"serverid=%s" % (serverid))
    result = None
    # Verify that serverid valid and is indeed a proxy server. The subtype 'ProxyServer'
    # is only found within proxy servers.
    if serverid is not None and getObjectAttribute(serverid, 'serverType') == 'PROXY_SERVER':
        # Retrieve the proxy server component, subtype 'ProxyServer'. There will be only one.
        result = getObjectsOfType('ProxyServer', scope = serverid)[0]
        sop(m,"Exit. Found proxy server component. result=%s" % (result))
    else:
        sop(m,"Exit. Proxy server component not found. result is None.")
    return result

def sopAdminTaskCreate(arg1, arg2, arg3):
    """For debug only"""
    sop("","==================================")
    sop("","AdminTask.create(%s, %s, %s)" % ( repr(arg1), repr(arg2), repr(arg3), ))

def createProxyServer( nodename, servername, templatename = None, extraArgs = None ):
    """Create new proxy server, return its object id.
    Specify extraArgs as a String in the format '-arg1 value1 -arg2 value2'.
    These will be appended to the AdminTask.createProxyServer call."""

    # Append specified args to servername arg
    args = ['-name %s' % (servername)]
    if templatename:
        args.append('-templateName %s' % (templatename))
    if extraArgs:
        args.append(extraArgs)

    return AdminTask.createProxyServer( nodename, '[%s]' % ( ' '.join(args) ) )

def createProxyServerWithTemplate( nodename, proxyname, templatename ):
    """Creates a new proxy server with the specified template, and returns its id.

    Some valid template names are: proxy_server, sip_proxy_server, and http_sip_proxy_server."""
    m = "createProxyServerWithTemplate:"
    sop(m,"Entry. nodename=%s proxyname=%s templatename=%s" % ( nodename, proxyname, templatename ))

    # We don't know what templates exist now or might in the future.  Just let the
    # proxy creation fail if the template name is bad.
    #if 'proxy_server' != templatename and 'sip_proxy_server' != templatename and 'http_sip_proxy_server' != templatename:
    #   raise m + " ERROR. Invalid templatename=%s. Valid template names are: proxy_server, sip_proxy_server, and http_sip_proxy_server." % ( templatename )
    return AdminTask.createProxyServer( nodename, ['-name', proxyname, '-templateName', templatename] )

def createProxyServerWithProtocols( nodename, proxyname, protocolnames ):
    """Creates a new proxy server with the specified protocols, and returns its id.

    Arg protocolnames is a space-delimited string strings, eg 'http', 'sip', or 'http sip' """
    m = "createProxyServerWithProtocols:"
    sop(m,"Entry. nodename=%s proxyname=%s protocolnames=%s" % ( nodename, proxyname, protocolnames ))

    # Error-check the protocols in the space-separated list.
    protocol_list = protocolnames.split(' ')
    for protocol in protocol_list:
        if 'http' != protocol and 'sip' != protocol:
            raise m + " ERROR. Invalid protocol name=%s. Valid protocol names are: http and sip." % ( protocol )

    argString = '[-name %s -selectProtocols [[[%s]]]]' % ( proxyname, protocolnames )
    sop(m,"Calling AdminTask.createProxyServer with nodename=%s and argString=%s" % ( nodename, argString ))
    return AdminTask.createProxyServer( nodename, argString )

def setSIPProxyDefaultCluster(nodename, servername, clustername):
    """Set the SIP proxy default cluster on the given proxy server"""
    sid = getSIPProxySettings(nodename,servername)
    AdminConfig.modify(sid, [['defaultClusterName',clustername]])

def setSIPProxyEnableAccessLog(nodename, servername, trueOrFalse):
    """Set the SIP proxy enable access log on or off on the given proxy server"""
    sid = getSIPProxySettings(nodename,servername)
    AdminConfig.modify(sid, [['enableAccessLog',trueOrFalse]])

def deleteProxyServerByNodeAndName( nodename, servername ):
    """Delete the named proxy server"""
    deleteServerByNodeAndName( nodename, servername )

def getSIPProxySettings( nodename, servername ):
    """Given a proxy server ID, return the ID of the SIPProxySettings object, or None if there is none"""
    m = "getSIPProxySettings:"
    proxy_server_id = getServerId(nodename,servername)
    sop(m,"proxy_server_id=%s" % proxy_server_id)

    # Modeled on getProxySettings, below
    proxy_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/SIPProxySettings:/' % ( nodename, servername ) )
    #sop(m,"Initial proxy_settings_id=%s" % ( repr(proxy_settings_id) ))
    #if proxy_settings_id == None:
    #    proxy_settings_id = createSIPProxySettings( nodename, servername )
    #sop(m,"Exit. proxy_settings_id=%s" % ( repr(proxy_settings_id) ))
    return proxy_settings_id

def setProxyConfig( appname, proxyconfig ):
    """Sets a DeployedObjectProxyConfig."""
    m = "setProxyConfig:"
    #sop(m,"setProxyConfig: appname=%s proxyconfig=%s" % ( appname, repr(proxyconfig) ))
    appFound = False

    deps = _splitlines(AdminConfig.list( 'Deployment' ))
    for dep_id in deps:
        #sop(m,"setProxyConfig: dep_id=%s" % ( repr(dep_id) ))
        if dep_id.startswith( appname ):
            sop(m,"Found app. Creating DeployedObjectProxyConfig.")
            appFound = True
            AdminConfig.create( 'DeployedObjectProxyConfig', dep_id, proxyconfig )

            break

    if not appFound:
        sop(m,"EXCEPTION: Did not find application. appname=%s" % (appname))
        raise Exception("setProxyConfig: EXCEPTION: Did not find application. appname=%s" % (appname))

    #sop(m,"setProxyConfig: Exit.")

def exportTargetTree():
    """Exports the cell's target tree from an ND dmgr to a file on the dmgr machine.

    This method produces a file which may be copied from a dmgr to a secure proxy,
    so that requests to the secure proxy may be forwarded to servers in the cell.
    Returns the fully-qualified filename of the config file (target.xml)."""
    m = "exportTargetTree:"
    sop(m,"Entry.")

    # Get a reference to the TargetTreeMbean running in the dmgr.
    mbean = AdminControl.queryNames('type=TargetTreeMbean,process=dmgr,*')
    sop(m,"mbean=%s" % (mbean))

    # Export the config.
    fqTargetFile = AdminControl.invoke( mbean, 'exportTargetTree' )

    sop(m,"Exit. Returning fqTargetFile=%s" % (fqTargetFile))
    return fqTargetFile

def exportTunnelTemplate(tunnelTemplateName,outputFileName):
    """Exports a cell's tunnel template from an ND dmgr to a file on the dmgr machine.

    This method produces a file which may be copied from a dmgr to a DMZ proxy,
    and imported using the importTunnelTemplate command.
    It is intended to enable dynamic routing over the CGBTunnel."""
    m = "exportTunnelTemplate:"
    sop(m,"Entry. tunnelTemplateName=%s outputFileName=%s" % (tunnelTemplateName,outputFileName))
    AdminTask.exportTunnelTemplate (['-tunnelTemplateName', tunnelTemplateName, '-outputFileName', outputFileName])
    sop(m,"Exit.")

def importTunnelTemplate (inputFileName, bridgeInterfaceNodeName, bridgeInterfaceServerName):
    """Imports a tunnel template file into a DMZ proxy.

    Intended to import a file which was exported from an ND cell
    using the exportTunnelTemplate command."""
    m = "importTunnelTemplate:"
    sop(m,"Entry. inputFileName=%s bridgeInterfaceNodeName=%s bridgeInterfaceServerName=%s" % (inputFileName, bridgeInterfaceNodeName, bridgeInterfaceServerName))
    AdminTask.importTunnelTemplate (['-inputFileName', inputFileName,
                                     '-bridgeInterfaceNodeName', bridgeInterfaceNodeName,
                                     '-bridgeInterfaceServerName',bridgeInterfaceServerName])
    sop(m,"Exit.")

def setServerAutoRestart( nodename, servername, autorestart ):
    """Sets whether the nodeagent will automatically restart a failed server.

    Specify autorestart='true' or 'false' (as a string)"""
    m = "setServerAutoRestart:"
    sop(m,"Entry. nodename=%s servername=%s autorestart=%s" % ( nodename, servername, autorestart ))
    if autorestart != "true" and autorestart != "false":
        raise m + " Invocation Error: autorestart must be 'true' or 'false'. autorestart=%s" % ( autorestart )
    server_id = getServerId(nodename,servername)
    if server_id == None:
        raise " Error: Could not find server. servername=%s nodename=%s" % (nodename,servername)
    sop(m,"server_id=%s" % server_id)
    monitors = getObjectsOfType('MonitoringPolicy', server_id)
    sop(m,"monitors=%s" % ( repr(monitors)) )
    if len(monitors) == 1:
        setObjectAttributes(monitors[0], autoRestart = "%s" % (autorestart))
    else:
        raise m + "ERROR Server has an unexpected number of monitor object(s). monitors=%s" % ( repr(monitors) )
    sop(m,"Exit.")

############################################################
# proxy-virtual-host related methods

def sopAdminConfigCreate(arg1, arg2, arg3):
    """For debug only"""
    sop("","==================================")
    sop("","AdminConfig.create(%s, %s, %s)" % ( repr(arg1), repr(arg2), repr(arg3), ))

def deleteAllProxyVirtualHostConfigs():
    """Deletes ProxyVirtualHostConfigs from proxy-settings.xml for all defined proxies."""
    # m = "deleteAllProxyVirtualHostConfigs:"
    proxylist = listProxyServers()
    for (nodename,proxyname) in proxylist:
        # sop(m,"Next proxy. nodename=%s proxyname=%s" % ( repr(nodename), repr(proxyname) ))
        deleteProxyVirtualHostConfig(nodename,proxyname)

def deleteProxyVirtualHostConfig( nodename, proxyname,):
    """Deletes a ProxyVirtualHostConfig object."""
    m = "deleteProxyVirtualHostConfig:"
    # sop(m,"Entry. nodename=%s proxyname=%s" % ( nodename, proxyname, ))

    proxy_id = AdminConfig.getid( '/Node:%s/Server:%s' % ( nodename, proxyname ) )
    # sop(m,"proxy_id=%s" % ( proxy_id, ))

    pvhc_id = AdminConfig.getid('/ProxyVirtualHostConfig:/')
    # sop(m,"pvhc_id=%s" % ( pvhc_id, ))
    if emptyString(pvhc_id):
        sop(m,"Nothing to do. ProxyVirtualHostConfig object does not exist. nodename=%s proxyname=%s" % ( nodename, proxyname, ))
    else:
        AdminConfig.remove( pvhc_id )
        sop(m,"Deleted ProxyVirtualHostConfig object. nodename=%s proxyname=%s pvhc_id=%s" % ( nodename, proxyname, pvhc_id, ))

    # sop(m,"Exit.")
    return pvhc_id

def getProxyVirtualHostConfig( nodename, proxyname,):
    """Gets or creates a ProxyVirtualHostConfig object."""
    m = "getProxyVirtualHostConfig:"
    sop(m,"Entry. nodename=%s proxyname=%s" % ( nodename, proxyname, ))

    proxy_id = AdminConfig.getid( '/Node:%s/Server:%s' % ( nodename, proxyname ) )
    sop(m,"proxy_id=%s" % ( proxy_id, ))

    pvhc_id = AdminConfig.getid('/ProxyVirtualHostConfig:/')
    sop(m,"pvhc_id=%s" % ( pvhc_id, ))
    if emptyString(pvhc_id):
        #sopAdminConfigCreate( 'ProxyVirtualHostConfig', proxy_id, [] )
        pvhc_id = AdminConfig.create( 'ProxyVirtualHostConfig', proxy_id, [] )
        sop(m,"Created new ProxyVirtualHostConfig object. pvhc_id=%s" % ( pvhc_id, ))
    else:
        sop(m,"Referenced existing ProxyVirtualHostConfig object. pvhc_id=%s" % ( pvhc_id, ))

    sop(m,"Exit. Returning pvhc_id=%s" % ( pvhc_id ))
    return pvhc_id

def checkProxyVirtualHostAction( action ):
    """Checks the contents of a dictionary for the required args"""
    m = "checkProxyVirtualHostAction: "
    sop(m,"Entry. action=%s" % ( action ))

    if 'name' not in action.keys() or emptyString(action['name']):
        raise m + "ERROR action dictionary is missing key 'name'. action=%s" % ( action )

    if 'type' not in action.keys() or emptyString(action['type']):
        raise m + "ERROR action dictionary is missing key 'type'. action=%s" % ( action )
    type = action['type']

    if "HTTPRequestHeaderAction" == type:
        if 'headerModifyAction' not in action.keys() or emptyString(action['headerModifyAction']):
            raise m + "ERROR: missing key in action.  action=%s" % ( action )
        headerModifyAction = action['headerModifyAction']
        if headerModifyAction == 'REMOVE':
            if 'headerName' not in action.keys() or emptyString(action['headerName']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'SET':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'EDIT':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValueExpression' not in action.keys() or emptyString(action['headerValueExpression']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'APPEND':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        else:
            raise m + "ERROR: headerModifyAction not recognized. action=%s" % ( action )
    elif "HTTPResponseHeaderAction" == type:
        headerModifyAction = action['headerModifyAction']
        if headerModifyAction == 'REMOVE':
            if 'headerName' not in action.keys() or emptyString(action['headerName']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'SET':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'EDIT':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValueExpression' not in action.keys() or emptyString(action['headerValueExpression']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        elif headerModifyAction == 'APPEND':
            if 'headerName' not in action.keys() or emptyString(action['headerName']) or \
               'headerValue' not in action.keys() or emptyString(action['headerValue']):
                raise m + "ERROR: missing key in action.  action=%s" % ( action )
        else:
            raise m + "ERROR: headerModifyAction not recognized. action=%s" % ( action )
    elif "HTTPRequestCompressionAction" == type or \
         "HTTPResponseCompressionAction" == type:
        if 'compressionType' not in action.keys() or emptyString(action['compressionType']):
            raise m + "ERROR: missing key in action.  action=%s" % ( action )
    elif "RoutingAction" == type:
        if 'routeType' not in action.keys() or emptyString(action['routeType']):
            raise m + "ERROR: missing key in action.  action=%s" % ( action )
        if 'LocalRoute' != action['routeType']:
            raise m + "ERROR: routeType not recognized. action=%s" % ( action )
    elif "RewritingAction" == type:
        sop(m,"Ok. No checking for now...")
    else:
        raise m + "ERROR: actionType not recognized. action=%s" % ( action )

    sop(m,"Exit. Success")

def createProxyVirtualHostAction( pvhc_id, action ):
    """Creates and returns a ProxyVirtualHost Action object."""
    m = "createProxyVirtualHostAction:"
    sop(m,"Entry. pvhc_id=%s action=%s" % ( pvhc_id, repr(action) ))

    # TODO: Verify the action object does not already exist.

    # Get the 'type' value from the action dictionary.
    actionType = action['type']

    # Temporarily remove the 'type' key-value pair from the action dictionary.
    # It is not recognized by the proxy.
    del action['type']
    # sop(m,"Removed action type from dictionary. actionType=%s action=%s" % ( actionType, repr(action) ))

    # If this is a routing action, temporarily remove the LocalRoute key:value string from the action dictionary.
    # This is done so that we can create the LocalRoute as an object below.
    routeType = None
    if 'RoutingAction' == actionType:
        routeType = action['routeType']
        del action['routeType']
        sop(m,"Plucked routeType from action dictionary. routeType=%s" % (routeType))

    # Create the action object.
    #sopAdminConfigCreate( actionType, pvhc_id, dictToList(action) )
    actionList = dictToList(action)
    # Note: We need to convert this list of list of strings into one huge string
    # for rewriting actions, in case any of the strings has a backslash in it.
    jaclString = listOfStringsToJaclString(actionList)
    action_id = AdminConfig.create( actionType, pvhc_id, jaclString )
    sop(m,"Created action. action_id=%s" % (action_id))

    # If this is a routing action, create a route object beneath the action object.
    if 'RoutingAction' == actionType:
        #sopAdminConfigCreate(routeType,action_id,[])
        route_id = AdminConfig.create(routeType,action_id,[])
        sop(m,"Created route object. route_id=%s" % (route_id))
        # Also restore the routeType key:value pair.
        action['routeType'] = routeType
        sop(m,"Restored routeType to action dictionary. action=%s" % (action))

    # Restore the 'type' key-value pair.
    action['type'] = actionType
    # sop(m,"Restored action type into dictionary. actionType=%s action=%s" % ( actionType, repr(action) ))

    if emptyString(action_id):
        raise m + "ERROR: Could not create ProxyVirtualHost Action object. action=%s" % ( action )
    sop(m,"Exit. action_id=%s" % ( action_id ))
    return action_id

def createProxyVirtualHostRuleExpression( pvhc_id, ruleName, ruleExpression, enabled_action_list ):
    """Creates and returns a ProxyVirtualHost ProxyRuleExpression object"""
    m = "createProxyVirtualHostRuleExpression:"
    sop(m,"Entry. pvhc_id=%s ruleName=%s ruleExpression=%s enabled_action_list=%s" % ( pvhc_id, ruleName, ruleExpression, enabled_action_list ))
    #sopAdminConfigCreate( 'ProxyRuleExpression', pvhc_id, [['name',ruleName],
    #                                                               ['expression',ruleExpression],
    #                                                               ['proxyActionRefs',action_list]] )  # 2007-0718-1935 Changed from proxyActions
    rule_id = AdminConfig.create( 'ProxyRuleExpression', pvhc_id, [['name',ruleName],
                                                                   ['expression',ruleExpression],
                                                                   ['enabledProxyActions',enabled_action_list]] )  # 2007-0718-1935 Changed from proxyActions # 2007-1016 Changed again.
    if emptyString(rule_id):
        raise m + "ERROR: Could not create ProxyRuleExpression object."
    sop(m,"Exit. rule_id=%s" % ( rule_id ))
    return rule_id

def createProxyVirtualHostObject( pvhc_id, pvh_hostname, pvh_port, rule_list):
    """Creates and returns a ProxyVirtualHost object."""
    m = "createProxyVirtualHostObject:"
    sop(m,"Entry. pvhc_id=%s pvh_hostname=%s pvh_port=%s rule_list=%s" % ( pvhc_id, pvh_hostname, pvh_port, rule_list))
    id = AdminConfig.create( 'ProxyVirtualHost', pvhc_id, [['virtualHostName',pvh_hostname],
                                                           ['virtualHostPort',pvh_port],
                                                           ['enabledProxyRuleExpressions',rule_list]],
                                                           'proxyVirtualHosts' )
    if emptyString(id):
        raise m + "ERROR: Could not create ProxyVirtualHost object."
    sop(m,"Exit. id=%s" % ( id ))
    return id

def createProxyVirtualHostSettings( pvh_id, pvh_settings, ):
    """Creates the ProxyVirtualHostSettings under a ProxyVirtualHost object."""
    m = "createProxyVirtualHostSettings:"
    sop(m,"Entry. pvh_id=%s pvh_settings=%s" % ( pvh_id, repr(pvh_settings), ))

    if None == pvh_settings:
        sop(m,"Exit. Nothing to do. pvh_settings is null.")
        return

    # Temporarily remove 'local_error_page_policy' from the pvh_settings dictionary.
    # It is not recognized by the proxy.
    lepp = None
    if 'local_error_page_policy' in pvh_settings.keys():
        lepp = pvh_settings['local_error_page_policy']
        sop(m,"lepp=%s" % ( repr(lepp) ))
        del pvh_settings['local_error_page_policy']

    # Temporarily remove 'custom_error_page_policy' from the pvh_settings dictionary.
    # It is not recognized by the proxy.
    cepp = None
    if 'custom_error_page_policy' in pvh_settings.keys():
        cepp = pvh_settings['custom_error_page_policy']
        del pvh_settings['custom_error_page_policy']

    # Create the ProxyVirtualHostSettings object.
    # Note: Each ProxyVirtualHost object can have exactly 1 ProxyVirtualHostSettings object.
    pvh_settings_list = dictToList(pvh_settings)
    sop(m,"pvh_settings_list=%s" % ( repr(pvh_settings_list) ))
    #sopAdminConfigCreate('ProxyVirtualHostSettings', pvh_id, pvh_settings_list)
    pvhs_id = AdminConfig.create('ProxyVirtualHostSettings', pvh_id, pvh_settings_list)

    # Create local error page policy
    if None != lepp:
        # Temporarily remove 'error_mappings' from the lepp dictionary.
        # It is not recognized by the proxy.
        error_mappings = None
        if 'error_mappings' in lepp.keys():
            error_mappings = lepp['error_mappings']
            sop(m,"error_mappings=%s" % ( repr(error_mappings) ))
            del lepp['error_mappings']

        # Now create the local error page policy object.
        lepp_list = dictToList(lepp)
        sop(m,"lepp_list=%s" % ( repr(lepp_list) ))
        #sopAdminConfigCreate('LocalErrorPagePolicy', pvhs_id, lepp_list)
        lepp_id = AdminConfig.create('LocalErrorPagePolicy', pvhs_id, lepp_list)

        # Create error mappings.
        if None != error_mappings:
            for error_mapping in error_mappings:
                sop(m,"error_mapping=%s" % ( repr(error_mapping) ))
                em_list = dictToList(error_mapping)
                sop(m,"em_list=%s" % ( repr(em_list) ))
                #sopAdminConfigCreate('ErrorMapping', lepp_id, em_list)
                em_id = AdminConfig.create('ErrorMapping', lepp_id, em_list)

        # Restore lepp for future use.
        if None != error_mappings:
            lepp['error_mappings'] = error_mappings

    # Create Custom Error Page Policy
    if None != cepp:
        cepp_list = dictToList(cepp)
        sop(m,"cepp_list=%s" % ( repr(cepp_list) ))
        #sopAdminConfigCreate('CustomErrorPagePolicy', pvhs_id, cepp_list)
        cepp_id = AdminConfig.create('CustomErrorPagePolicy', pvhs_id, cepp_list)
        sop(m,"cepp_id=%s" % ( cepp_id ))

    # Restore pvh_settings for future use.
    if None != lepp:
        pvh_settings['local_error_page_policy'] = lepp
    if None != cepp:
        pvh_settings['custom_error_page_policy'] = cepp

    sop(m,"Exit.")

def setProxyVirtualHost( nodename, proxyname, pvh_hostname, pvh_port, pvh_rule_expressions, pvh_settings ):
    """Sets the specified proxy virtual host settings."""
    m = "setProxyVirtualHost:"
    sop(m,"Entry. nodename=%s proxyname=%s pvh_hostname=%s pvh_port=%s pvh_rule_expressions=%s pvh_settings=%s" % ( nodename, proxyname, pvh_hostname, pvh_port, repr(pvh_rule_expressions), repr(pvh_settings), ))

    # Get the top-level virtual host config object.
    pvhc_id = getProxyVirtualHostConfig(nodename, proxyname)
    if None == pvhc_id:
        raise m + "ERROR: Could not get ProxyVirtualHostConfig object. nodename=%s proxyname=%s pvh_hostname=%s pvh_port=%s" % ( nodename, proxyname, pvh_hostname, pvh_port )

    # Create each rule and store them in a list.
    rule_list = []
    for rule_expression in pvh_rule_expressions:
        ruleName = rule_expression['name']
        ruleExpression = rule_expression['expression']
        enabledProxyActions = rule_expression['enabledProxyActions']
        sop(m,"Next rule_expression. ruleName=%s ruleExpression=%s" % ( ruleName, ruleExpression, ))
        # Create each action object and save them in a list.
        enabled_action_list = []
        for action in enabledProxyActions:
            # sop(m,"Next action. action=%s" % ( action ))
            actionName = action['name']
            checkProxyVirtualHostAction( action )
            action_id = createProxyVirtualHostAction( pvhc_id, action )
            sop(m,"action_id=%s" % ( action_id ))
            enabled_action_list.append(action_id)  # Changed this from actionName 2007-0718-1935
        rule_id = createProxyVirtualHostRuleExpression( pvhc_id, ruleName, ruleExpression, enabled_action_list )
        rule_list.append(rule_id)   # Changed this from ruleName 2007-0718-1935

    # Create the ProxyVirtualHost object.
    pvh_id = createProxyVirtualHostObject( pvhc_id, pvh_hostname, pvh_port, rule_list )

    # Add the pvh ID to the list of enabledProxyVirtualHosts.
    setObjectAttributes(pvhc_id, enabledProxyVirtualHosts = pvh_id)

    # Create ProxyVirtualHostSettings
    createProxyVirtualHostSettings( pvh_id, pvh_settings )
    sop(m,"Exit.")

############################################################
# http proxy specific methods

def createCustomErrorPagePolicy( nodename, servername, policy ):
    """Creates a CustomErrorPagePolicy object for the specified proxy."""
    m = "createCustomErrorPagePolicy:"
    #sop(m,"Entry. nodename=%s servername=%s policy=%s" % ( repr(nodename), repr(servername), repr(policy), ))
    proxy_settings_id = getProxySettings( nodename, servername )
    AdminConfig.create( 'CustomErrorPagePolicy', proxy_settings_id, policy )

def createRoutingAction( routing_rule_id, route_type, key, value ):
    """Creates a routing_action under the specified routing rule."""
    m = "createRoutingAction:"
    #sop(m,"Entry. routing_rule_id=%s route_type=%s key=%s value=%s" % ( repr(routing_rule_id), route_type, key, value ))
    return AdminConfig.create(  route_type, routing_rule_id, [[key, value]] )

def createRoutingRule( nodename, servername, rulename, virtualHostName, isEnabled, uriGroup,):
    """Creates a RoutingRule under the specified proxy server."""
    m = "createRoutingRule:"
    #sop(m,"Entry. nodename=%s servername=%s rulename=%s virtuelHostName=%s isEnabled=%s uriGroup=%s" % ( nodename, servername, rulename, virtualHostName, isEnabled, uriGroup))
    routing_policy_id = getRoutingPolicy( nodename, servername)
    return AdminConfig.create( 'RoutingRule', routing_policy_id, [['name', rulename], ['virtualHostName', virtualHostName], ['isEnabled', isEnabled], ['uriGroup', uriGroup]] )

def getRoutingPolicy( nodename, servername ):
    """Returns a RoutingPolicy object for the specified proxy.

    Creates a RoutingPolicy object for the specified ProxySettings if one does not exist."""
    m = "getRoutingPolicy:"
    #sop(m,"Entry. nodename=%s servername=%s" % ( repr(nodename), repr(servername) ))
    proxy_settings_id = getProxySettings( nodename, servername )
    routing_policy_id = AdminConfig.showAttribute( proxy_settings_id, "routingPolicy" )
    #sop(m,"Initial routing_policy_id=%s" % ( repr(routing_policy_id) ))
    if routing_policy_id == None:
        routing_policy_id = AdminConfig.create( 'RoutingPolicy', proxy_settings_id, [] )
    #sop(m,"Exit. routing_policy_id=%s" % ( repr(routing_policy_id) ))
    return routing_policy_id

def getRewritingPolicy( nodename, servername ):
    """Returns a RewritingPolicy object for the specified proxy.

    Creates a RewritingPolicy object for the specified ProxySettings if one does not exist."""
    m = "getRewritingPolicy:"
    sop(m,"Entry. nodename=%s servername=%s" % ( repr(nodename), repr(servername) ))
    proxy_settings_id = getProxySettings( nodename, servername )
    sop(m,"proxy_settings_id=%s" % ( proxy_settings_id ))
    rewriting_policy_id = AdminConfig.showAttribute( proxy_settings_id, "rewritingPolicy" )
    sop(m,"Initial rewriting_policy_id=%s" % ( repr(rewriting_policy_id) ))
    if rewriting_policy_id == None:
        rewriting_policy_id = AdminConfig.create( 'RewritingPolicy', proxy_settings_id, [] )
    sop(m,"Exit. rewriting_policy_id=%s" % ( repr(rewriting_policy_id) ))
    return rewriting_policy_id

def createRewritingRule( nodename, servername, fromURLPattern, toURLPattern, ):
    """Creates a RewritingRule under the specified proxy server."""
    m = "createRewritingRule:"
    sop(m,"Entry. nodename=%s servername=%s fromURLPattern=%s toURLPattern=%s" % ( nodename, servername, fromURLPattern, toURLPattern, ))
    rewriting_policy_id = getRewritingPolicy( nodename, servername )
    rewriting_rule_id = AdminConfig.create( 'RewritingRule', rewriting_policy_id, [['fromURLPattern', fromURLPattern], ['toURLPattern', toURLPattern]] )
    sop(m,"Exit. rewriting_rule_id=%s" % ( rewriting_rule_id ))
    return rewriting_rule_id

def getProxySettings( nodename, servername ):
    """Returns a ProxySettings object for the specified proxy.

    Creates a ProxySettings object for the specified proxy if one does not exist."""
    m = "getProxySettings:"
    #sop(m,"Entry. nodename=%s servername=%s" % ( repr(nodename), repr(servername) ))
    proxy_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/ProxySettings:/' % ( nodename, servername ) )
    #sop(m,"Initial proxy_settings_id=%s" % ( repr(proxy_settings_id) ))
    if proxy_settings_id == None or proxy_settings_id == '':
        proxy_settings_id = createProxySettings( nodename, servername )
    #sop(m,"Exit. proxy_settings_id=%s" % ( repr(proxy_settings_id) ))
    return proxy_settings_id

def createProxySettings( nodename, servername ):
    """Creates a ProxySettings object for the specified proxy."""
    m = "createProxySettings:"
    #sop(m,"Entry. nodename=%s servername=%s" % ( repr(nodename), repr(servername) ))
    proxy_server_id = AdminConfig.getid( '/Node:%s/Server:%s' % ( nodename, servername ) )
    #sop(m,"proxy_server_id=%s" % ( repr(proxy_server_id) ))
    proxy_settings_id = AdminConfig.create( 'ProxySettings', proxy_server_id, [['routingPolicy', '']] )

    #sop(m,"Exit. proxy_settings_id=%s" % ( repr(proxy_settings_id) ))
    return proxy_settings_id

def setProxySettings( nodename, servername, settingname, settingvalue ):
    """Sets the specified proxy setting."""
    m = "setProxySettings:"
    #sop(m,"Entry. nodename=%s servername=%s settingname=%s settingvalue=%s" % ( repr(nodename), repr(servername), repr(settingname), repr(settingvalue), ))
    proxy_settings_id = getProxySettings( nodename, servername )
    AdminConfig.modify( proxy_settings_id, [[settingname, settingvalue]])
    #sop(m,"Exit.")

def setProxySettingsCustomProperty( nodename, servername, propname, propvalue ):
    """Sets the specified proxy setting custom property."""
    m = "setProxySettingsCustomProperty:"
    sop(m,"Entry. nodename=%s servername=%s propname=%s propvalue=%s" % ( repr(nodename), repr(servername), repr(propname), repr(propvalue), ))
    proxy_settings_id = getProxySettings( nodename, servername )
    AdminConfig.modify( proxy_settings_id, [['properties', [[['name', propname], ['value', propvalue]]]]] )
    sop(m,"Exit.")

def listProxySettings():
    """Returns list of names of all ProxySettings objects"""
    return getObjectNameList( 'ProxySettings' )

def deleteAllProxySettings():
    """Deletes all ProxySettings objects."""
    m = "deleteAllProxySettings:"
    proxylist = listProxyServers()
    for (nodename,proxyname) in proxylist:
        #sop(m,"nodename=%s proxyname=%s" % ( repr(nodename), repr(proxyname) ))
        proxy_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/ProxySettings:/' % ( nodename, proxyname ) )
        if proxy_settings_id != None and proxy_settings_id != '':
            sop(m,"Deleting proxy_settings %s" % ( repr(proxy_settings_id) ))
            AdminConfig.remove( proxy_settings_id )

        # We do need a ProxySettings or some config things blow up - so create
        # a new one, with the default settings.
        createProxySettings(nodename,proxyname)

def verifyProxyProtocol( nodename, servername, protocol, ):
    """Verifies the proxy supports the protocol.

    Returns True if the proxy supports the protocol, otherwise False.
    Valid protocol strings are 'http' and 'sip'."""
    m = "verifyProxyProtocol:"
    sop(m,"Entry. nodename=%s servername=%s protocol=%s" % ( nodename, servername, protocol, ))

    if "sip" == protocol:
        proxy_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/SIPProxySettings:/' % ( nodename, servername ) )
    elif "http" == protocol:
        proxy_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/ProxySettings:/' % ( nodename, servername ) )
    else:
        raise m + "Error. Protocol must be http or sip. protocol=%s" % ( protocol )

    rc = False
    if proxy_settings_id != None:
        rc = True

    sop(m,"Exit. rc=%s" % ( rc ))
    return rc

############################################################
# dmz proxy specific methods

def isDmzProxySecurityLevelValid( level ):
    """Indicates whether the supplied level is valid for secure proxy security."""
    if (('high' == level) or ('medium' == level) or ('low' == level)):
        return True
    return False

def setDmzProxyOverallSecurityLevel( nodename, proxyname, level ):
    """Sets overall security level for a secure proxy using high, medium, or low."""
    m = "setDmzProxyOverallSecurityLevel:"
    sop(m,"Entry: nodename=%s proxyname=%s level=%s" % ( nodename, proxyname, level, ))
    # Check valid level
    if not isDmzProxySecurityLevelValid(level):
        raise "%s Error: Invalid level. level=%s" % ( m, level, )
    proxy_server_id = getServerId(nodename,proxyname)
    if proxy_server_id is None:
        raise Exception("ERROR: no such proxy: node %s name %s" % (nodename,proxyname))
    sop(m,"proxy_server_id=%s" % ( proxy_server_id ))
    # Sample: AdminTask.setServerSecurityLevel('Proxy1(cells/firehawkDmgrCell/nodes/firehawk/servers/Proxy1|server.xml)','-proxySecurityLevel high -managementSecurityLevel high')
    rc = AdminTask.setServerSecurityLevel( proxy_server_id, '-proxySecurityLevel %s ' %  level )
    sop(m,"Exit. rc=%s" % ( repr(rc), ))

def isDmzProxySecurityActionValueValid( action, value ):
    """Indicates whether the supplied action/value is valid for secure proxy security."""
    if 'routing' == action and ('static' == value or 'dynamic' == value):
        return True
    if 'jarVerification' == action and ('unsigned' == value or 'dynamic' == value):
        return True
    if 'errorPageHandling' == action and ('local' == value or 'remote' == value):
        return True
    if 'startupPermissions' == action and ('privileged' == value or 'unprivileged' == value):
        return True
    return False

def setDmzProxySecurityActionValue( nodename, proxyname, action, value ):
    """Sets  security value for a secure proxy using the individual action and value."""
    m = "setDmzProxySecurityActionValue:"
    sop(m,"Entry: nodename=%s proxyname=%s action=%s value=%s" % ( nodename, proxyname, action, value, ))
    # Check valid action and value
    if not isDmzProxySecurityActionValueValid( action, value ):
        raise "%s Error: Unrecognized action/value pair. action=%s value=%s." % ( m, action, value, )
    proxy_server_id = getServerId(nodename,proxyname)
    # sop(m,"proxy_server_id=%s" % ( proxy_server_id ))
    # Sample: AdminTask.setServerSecurityLevel('Proxy1(cells/firehawkDmgrCell/nodes/firehawk/servers/Proxy1|server.xml)','-proxySecurityLevel routing=static;errorpage=remote')
    AdminTask.setServerSecurityLevel( proxy_server_id, '-proxySecurityLevel %s=%s' % ( action, value ))
    # sop(m,"Exit.")

# Not implemented in the proxy API.
# def setDmzProxySecurityActionLevel( nodename, proxyname, action, level ):

def getDmzProxySecurityActionLevelOrValue( nodename, proxyname, action, detailsFormat, ):
    """Gets an individual security level or value. detailsFormat must be 'levels' or 'values'"""
    m = "getDmzProxySecurityActionLevelOrValue:"
    sop(m,"Entry: nodename=%s proxyname=%s action=%s detailsFormat=%s" % ( nodename, proxyname, action, detailsFormat, ))
    if 'levels' != detailsFormat and 'values' != detailsFormat:
        raise "%s Error: Unrecognized detailsFormat=%s Must be 'levels' or 'values'." % ( m, detailsFormat, )
    proxy_server_id = getServerId(nodename,proxyname)
    sop(m,"proxy_server_id=%s" % ( proxy_server_id ))
    propString = AdminTask.getServerSecurityLevel( proxy_server_id, '-proxyDetailsFormat %s' % ( detailsFormat ))
    sop(m,"propString=%s" % ( propString, ))
    # Convert string of properties into a dictionary of keys and values.
    dict = propsToDictionary(propString)
    sop(m,"dict=%s" % ( repr(dict), ))
    if action in dict.keys():
        rc = dict[action]
        sop(m,"Exit. Returning rc=%s for action=%s" % ( rc, action, ))
        return rc
    raise "%s Error: action was not found in security level response string. nodename=%s proxyname=%s action=%s detailsFormat=%s dict=%s" % ( m, nodename, proxyname, action, detailsFormat, dict )

def getDmzProxySecurityActionLevel( nodename, proxyname, action, ):
    """Gets an individual security level"""
    return getDmzProxySecurityActionLevelOrValue( nodename, proxyname, action, 'levels' )

def getDmzProxySecurityActionValue( nodename, proxyname, action, ):
    """Gets an individual security value"""
    return getDmzProxySecurityActionLevelOrValue( nodename, proxyname, action, 'values' )

def getDmzProxyOverallSecurityLevel( nodename, proxyname ):
    """Gets overall security level for a secure proxy."""
    return getDmzProxySecurityActionLevel( nodename, proxyname, 'proxySecurityLevel' )

def setDmzProxySecurityLevels( nodename, proxyname, dmz_security_levels ):
    """Testcase method to set security levels for a secure proxy."""
    m = "setDmzProxySecurityLevels:"
    sop(m,"Entry: nodename=%s proxyname=%s dmz_security_levels=%s" % ( nodename, proxyname, repr(dmz_security_levels) ))

    # Get scope - default or custom
    if not 'scope' in dmz_security_levels.keys():
        raise "%s Error: scope not defined in dmz_security_levels." % ( m, )
    scope = dmz_security_levels['scope']
    if 'default' != scope and 'custom' != scope:
        raise "%s Error: Invalid scope. scope=%s" % ( m, scope )
    sop(m,"scope=%s" % ( scope, ))

    # Get overall level.
    if not 'overall_level' in dmz_security_levels.keys():
        raise "%s Error: overall_level not defined in dmz_security_levels." % ( m, )
    overall_level = dmz_security_levels['overall_level']
    if not isDmzProxySecurityLevelValid( overall_level ):
        raise "%s Error: Invalid overall_level. overall_level=%s" % ( m, overall_level )
    sop(m,"overall_level=%s" % ( overall_level, ))

    # Get individual levels.
    if not 'individual_levels' in dmz_security_levels.keys():
        raise "%s Error: individual_levels not defined in dmz_security_levels." % ( m, )
    individual_levels = dmz_security_levels['individual_levels']
    sop(m,"individual_levels=%s" % ( repr(individual_levels), ))

    # Set security levels.
    if 'default' == scope:
        setDmzProxyOverallSecurityLevel( nodename, proxyname, overall_level )
    else:
        for individual_level in individual_levels:
            action = individual_level['action']
            level  = individual_level['level']
            value  = individual_level['value']
            sop(m,"action=%s level=%s value=%s" % ( action, level, value, ))
            setDmzProxySecurityActionValue( nodename, proxyname, action, value )
    sop(m,"Exit." % ( m ))

def verifyDmzProxySecurityLevels( nodename, proxyname, dmz_security_levels ):
    """Testcase method verifies security levels for a secure proxy.
    I.e. raises an exception if the security level settings for the proxy
    aren't the same as those passed in."""
    m = "verifyDmzProxySecurityLevels:"
    sop(m,"Entry: nodename=%s proxyname=%s dmz_security_levels=%s" % ( nodename, proxyname, repr(dmz_security_levels) ))

    # Verify overall security level matches that specified in the testcase config file.
    overall_level = dmz_security_levels['overall_level']
    actual_overall_level = getDmzProxyOverallSecurityLevel(nodename, proxyname )
    if overall_level != actual_overall_level:
        raise "%s Error: Unexpected overall_level. expected=%s actual=%s nodename=%s proxyname=%s" % ( m, overall_level, actual_overall_level, nodename, proxyname, )

    # Verify individual levels and values matches those specified in the testcase config file.
    individual_levels = dmz_security_levels['individual_levels']
    for individual_level in individual_levels:
        action = individual_level['action']
        level  = individual_level['level']
        value  = individual_level['value']
        actual_level = getDmzProxySecurityActionLevel( nodename, proxyname, action, )
        if level != actual_level:
            raise "%s Error: Unexpected level. expected=%s actual=%s nodename=%s proxyname=%s action=%s" % ( m, level, actual_level, nodename, proxyname, action, )
        actual_value = getDmzProxySecurityActionValue( nodename, proxyname, action, )
        if value != actual_value:
            raise "%s Error: Unexpected value. expected=%s actual=%s nodename=%s proxyname=%s action=%s" % ( m, value, actual_value, nodename, proxyname, action, )
    sop(m,"Exit.")

def setAndVerifyDmzProxySecurityLevels( nodename, proxyname, dmz_security_levels ):
    """Testcase method to set and verify security levels for a secure proxy."""
    m = "setAndVerifyDmzProxySecurityLevels:"
    sop(m,"Entry.")
    setDmzProxySecurityLevels( nodename, proxyname, dmz_security_levels )
    verifyDmzProxySecurityLevels( nodename, proxyname, dmz_security_levels )
    sop(m,"Exit.")

############################################################
# ObjectCacheInstance related methods

def getObjectCacheId( nodename, servername, objectcachename ):
    """Returns an ObjectCache ID for the specified ObjectCacheInstance."""
    return getObjectByNodeServerAndName( nodename, servername, 'ObjectCacheInstance', objectcachename )

def setObjectCacheSettings( nodename, servername, objectcachename, settingname, settingvalue ):
    """Sets the specified ObjectCacheInstance setting."""
    m = "setObjectCacheSettings:"
    #sop(m,"Entry. nodename=%s servername=%s objectcachename=%s settingname=%s settingvalue=%s" % ( repr(nodename), repr(servername), repr(objectcachename), repr(settingname), repr(settingvalue), ))
    objectcache_id = getObjectCacheId( nodename, servername, objectcachename )
    #sop(m,"objectcache_id=%s settingname=%s settingvalue=%s" % ( repr(objectcache_id), repr(settingname), repr(settingvalue), ))
    AdminConfig.modify( objectcache_id, [[settingname, settingvalue]])
    actualvalue = AdminConfig.showAttribute( objectcache_id, settingname )
    sop(m,"Exit. Set %s to %s on node %s server %s cache %s" % ( repr(settingname), repr(actualvalue), repr(nodename), repr(servername), repr(objectcachename), ))

def enableObjectCacheDiskOffload( nodename, servername, objectcachename ):
    """Convenience method enables disk offload."""
    setObjectCacheSettings( nodename, servername, objectcachename, "enableDiskOffload", "true" )

def disableObjectCacheDiskOffload( nodename, servername, objectcachename ):
    """Convenience method disables disk offload"""
    setObjectCacheSettings( nodename, servername, objectcachename, "enableDiskOffload", "false" )

def setObjectCacheOffloadLocation( nodename, servername, objectcachename, offloadlocation ):
    """Convenience method sets the offload location on disk"""
    setObjectCacheSettings( nodename, servername, objectcachename, "diskOffloadLocation", offloadlocation )

def _getObjectScope(objectid):
    """Intended for private use within wsadminlib only.
    Pick out the part of the config object ID between the '('
    and the '|' -- we're going to use it in createObjectCache to identify the scope
    of the object"""
    return objectid.split("(")[1].split("|")[0]

def _getCacheProviderAtScope(scopeobjectid):
    """Return the CacheProvider at the same scope as the given object.
    The given object should be a Cell, Node, Cluster, Server, etc..."""

    # FIXME!
    # We need to find the CacheProvider object at the same scope as
    # the given object.  There doesn't seem to be a good way to do that.
    # For now, look at the part of the config IDs between the "(" and the "|";
    # they should be the same in the scope object and the corresponding
    # CacheProvider object
    scope = _getObjectScope(scopeobjectid)
    found = False
    for cacheprovider in getObjectsOfType('CacheProvider'):
        if _getObjectScope(cacheprovider) == scope:
            return cacheprovider
    return None

def createObjectCache(scopeobjectid, name, jndiname):
    """Create a dynacache object cache instance.

    The scope object ID should be the object ID of the config object
    at the desired scope of the new cache instance.  For example,
    for cell scope, pass the Cell object; for node scope, the Node
    object; for cluster scope, the Cluster object, etc. etc.

    Name & jndiname seem to be arbitrary strings.  Name must be
    unique, or at least not the same as another object cache in the
    same scope, not sure which.

    Returns the new object cache instance's config id."""

    cacheprovider = _getCacheProviderAtScope(scopeobjectid)
    if None == cacheprovider:
        raise Exception("COULD NOT FIND CacheProvider at the same scope as %s" % scopeobjectid)

    return AdminTask.createObjectCacheInstance(cacheprovider, ["-name", name,"-jndiName", jndiname])

def createServletCache(scopeobjectid, name, jndiname):
    """Create a dynacache servlet cache instance.

    The scope object ID should be the object ID of the config object
    at the desired scope of the new cache instance.  For example,
    for cell scope, pass the Cell object; for node scope, the Node
    object; for cluster scope, the Cluster object, etc. etc.

    Name & jndiname seem to be arbitrary strings.

    Returns the new object cache instance's config id."""

    # We need to find the CacheProvider object at the same scope as
    # the given object.  There doesn't seem to be a good way to do that.
    # For now, look at the part of the config IDs between the "(" and the "|";
    # they should be the same in the scope object and the corresponding
    # CacheProvider object
    cacheprovider = _getCacheProviderAtScope(scopeobjectid)
    if None == cacheprovider:
        raise Exception("COULD NOT FIND CacheProvider at the same scope as %s" % scopeobjectid)

    return AdminTask.createServletCacheInstance(cacheprovider, ["-name", name,"-jndiName", jndiname])

############################################################
# DynaCache related methods
def getDynaCacheIDsInMemory():
    """Returns all valid cache ids for the specified cache."""
    cachename = 'proxy/DefaultCacheInstance'
    proxies = listProxyServers()
    cacheIds = []
    for (nodename, proxyname) in proxies:
        dynacache_mbean = getDynaCacheMbean(nodename, proxyname, 'DynaCache')
        cacheId.append(AdminControl.invoke(dynacache_mbean,'getCacheIDsInMemory','[%s .]' % (cachename)))
    return cacheIds

def invalidateDynaCacheByID(nodename, proxyname, id):
    """Invalidates a specified cache by its id."""
    cachename = 'proxy/DefaultCacheInstance'
    proxies = listProxyServers()
    bool = 'true'
    cids = []
    dynacache_mbean=getDynaCacheMbean(nodename, proxyname, 'DynaCache')
    cids.append(AdminControl.invoke(dynacache_mbean,'invalidateCacheIDs','[%s %s %s]' % (cachename,id,bool)))
    return cids

def getDynaCacheMbean( nodename, servername, dynacachename ):
    """Returns a DynaCache ID for the specified cache.
    Note: there's always a dynacache mbean at the dmgr, which you can
    get using processname=dmgr.
    If there are clustered servers, each of those servers will also
    have a dynacache mbean.
    """
    mbeans = AdminControl.queryNames( 'type=DynaCache,node=%s,process=%s,name=%s,*' % ( nodename, servername, dynacachename )).split("\n")
    if len(mbeans) > 1:
        # FIXME: IS THIS RIGHT?
        raise "ERROR - code assumption violated - found more than one DynaCache mbean - need to fix this code"
    if len(mbeans) == 0:
        return ''
    return mbeans[0]

def getDynaCacheInstanceNames( nodename, servername ):
    """Returns the names of all DynaCache instances within the specified server."""
    dynacache_mbean = getDynaCacheMbean( nodename, servername, 'DynaCache' )
    return AdminControl.invoke( dynacache_mbean, 'getCacheInstanceNames' )

def clearDynaCache( nodename, servername, dynacachename ):
    """Clears the specified cache."""
    m = "clearDynaCache:"
    dynacache_mbean = getDynaCacheMbean( nodename, servername, 'DynaCache' )
    if '' == dynacache_mbean:
        sop(m, "DID NOT FIND DYNACACHE MBEAN... POKING AROUND")
        foo = AdminControl.queryNames('type=DynaCache,*')
        sop(m, "all dynacaches in server = %s" % repr(foo))
        raise "ERROR: giving up since did not find cache"
    sop(m,"Clearing cache. node=%s server=%s cachename=%s dynacache_mbean=%s" % ( repr(nodename), repr(servername), repr(dynacachename), repr(dynacache_mbean) ))
    return AdminControl.invoke( dynacache_mbean, 'clearCache', dynacachename )

def getDynaCacheStatistic( nodename, servername, dynacachename, statname ):
    """Returns the specified statistic."""
    # TODO: The cache name is not used now. Figure out how to differentiate between caches.
    dynacache_mbean = getDynaCacheMbean( nodename, servername, 'DynaCache' )
    return AdminControl.invoke( dynacache_mbean, 'getCacheStatistics', statname )

def getMemoryCacheEntries( nodename, servername, dynacachename ):
    """Returns the number of entries in cache."""
    statname = 'MemoryCacheEntries'
    verboseresult = getDynaCacheStatistic( nodename, servername, dynacachename, statname )
    result = 'INDETERMINATE'
    if verboseresult.startswith( statname + '=' ):
        result = verboseresult[len( statname + '=' ):]
    return result

def getCacheHits( nodename, servername, dynacachename ):
    """Returns the number of entries in cache."""
    statname = 'CacheHits'
    verboseresult = getDynaCacheStatistic( nodename, servername, dynacachename, statname )
    result = 'INDETERMINATE'
    if verboseresult.startswith( statname + '=' ):
        result = verboseresult[len( statname + '=' ):]
    return result

def clearAllProxyCaches():
    """Convenience method clears the cache of all defined proxies."""
    m = "clearAllProxyCaches"
    sop(m,"ENTRY")
    cachename = 'proxy/DefaultCacheInstance'
    proxies = listProxyServers()
    for (nodename,proxyname) in proxies:
        proxy_settings_id = getProxySettings( nodename, proxyname )
        cache_instance_name = getObjectAttribute(proxy_settings_id, 'cacheInstanceName')
        if cache_instance_name != cachename:
            raise "ERROR: cache_instance_name %s is not %s" % (cache_instance_name, cachename)
        clearDynaCache( nodename, proxyname, cachename )

def setServletCaching( nodename, servername, enabled, ):
    """Enables servlet caching in the webcontainer for the specified server."""
    m = "setServletCaching:"
    #sop(m,"Entry. Setting servlet caching. nodename=%s servername=%s enabled=%s" % ( repr(nodename), repr(servername), repr(enabled) ))
    if enabled != 'true' and enabled != 'false':
        raise m + " Error: enabled=%s. enabled must be 'true' or 'false'." % ( repr(enabled) )
    server_id = getServerByNodeAndName( nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    webcontainer = AdminConfig.list('WebContainer', server_id)
    #sop(m,"webcontainer=%s " % ( repr(webcontainer), ))
    result = AdminConfig.modify(webcontainer, [['enableServletCaching', enabled]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def setNumAsyncTimerThreads( nodename, servername, numberThreads, ):
    """Setting number of async timer threads."""
    m = "setNumAsyncTimerThreads:"
    #sop(m,"Entry. Setting number of async timer threads. nodename=%s servername=%s numberThreads=%s" % ( repr(nodename), repr(servername), repr(numberThreads) ))
    server_id = getServerByNodeAndName( nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    webcontainer = AdminConfig.list('WebContainer', server_id)
    #sop(m,"webcontainer=%s " % ( repr(webcontainer), ))
    result = AdminConfig.modify(webcontainer, [['numberAsyncTimerThreads', numberThreads]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def setUseAsyncRunnableWorkManager( nodename, servername, useAsyncRunnableWorkManager, ):
    """Sets whether to use the work manager for async runnables."""
    m = "setUseAsyncRunnableWorkManager:"
    #sop(m,"Entry. Sets whether to use the work manager for async runnables. nodename=%s servername=%s useAsyncRunnableWorkManager=%s" % ( repr(nodename), repr(servername), repr(useAsyncRunnableWorkManager) ))
    server_id = getServerByNodeAndName( nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    webcontainer = AdminConfig.list('WebContainer', server_id)
    #sop(m,"webcontainer=%s " % ( repr(webcontainer), ))
    result = AdminConfig.modify(webcontainer, [['useAsyncRunnableWorkManager', useAsyncRunnableWorkManager]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def setPortletCaching( nodename, servername, enabled, ):
    """Enables portlet caching in the webcontainer for the specified server."""
    m = "setPortletCaching:"
    #sop(m,"Entry. Setting portlet caching. nodename=%s servername=%s enabled=%s" % ( repr(nodename), repr(servername), repr(enabled) ))
    if enabled != 'true' and enabled != 'false':
        raise m + " Error: enabled=%s. enabled must be 'true' or 'false'." % ( repr(enabled) )
    server_id = getServerByNodeAndName( nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    portletcontainer = AdminConfig.list('PortletContainer', server_id)
    #sop(m,"portletcontainer=%s " % ( repr(webcontainer), ))
    result = AdminConfig.modify(portletcontainer, [['enablePortletCaching', enabled]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def invalidateAllDynaCacheByIDs(node, server):
    """Invalidates all caches by id."""
    cacheIds = getDynaCacheIDsInMemory()
    size = len(cacheIds)
    invalCacheIDs = []
    tmp1 = []
    break1 = []
    break2 = []

    """Check if we have cacheIds to split."""
    if (size > 0 and cacheIds != ['']):

        """Split each element of cacheIds where ',' occurs."""
        for x in cacheIds:
            break1 = re.split('[,]+', x)

        """Search each element of break1 where '>' occurs."""
        for x in break1:
            bool = re.search('[>]', x)

            """If '>' occurs, then split that entry where '>' occurs and save it in a tmp array."""
            if(bool != None):
                break2 = re.split('[>]+', x)
                tmp1.append(break2[0])

        """Invalidate cache entries using the ids stored in the temporary array."""
        for id in tmp1:
            invalCacheIDs.append(invalidateDynaCacheByID(node,server,id))

def clearAllProxyCachesByID():
    """Convenience method clears the cache of all defined proxies by id."""
    cachename = 'proxy/DefaultCacheInstance'
    proxies = listProxyServers()
    for (nodename,proxyname) in proxies:
        invalidateAllDynaCacheByIDs(nodename, proxyname)

def enableCacheReplication(cacheobjectid, domainname, replicationType = 'PUSH'):
    """Enables data replication on any cache instance object, be it the default Dynacache object
    or an object or servlet cache instance."""
    m = "enableDynaCacheReplication:"

    #sop(m,"dynacache=%s " % ( repr(dynacache), ))
    AdminConfig.modify(cacheobjectid, [['enableCacheReplication', 'true'], ['replicationType', replicationType]])
    drssettings = getObjectAttribute(cacheobjectid, 'cacheReplication')
    if drssettings == None:
        AdminConfig.create('DRSSettings', cacheobjectid, [['messageBrokerDomainName', domainname]])
        # Note: creating the DRSSettings object automagically sets the cacheReplication attribute
        # of the cache object to refer to it
    else:
        setObjectAttributes(drssettings, messageBrokerDomainName = domainname)

def enableDynaCacheReplication(nodename, servername, domainname, replicationType = 'PUSH'):
    """Enables Data Replication for the dynacache service on the given server using a given replication type (defaults to PUSH)"""
    m = "enableDynaCacheReplication:"
    #sop(m,"Entry. nodename=%s servername=%s domainname=%s replicationType=%s" % ( repr(nodename), repr(servername), repr(domainname), replicationType ))
    server_id = getServerByNodeAndName(nodename, servername)
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    dynacache = AdminConfig.list('DynamicCache',  server_id)
    enableCacheReplication(dynacache, domainname, replicationType)

def enableEJBReplication(nodename, servername, domainname):
    """UNTESTED"""

    """Enables data replication for EJB stateful session beans on the given server.
    The data replication domain should already exist."""
    server_id = getServerByNodeAndName(nodename, servername)
    ejbcontainer = getObjectsOfType('EJBContainer', server_id)[0]  # should be just one
    setObjectAttributes(ejbcontainer, enableSFSBFailover = 'true')
    drssettings = getObjectAttribute(ejbcontainer, 'drsSettings')
    if drssettings == None:
        AdminConfig.create('DRSSettings', ejbcontainer, [['messageBrokerDomainName', domainname]])
    else:
        setObjectAttributes(drssettings, messageBrokerDomainName = domainname)

def enableEJBApplicationReplication(applicationname, domainname):
    """UNTESTED"""

    """Enables data replication for EJB stateful session beans on the given application.
    The data replication domain should already exist."""
    appconf = _getApplicationConfigObject(applicationname)
    setObjectAttributes(appconf, overrideDefaultDRSSettings = 'true')
    drssettings = getObjectAttribute(appconf, 'drsSettings')
    if drssettings == None:
        AdminConfig.create('DRSSettings', appconf, [['messageBrokerDomainName', domainname]])
        # Note: creating the DRSSettings object automagically sets the cacheReplication attribute
        # of the cache object to refer to it
    else:
        setObjectAttributes(drssettings, messageBrokerDomainName = domainname)

def setDiskOffload(nodename, servername, enabled):
    """Enables or disables dynacache offload to disk on the given server"""
    m = "setDiskOffload:"
    #sop(m,"Entry. nodename=%s servername=%s enabled=%s" % ( repr(nodename), repr(servername), repr(enabled) ))
    if enabled != 'true' and enabled != 'false':
        raise m + " Error: enabled=%s. enabled must be 'true' or 'false'." % ( repr(enabled) )
    server_id = getServerByNodeAndName(nodename, servername)
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    dynacache = AdminConfig.list('DynamicCache', server_id)
    #sop(m,"dynacache=%s " % ( repr(dynacache), ))
    AdminConfig.modify(dynacache, [['enableDiskOffload', enabled]])
    #sop(m,"Exit.")

def setDiskOffloadLocation(nodename, servername, location):
    """Enables or disables dynacache offload to disk on the given server"""
    m = "setDiskOffloadLocation:"
    #sop(m,"Entry. nodename=%s servername=%s enabled=%s" % ( repr(nodename),repr(servername), repr(enabled) ))
    server_id = getServerByNodeAndName(nodename, servername)
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    dynacache = AdminConfig.list('DynamicCache', server_id)
    #sop(m,"dynacache=%s " % ( repr(dynacache), ))
    AdminConfig.modify(dynacache, [['diskOffloadLocation', location]])
    #sop(m,"Exit.")

def setFlushToDisk(nodename, servername, enabled):
    """Enables or disables dynacache offload to disk on the given server"""
    m = "setFlushToDisk:"
    #sop(m,"Entry. nodename=%s servername=%s enabled=%s" % ( repr(nodename), repr(servername), repr(enabled) ))
    if enabled != 'true' and enabled != 'false':
        raise m + " Error: enabled=%s. enabled must be 'true' or 'false'." % ( repr(enabled) )
    server_id = getServerByNodeAndName(nodename, servername)
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    dynacache = AdminConfig.list('DynamicCache', server_id)
    #sop(m,"dynacache=%s " % ( repr(dynacache), ))
    AdminConfig.modify(dynacache, [['flushToDiskOnStop', enabled]])
    #sop(m,"Exit.")

############################################################
# webserver and plugin related methods

def deleteAllWebServers():
    """Delete all webserver definitions"""
    m = "deleteAllWebServers:"
    #sop(m,"Entry.")
    deleteServersOfType( "WEB_SERVER" )
    #sop(m,"Exit. ")

def createWebserver(servername, nodename, webPort, webInstallRoot, pluginInstallRoot,
                    configurationFile, webAppMapping, adminPort, adminUserID, adminPasswd):
    '''creates a Webserver using the specified parameters'''
    m = "createWebserver:"
    #sop(m,"Entry. ")
    creationString = '[-name %s -templateName IHS -serverConfig [-webPort %s -webInstallRoot %s -pluginInstallRoot %s -configurationFile %s -webAppMapping %s] -remoteServerConfig [-adminPort %s -adminUserID %s -adminPasswd %s]]' % (servername, webPort, webInstallRoot, pluginInstallRoot, configurationFile, webAppMapping, adminPort, adminUserID, adminPasswd)
    #sop(m,"creationString=%s " % creationString)
    AdminTask.createWebServer(nodename, creationString)
    #sop(m,"Exit. ")

def setProcessCustomProperty(process_id, propname, propvalue):
    """Sets or modifies a custom property on a specified process"""
    m = "setProcessCustomProperty: "
    sop(m,"Entry. process_id=%s propname=%s propvalue=%s" % (process_id, propname, propvalue))

    # Is property present already?
    properties = _splitlines(AdminConfig.list('Property', process_id))
    sop(m,"properties=%s" % ( repr(properties) ))

    for p in properties:
        sop(m,"p=%s" % ( repr(p) ))
        pname = AdminConfig.showAttribute(p, "name")
        if pname == propname:
            # Already exists, just change value
            AdminConfig.modify(p, [['value', propvalue]])
            sop(m,"Exit. Modified an existing property with the same name.")
            return
    # Does not exist, create and set
    p = AdminConfig.create('Property', process_id, [['name', propname],['value', propvalue]])
    sop(m,"Exit. Created a new property.")

def setWebserverCustomProperty(nodename, servername, propname, propvalue):
    """Sets or modifies a custom property on a webserver"""
    m = "setWebserverCustomProperty: "
    # sop(m, "Entry. Setting prop %s=%s" % (propname,propvalue))

    webserver_id = getWebserverByNodeAndName(nodename, servername)
    if None == webserver_id:
        raise m + "Error: Could not find id. webserver_id=%s" % ( webserver_id )
    # sop(m,"webserver_id=%s" % ( webserver_id ))

    setProcessCustomProperty(webserver_id, propname, propvalue)
    # sop(m,"Exit.")

def setESIInvalidationMonitor(servername, nodename, esiInvalidationMonitor):
    '''sets the esiInvalidationMonitor property for the webserver plugin'''
    m = "setESIInvalidationMonitor:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('PluginProperties', webserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['ESIInvalidationMonitor', esiInvalidationMonitor]])
    #sop(m,"Exit. ")

def generatePluginCfg(servername, nodename):
    '''generates and propogates the webserver plugin for the specified webserver'''
    m = "generatePluginCfg:"
    #sop(m,"Entry. ")
    plgGen = AdminControl.queryNames('type=PluginCfgGenerator,*')
    #sop(m,"plgGen=%s " % plgGen)
    ihsnode = nodename
    nodename = getDmgrNodeName()
    configDir = os.path.join(getWasProfileRoot(nodename), 'config')
    if getNodePlatformOS(nodename) == 'windows':
        configDir = configDir.replace('/','\\')
    #sop(m,"configDir=%s " % configDir)
    AdminControl.invoke(plgGen, 'generate', '[%s %s %s %s true]' % (
                       configDir, getCellName(), ihsnode, servername),
                       '[java.lang.String java.lang.String java.lang.String java.lang.String java.lang.Boolean]')
    #sop(m,"Exit. ")

def setServerIOTimeout(servername, nodename, timeout):
    '''sets the ServerIOTimeout property for the specified appserver'''
    m = "setServerIOTimeout:"
    #sop(m,"Entry. ")
    appserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('WebserverPluginSettings', appserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['ServerIOTimeout', timeout ]])
    #sop(m,"Exit. ")

def setConnectTimeout(servername, nodename, timeout):
    '''sets the ConnectTimeout property for the specified appserver'''
    m = "setConnectTimeout:"
    #sop(m,"Entry. ")
    appserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('WebserverPluginSettings', appserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['ConnectTimeout', timeout ]])
    #sop(m,"Exit. ")

def setRetryInterval(servername, nodename, retryInterval):
    '''sets the RetryInterval property for the cluster of the specified appserver'''
    m = "setRetryInterval:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgClusterProps = AdminConfig.list('PluginServerClusterProperties', webserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgClusterProps, [['RetryInterval', retryInterval ]])
    #sop(m,"Exit. ")

def setRefreshInterval(servername, nodename, interval):
    '''sets the RefreshInterval property for the webserver plugin'''
    m = "setRefreshInterval:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('PluginProperties', webserver)
    AdminConfig.modify(plgProps, [['RefreshInterval', interval]])
    #sop(m,"plgProps=%s " % plgProps)
    #sop(m,"Exit. ")

def pluginESIEnable(servername, nodename, cacheSize):
    '''enables ESI for the webserver plugin, cacheSize in kilobytes'''
    m = "pluginESIEnable:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('PluginProperties', webserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['ESIEnable', "true"]])
    AdminConfig.modify(plgProps, [['ESIMaxCacheSize', cacheSize ]])
    #sop(m,"Exit. ")

def pluginESIDisable(servername, nodename):
    '''enables ESI for the webserver plugin'''
    m = "pluginESIDisable:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('PluginProperties', webserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['ESIEnable', "false"]])
    #sop(m,"Exit. ")

def setPluginLogLevel(servername, nodename, level):
    '''Sets the WebSphere Plugin LogLevel= parm (ERROR, TRACE, etc)'''
    m = "setPluginLogLevel:"
    #sop(m,"Entry. ")
    webserver = getServerByNodeAndName(nodename, servername)
    #sop(m,"webserver=%s " % webserver)
    plgProps = AdminConfig.list('PluginProperties', webserver)
    #sop(m,"plgProps=%s " % plgProps)
    AdminConfig.modify(plgProps, [['LogLevel', level ]])
    #sop(m,"Exit. ")

def setWebServerPluginProp(nodename, servername, pName, pValue):
    '''sets a web server plugin custom property'''
    m = "setWebServerPluginProp:"
    sop(m,"Entry.")
    webserver = getServerByNodeAndName(nodename, servername)
    sop(m,"webserver=%s" % (webserver))
    # If we found a valid Server config id and the server is indeed a web server...
    if webserver is not None and getObjectAttribute(webserver, 'serverType') == 'WEB_SERVER':
       plgProps = getObjectsOfType('PluginProperties', scope = webserver)[0] #There will be only one
       sop(m,"plgProps=%s" % (plgProps))
       setCustomPropertyOnObject(plgProps, pName, pValue)
       sop(m,"Exit.")
    else:
       raise m + " ERROR: server is not valid. nodename=%s servername=%s" % (nodename, servername)


############################################################
# CEA related methods

def getServerCEASettingsId(nodename, servername):
    """Returns the Object ID for CEA Settings in a server, or None"""
    m = "getServerCEASettingsId:"
    #sop(m,"Entry. nodename=%s servername=%s" % (nodename, servername))
    cea_settings_id = AdminConfig.getid( '/Node:%s/Server:%s/CEASettings:/' % ( nodename, servername ) )
    #sop(m,"Exit. Returning cea_settings_id=%s" % (cea_settings_id))
    return cea_settings_id

def getClusterCEASettingsId(clustername):
    """Returns the Object ID for CEA Settings in a cluster, or None"""
    m = "getClusterCEASettingsId:"
    #sop(m,"Entry. clustername=%s" % ( clustername ))
    cellname = getCellName()
    cea_settings_id = AdminConfig.getid( '/Cell:%s/ServerCluster:%s/CEASettings:/' % (cellname, clustername) )
    #sop(m,"Exit. Returning cea_settings_id=%s" % (cea_settings_id))
    return cea_settings_id

def listCEASettings(cea_settings_id):
    """Displays all values in a CEASettings object (useful for debug)."""
    m = "listCEASettings:"
    if cea_settings_id != None and cea_settings_id != '':
        sop(m,"Entry. cea_settings_id=%s" % (cea_settings_id))
        commsvc_id = AdminConfig.showAttribute(cea_settings_id,'commsvc')
        sop(m,"commsvc_id=%s" % (commsvc_id))
        if commsvc_id != None and commsvc_id != '':
             attribs = AdminConfig.show(commsvc_id)
             sop(m,"commsvc attributes: %s" % ( attribs ))
        cti_gateway_id = AdminConfig.showAttribute(cea_settings_id,'ctiGateway')
        sop(m,"cti_gateway_id=%s" % (cti_gateway_id))
        if cti_gateway_id != None and cti_gateway_id != '':
            attribs = AdminConfig.show(cti_gateway_id)
            sop(m,"cti gateway attributes: %s" % ( attribs ))
        sop(m,"Exit.")
    else:
        sop(m,"Entry/Exit. cea_settings_id is not defined. cea_settings_id=%s" % ( cea_settings_id ))

def listAllCEASettings():
    """Displays all CEASettings objects (useful for debug)."""
    m = "listAllCEASettings:"

    # Servers
    serverlist = listAllServers()
    for (nodename,servername) in serverlist:
        cea_settings_id = getServerCEASettingsId(nodename, servername)
        if cea_settings_id != None and cea_settings_id != '':
            listCEASettings(cea_settings_id)
        else:
            sop(m,"No CEASettings for nodename=%s servername=%s cea_settings_id=%s" % (nodename, servername, cea_settings_id))

    # Clusters
    clusternames = listServerClusters()
    for clustername in clusternames:
        cea_settings_id = getClusterCEASettingsId(clustername)
        if cea_settings_id != None and cea_settings_id != '':
            listCEASettings(cea_settings_id)
        else:
            sop(m,"No CEASettings for clustername=%s cea_settings_id=%s" % (clustername, cea_settings_id))

def deleteAllCEASettings():
    """Deletes all CEASettings objects."""
    m = "deleteAllCEASettings:"
    serverlist = listAllServers()
    for (nodename,servername) in serverlist:
        sop(m,"nodename=%s servername=%s" % (nodename, servername))
        cea_settings_id = getServerCEASettingsId(nodename, servername)
        if cea_settings_id != None and cea_settings_id != '':
            sop(m,"Deleting CEA_settings %s" % (cea_settings_id))
            AdminConfig.remove( cea_settings_id )

def createCEASettings( nodename, servername, clustername=None ):
    """Creates a CEASettings object for the specified server,
    along with one CTIGateway object and one Commsvc object.
    Specify nodename and servername, or specify clustername, but not both."""
    m = "createCEASettings:"
    sop(m,"Entry. nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername ))

    if None != nodename and None != servername and None == clustername:
        owner_id = getServerId(nodename,servername)
        if None == owner_id:
            raise m + "ERROR: Could not get server ID. nodename=%s servername=%s" % ( nodename, servername )
    elif None == nodename and None == servername and None != clustername:
        owner_id = getClusterId(clustername)
        if None == owner_id:
            raise m + "ERROR: Could not get cluster ID. clustername=%s" % ( clustername )
    else:
        raise m + "ERROR: Invalid parms. Specify nodename/servername or clustername, but not both.  nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername )

    sop(m,"owner_id=%s" % ( owner_id ))
    cea_settings_id = AdminConfig.create( 'CEASettings', owner_id, [] )

    # Create an underlying CTIGateway object
    cti_gateway_id = AdminConfig.create('CTIGateway', cea_settings_id, [] )
    if None == cti_gateway_id or cti_gateway_id == '':
        raise m + "ERROR: Could not create cti_gateway. nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername )
    sop(m,"cti_gateway_id=%s" % ( cti_gateway_id ))

    # Create an underlying Commsvc object.
    commsvc_id = AdminConfig.create('Commsvc', cea_settings_id, [] )
    if None == commsvc_id or commsvc_id == '':
        raise m + "ERROR: Could not create commsvc. nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername )
    sop(m,"commsvc_id=%s" % ( commsvc_id ))

    sop(m,"Exit. Returning cea_settings_id=%s" % ( cea_settings_id) )
    return cea_settings_id

def getCEASettings( nodename, servername, clustername=None ):
    """Gets or creates a CEASettings object for the specified server or cluster.
    Specify nodename and servername, or specify clustername, but not both."""
    m = "getCEASettings:"
    sop(m,"Entry. nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername, ))

    if None != nodename and None != servername and None == clustername:
        cea_settings_id = getServerCEASettingsId(nodename, servername)
    elif None == nodename and None == servername and None != clustername:
        cea_settings_id = getClusterCEASettingsId(clustername)
    else:
        raise m + "ERROR: Invalid parms. Specify nodename/servername or clustername, but not both.  nodename=%s servername=%s clustername=%s" % ( nodename, servername, clustername )

    sop(m,"Initial cea_settings_id=%s" % (cea_settings_id))
    if cea_settings_id == None or cea_settings_id == '':
        cea_settings_id = createCEASettings( nodename, servername, clustername )
    sop(m,"Exit. Returning cea_settings_id=%s" % ( cea_settings_id) )
    return cea_settings_id

def setCEASetting( nodename, servername, settingname, settingvalue, clustername=None ):
    """Sets one specified CEA setting.
    Specify nodename and servername, or specify clustername, but not both."""
    m = "setCEASetting:"
    sop(m,"Entry. nodename=%s servername=%s settingname=%s settingvalue=%s clustername=%s" % ( nodename, servername, settingname, settingvalue, clustername ))
    cea_settings_id = getCEASettings( nodename, servername, clustername )
    # Handle attributes in the CTIGateway and Commsvc configs.
    if settingname == 'maxRequestHoldTime':
        child_id = AdminConfig.showAttribute(cea_settings_id,'commsvc')
        sop(m,"Setting Commsvc attribute.")
    else:
        child_id = AdminConfig.showAttribute(cea_settings_id,'ctiGateway')
        sop(m,"Setting CTIGateway attribute.")
    AdminConfig.modify( child_id, [[settingname, settingvalue]])
    sop(m,"Exit.")

def setCEASettings( nodename, servername, settingsdict, clustername=None ):
    """Sets multiple CEA settings, where settingsdict contains names and values.
    For example:
        settingsdict = { 'gatewayProtocol': 'UDP',
                         'maxRequestHoldTime': '33'.
                         'gatewayAddress': 'fred.dummy.com',
                         'gatewayPort': '6543',
                         'superUsername': 'Fred' }
    Specify nodename and servername, or specify clustername, but not both."""
    m = "setCEASettings:"
    sop(m,"Entry. nodename=%s servername=%s settingsdict=%s" % ( nodename, servername, repr(settingsdict)))
    for (settingname,settingvalue) in settingsdict.items():
        setCEASetting(nodename,servername,settingname,settingvalue,clustername)
    sop(m,"Exit.")

############################################################
# coregroup related methods

def createCoregroup( name ):
    """Create a new Coregroup."""

    # first check if the requested coregroup already exists, if so we're done
    existing_cgs = _splitlines(AdminConfig.list('CoreGroup'))
    for coregroup in existing_cgs:
        coregroup_name = AdminConfig.showAttribute(coregroup, 'name')
        if coregroup_name == name:
            return

    # create a new coregroup if the existing one is not found
    AdminTask.createCoreGroup('[-coreGroupName %s]' % name)

def listCoregroups():
    """Returns list of names of all Coregroup objects"""
    return getObjectNameList( 'CoreGroup' )

def deleteAllCoregroups():
    """Deletes all Coregroups and their associated access points except the DefaultCoreGroup.

    Be careful with this code. It can actually delete the DefaultCoreGroup.
    And if you do, the ws runtime will be completely messed up. You've been warned."""
    m = "deleteAllCoreGroups:"
    #sop(m,"Entry.")
    obj_names = getObjectNameList( 'CoreGroup' )
    cgb_settings = AdminConfig.list( 'CoreGroupBridgeSettings' )
    for obj_name in obj_names:
        #sop(m,"obj_name=%s" % ( repr(obj_name) ))
        if -1 == repr( obj_name ).find( 'DefaultCoreGroup' ):
            id = getObjectByName( 'CoreGroup', obj_name )
            sop(m,"Deleting coregroup and access points. id=%s" % ( repr(id) ))
            AdminTask.deleteCoreGroupAccessPoints(cgb_settings, ['-coreGroupName', obj_name])
            AdminConfig.remove( id )
    #sop(m,"Exit")

def getCoreGroupIdByName( coregroupname ):
    """Returns the id for a coregroup object."""
    m = "getCoreGroupIdByName:"
    coregroup_id = None
    cg_names = getObjectNameList( 'CoreGroup' )
    for cg_name in cg_names:
        if -1 != repr( cg_name ).find( coregroupname ):
            coregroup_id = getObjectByName( 'CoreGroup', cg_name )
            sop(m,"Match: Found coregroup. coregroup_id=%s" % ( repr(coregroup_id) ))
            break
    if coregroup_id == None:
        raise m + "Error: Could not find coregroup. coregroupname=%s" % (coregroupname)
    return coregroup_id

def listCoreGroupServers( coregroup_id ):
    """Returns a list of coregroupserver_ids in the specified coregroup."""
    m = "listCoreGroupServers:"
    #sop(m,"Entry. coregroup_id=%s" % ( coregroup_id ))

    # Get the list of servers within the coregroup.
    coregroupservers = AdminConfig.showAttribute( coregroup_id, "coreGroupServers" )
    #sop(m,"coregroup_id=%s coregroupservers=%s" % ( repr(coregroup_id), coregroupservers ))

    # FIXME: use _splitlist() here

    # Verify the string contains a list of servers.
    if (not coregroupservers.startswith( '[' )) or (not coregroupservers.endswith( ']' )):
        raise m + " Error: CoreGroupServer string does not start and stop with square brackets."

    # Strip off the leading and trailing square brackets.
    coregroupservers = coregroupservers[1:(len(coregroupservers) - 1)]
    #sop(m,"coregroupservers=%s" % (coregroupservers))

    # Convert the single long string to a list of strings.
    coregroupservers_list = coregroupservers.split(' ')

    #sop(m,"Exit. coregroupservers_list=%s" % ( repr(coregroupservers_list) ))
    return coregroupservers_list

def findCoreGroupIdForServer( servername ):
    """Returns the coregroup_id which contains the specified server."""
    m = "findCoreGroupForServer:"
    #sop(m,"Entry. servername=%s" % ( servername ))

    coregroup_id = None
    cg_names = getObjectNameList( 'CoreGroup' )
    for cg_name in cg_names:
        cg_id = getObjectByName( 'CoreGroup', cg_name )
        #sop(m,"cg_id=%s" % ( repr(cg_id) ))
        serverlist = listCoreGroupServers(cg_id)
        for server in serverlist:
            #sop(m,"server=%s" % ( server ))
            if -1 != repr( server ).find( servername ):
                coregroup_id = cg_id
                #sop(m,"Match: Found server in coregroup. coregroup_id=%s" % ( repr(coregroup_id) ))
                break
        if coregroup_id != None:
            break
    if coregroup_id == None:
        raise m + " Error: Could not find coregroup. coregroupname=%s" % (coregroupname)

    #sop(m,"Exit. coregroup_id=%s" % ( coregroup_id ))
    return coregroup_id

def moveServerToCoreGroup( nodename, servername, coregroupname ):
    """Adds the specified server to the specified coregroup."""
    m = "moveServerToCoreGroup:"
    #sop(m,"Entry. nodename=%s servername=%s coregroupname=%s" % ( nodename, servername, coregroupname ))

    # Find the coregroup which presently contains the server.
    oldcoregroup_id = findCoreGroupIdForServer(servername)
    #sop(m,"moveServerToCoreGroup: oldcoregroup_id=%s" % ( oldcoregroup_id ))

    # Extract the name of this coregroup from the id.
    oldcoregroup_name = getNameFromId(oldcoregroup_id)
    #sop(m,"oldcoregroup_name=%s" % ( oldcoregroup_name ))

    # Move the server.
    commandstring = '[-source DefaultCoreGroup -target %s -nodeName %s -serverName %s]' % ( coregroupname, nodename, servername )
    #sop(m,"commandstring=%s" % ( commandstring ))
    AdminTask.moveServerToCoreGroup( commandstring )

    sop(m,"Moved server %s to coregroup %s." % ( servername, coregroupname ))

def addPreferredCoordinatorToCoreGroup( nodename, servername, coregroupname ):
    """Adds the specified server to the specified coregroup."""
    m = "addPreferredCoordinatorToCoreGroup:"
    #sop(m,"Entry. nodename=%s servername=%s coregroupname=%s" % ( nodename, servername, coregroupname ))

    # Get the ID for the coregroup.
    coregroup_id = getCoreGroupIdByName(coregroupname)
    #sop(m,"coregroup_id=%s" % ( coregroup_id ))

    # Get the ID for the coregroupserver object which represents the real server.
    coregroupserver_id = getCoreGroupServerIdByName(coregroup_id, servername)
    #sop(m,"coregroupserver_id=%s" % ( coregroupserver_id ))

    # Debug - before adding the coordinator.
    #sop(m,"before: preferredCoordinatorServers=%s" % ( AdminConfig.showAttribute( coregroup_id, "preferredCoordinatorServers" ) ))

    # Add the coordinator.
    AdminConfig.modify( coregroup_id, [['preferredCoordinatorServers', coregroupserver_id]])

    # Debug - after adding the coordinator.
    #sop(m,"after: preferredCoordinatorServers=%s" % ( AdminConfig.showAttribute( coregroup_id, "preferredCoordinatorServers" ) ))

    sop(m,"Exit. Added preferred coordinator %s to coregroup %s." % ( servername, coregroupname ))

def listPreferredCoordinators( coregroupname ):
    """Returns a string of preferredCoordinatorServers defined within a CoreGroup."""
    m = "listPreferredCoordinators:"
    coregroup_id = getCoreGroupIdByName(coregroupname)
    preferredCoordinators = AdminConfig.showAttribute( coregroup_id, "preferredCoordinatorServers" )
    #sop(m,"coregroup_id=%s preferredCoordinators=%s" % ( repr(coregroup_id), preferredCoordinators ))
    return preferredCoordinators

def getCoreGroupServerIdByName( coregroup_id, servername ):
    """Returns the ID of a CoreGroupServer object representing the supplied server name."""
    m = "getCoreGroupServerIdByName:"
    #sop(m,"Entry. coregroup_id=%s servername=%s" % ( coregroup_id, servername ))
    coregroupserver_id = None

    # Get a list of coregroupservers within the specified coregroup.
    cgs_string = AdminConfig.showAttribute(coregroup_id, "coreGroupServers")
    #sop(m,"cgs_string=%s" % ( cgs_string ))

    # Check for reasonable data.
    if len(cgs_string) > 2:
        # Expunge the square brackets and convert the string to a list.
        cgs_list = cgs_string[1:-1].split(" ")
        # Test each server for a match.
        for cgs_id in cgs_list:
            cgs_name = getNameFromId(cgs_id)
            if servername == cgs_name:
                #sop(m,"Found match. cgs_id=%s cgs_name=%s servername=%s" % ( cgs_id, cgs_name, servername ))
                coregroupserver_id = cgs_id
                break
            # else:
            #     sop(m,"No match. cgs_id=%s cgs_name=%s servername=%s" % ( cgs_id, cgs_name, servername ))
    else:
        raise m + "Error: Could not get valid list of coregroupservers. servername=%s cgs_string=%s" % ( servername, cgs_string )

    if None == coregroupserver_id:
        raise m + "Error: Could not find servername within coregroupservers. servername=%s cgs_string=%s" % ( servername, cgs_string )

    #sop(m,"Exit. coregroupserver_id=%s" % ( coregroupserver_id ))
    return coregroupserver_id



############################################################
# core-group-bridge related methods

def deleteAllCoreGroupAccessPointGroups():
    '''Deletes all coregroup access point groups except for the default access point group'''
    m = "deleteAllAccessPointGroups:"
    #sop(m,"Entry. ")

    existing_apgs = _splitlines(AdminConfig.list('AccessPointGroup'))

    for apg in existing_apgs:
        apg_name = AdminConfig.showAttribute(apg, 'name')
        if -1 == repr( apg_name ).find( 'DefaultAccessPointGroup' ):
            AdminConfig.remove(apg)

    #sop(m,"Exit. ")

def createAccessPointGroup(name):
    '''Create a coregroup Access Point Group with the given name'''
    m = "createAccessPointGroup:"
    #sop(m,"Entry. name=%s" % ( name ))

    result = ''
    cgb_settings = AdminConfig.list('CoreGroupBridgeSettings')

    existing_apgs = _splitlines(AdminConfig.list('AccessPointGroup'))

    # search existing cgaps before trying to create
    for apg in existing_apgs:
        apg_name = AdminConfig.showAttribute(apg, 'name')
        # if access point group already exists, clear out the CGAP entries
        if apg_name == name:
            sop(m,"APG with name=%s found, clearing CGAPS" % name)
            result = apg
            AdminConfig.modify(apg, [['coreGroupAccessPointRefs', []]])
            break

    # if access point was not found, create it
    if result == '':
        sop(m,"Creating APG name=%s" % name)
        result = AdminConfig.create('AccessPointGroup', cgb_settings, [['name', name]])

    #sop(m,"Exit. result=%s" % ( result ))
    return result


def createCoreGroupAccessPoint(name, coreGroup):
    '''Creates a core group access point (or returns an existing one) with the given name and core group'''
    m = "createCoreGroupAccessPoint:"
    #sop(m,"Entry. name=%s, coreGroup=%s" % ( name, coreGroup ))

    result = ''
    cgb_settings = AdminConfig.list('CoreGroupBridgeSettings')

    existing_cgaps = _splitlines(AdminConfig.list('CoreGroupAccessPoint'))

    # search existing cgaps before trying to create
    for cgap in existing_cgaps:
        cgap_name = AdminConfig.showAttribute(cgap, 'name')
        cgap_coreGroup = AdminConfig.showAttribute(cgap, 'coreGroup')
        # if name is the same, check whether it has correct coregroup
        if cgap_name == name:
            result = cgap
            if cgap_coreGroup != coreGroup:
                sop(m,"CGAP with name=%s found, modifying coreGroup." % name)
                AdminConfig.modify(cgap, [['coreGroup', coreGroup]])
            else:
                sop(m,"CGAP with name=%s, coreGroup=%s found, using pre-existing" % (name, coreGroup))

            break

    # if a CGAP was not found with that name, create a new one
    if result == '':
        result = AdminConfig.create('CoreGroupAccessPoint', cgb_settings, [['name', name], ['coreGroup', coreGroup]])

    #sop(m,"Exit. result=%s" % ( result ))
    return result

def setCoreGroupAccessPointIntoAccessPointGroup(apg_id, cgap_id):
    '''Moves a coregroup access point into an access point group'''
    m = "setCoreGroupAccessPointIntoAccessPointGroup:"
    #sop(m,"Entry. apg_id=%s, cgap_id=%s" % ( apg_id, cgap_id ))

    # since showAttribute returns the string form ('[id_1 id_2]'), we must split specially to get a list
    current_cgaps = AdminConfig.showAttribute(apg_id, 'coreGroupAccessPointRefs')[-1:1].split(' ')
    current_cgaps.append(cgap_id)

    AdminConfig.modify(apg_id, [['coreGroupAccessPointRefs', current_cgaps]])

    #sop(m,"Exit.")


def createBridgeInterfaces(cgap_id, node, server, chain):
    """Creates a bridge interface in the specified coregroup"""
    m = "createBridgeInterfaces:"
    result = None
    #sop(m,"createBridgeInterfaces: Entry. cgap=" + cgap + " node=" + node + " server=" + server + " chain=" + chain)

    result = AdminConfig.create( 'BridgeInterface', cgap_id, [['node', node], ['server', server], ['chain', chain]] )

    #sop(m,"createBridgeInterfaces: Exit. result=" + result)
    return result

def createPeerAccessPoint( name, cell, coreGroup, coreGroupAccessPoint ):
    """Creates a peer access point in all existing bridges"""
    result = None
    # print "createPeerAccessPoint: Entry. name=" + name + " cell=" + cell + " coreGroup=" + coreGroup + " coreGroupAccessPoint=" + coreGroupAccessPoint

    cgbSettings = _splitlines(AdminConfig.list( 'CoreGroupBridgeSettings' ))
    for bridge_id in cgbSettings:
        # print "createPeerAccessPoint: bridge_id=" + bridge_id + " Creating Peer Access Point."
        result = AdminConfig.create( 'PeerAccessPoint', bridge_id, [['name', name], ['cell', cell], ['coreGroup', coreGroup], ['coreGroupAccessPoint', coreGroupAccessPoint]] )
        break

    # print "createPeerAccessPoint: Exit. result=" + result
    return result

def createPeerEndPoint( pap, host, port ):
    """Creates a peer end point in the specified Peer Access Point."""
    result = None
    # print "createPeerEndPoint: Entry. pap=" + pap + " host=" + host + " port=%d" % (port)

    paps = _splitlines(AdminConfig.list( 'PeerAccessPoint' ))
    for pap_id in paps:
        # print "createPeerEndPoint: pap_id=" + pap_id
        # Extract the PAP name from the start of the pap_id
        # eg, PAP_1(cells/ding6Cell01|coregroupbridge.xml#PeerAccessPoint_1157676511879)
        ix = pap_id.find('(')
        # print "createPeerEndPoint: ix=%d" % (ix)
        if ix != -1 :
            pap_name = pap_id[0:ix]
            # print "createPeerEndPoint: pap_name=" + pap_name
            if pap_name == pap:
                # print "createPeerEndPoint: Found pap. Creating end point."
                result = AdminConfig.create( 'EndPoint', pap_id, [['host', host], ['port', port]] )
                break

    # print "createPeerEndPoint: Exit. result=" + result
    return result

def setPeerAccessPointIntoAccessPointGroup( apg, pap ):
    """Sets a peer end point into an Access Point Group"""
    m = "setPeerAccessPointIntoAccessPointGroup:"
    requested_pap_id = None
    papFound = 0
    apgFound = 0
    #sop(m,"Entry. apg=" + apg + " pap=" + pap)

    paps = _splitlines(AdminConfig.list( 'PeerAccessPoint' ))
    for pap_id in paps:
        #sop(m,"pap_id=" + pap_id)
        pap_name = getNameFromId(pap_id)
        #sop(m,"pap_name=" + pap_name)
        if pap_name == pap:
            #sop(m,"Found pap. Saving it.")
            requested_pap_id = pap_id
            papFound = 1
            break

    if 0 == papFound:
        sop(m,"Exit. ERROR. Did not find pap.")
        # TODO: Throw exception here.
        return

    apgs = _splitlines(AdminConfig.list( 'AccessPointGroup' ))
    for apg_id in apgs:
        #sop(m,"apg_id=" + apg_id)
        apg_name = getNameFromId(apg_id)
        #sop(m,"apg_name=" + apg_name)
        if apg_name == apg:
            #sop(m,"Found apg. Modifying it.")
            AdminConfig.modify(apg_id, [[ 'peerAccessPointRefs', requested_pap_id ]])
            apgFound = 1
            break

    if 0 == apgFound:
        sop(m,"Exit. ERROR. Did not find apg.")
        # TODO: Throw exception here.
        return

    #sop(m,"Exit.")
    return

def deleteAllBridgeInterfaces():
    """Deletes all bridge interfaces"""
    result = None
    # print "deleteAllBridgeInterfaces: Entry."

    bridge_interfaces = _splitlines(AdminConfig.list( 'BridgeInterface' ))
    for bridge_interface_id in bridge_interfaces:
        # node = AdminConfig.showAttribute(bridge_interface_id, "node")
        # server = AdminConfig.showAttribute(bridge_interface_id, "server")
        # chain = AdminConfig.showAttribute(bridge_interface_id, "chain")
        # print "deleteAllBridgeInterfaces: Deleting. node=" + node + " server=" + server + " chain=" + chain
        AdminConfig.remove( bridge_interface_id )

    # print "deleteAllBridgeInterfaces: Exit."
    return result

def deleteAllPeerAccessPoints():
    """Deletes all peer access points"""
    result = None
    # print "deleteAllPeerAccessPoints: Entry."

    paps = _splitlines(AdminConfig.list( 'PeerAccessPoint' ))
    # print "paps..."
    # print paps
    for pap_id in paps:
        # name = AdminConfig.showAttribute(pap_id, "name")
        # cell = AdminConfig.showAttribute(pap_id, "cell")
        # coreGroup = AdminConfig.showAttribute(pap_id, "coreGroup")
        # cgap = AdminConfig.showAttribute(pap_id, "coreGroupAccessPoint")
        # print "deleteAllPeerAccessPoints: name=" + name + " cell=" + cell + " coreGroup=" + coreGroup + " cgap=" + cgap + " pap_id=" + pap_id

        deleteAllPeerEndPoints(pap_id)
        AdminConfig.remove( pap_id )

    # print "deleteAllPeerAccessPoints: Exit."
    return result

def deleteAllPeerEndPoints( pap_id ):
    """Deletes all Peer End Points for the specified Peer Access Point"""
    result = None
    # print "deleteAllPeerEndPoints: Entry."

    pepsString = AdminConfig.showAttribute(pap_id, "peerEndPoints")
    # print "pepsString=" + pepsString
    if len(pepsString) > 2:
        peps = pepsString[1:-1].split(" ")
        # print "peps..."
        # print peps

        for pep_id in peps:
            host = AdminConfig.showAttribute(pep_id, "host")
            port = AdminConfig.showAttribute(pep_id, "port")
            # print "deleteAllPeerEndPoints: Deleting PeerEndPoint. host=" + host + " port=" + port
            AdminConfig.remove( pep_id )
            break
    # else:
        # print "deleteAllPeerEndPoints: No PeerEndPoints to delete."

    # print "deleteAllPeerEndPoints: Exit."
    return result

def deleteAllPeerAccessPointsFromAllAccessPointGroups():
    """Sets a peer end point into an Access Point Group"""
    result = None
    m = "deleteAllPeerAccessPointsFromAllAccessPointGroups:"
    sop(m,"Entry.")

    apgs = _splitlines(AdminConfig.list( 'AccessPointGroup' ))
    for apg_id in apgs:
        sop(m,"apg_id=" + apg_id)
        apg_name = getNameFromId(apg_id)
        sop(m,"apg_name=" + apg_name)
        AdminConfig.modify(apg_id, [[ 'peerAccessPointRefs', '' ]])

    sop(m,"Exit.")
    return result

############################################################
# core-group-bridge-tunnel related methods

def deleteAllTunnelAccessPointGroups():
    '''Deletes all tunnel access point groups.'''
    m = "deleteAllTunnelAccessPointGroups:"
    sop(m,"Entry.")

    existing_tapgs = _splitlines(AdminConfig.list('TunnelAccessPointGroup'))

    for tapg in existing_tapgs:
        tapg_name = AdminConfig.showAttribute(tapg, 'name')
        sop(m,"Deleting tapg_name %s" % ( tapg_name ))
        AdminConfig.remove(tapg)

    sop(m,"Exit.")

def findTunnelAccessPointGroup(name):
    """Return the config ID of the TAPG with the given name, or else None"""

    existing_tapgs = _splitlines(AdminConfig.list('TunnelAccessPointGroup'))

    # search existing cgaps before trying to create
    for tapg in existing_tapgs:
        tapg_name = AdminConfig.showAttribute(tapg, 'name')
        if tapg_name == name:
            return tapg
    return None

def createTunnelAccessPointGroup(name):
    '''Create a Tunnel Access Point Group with the given name, or, if it already exists, clear out its CGAP entries'''
    m = "createTunnelAccessPointGroup:"
    sop(m,"Entry. name=%s" % (name))

    result = ''
    cgb_settings = AdminConfig.list('CoreGroupBridgeSettings')

    existing_tapg = findTunnelAccessPointGroup(name)
    if existing_tapg is not None:
        sop(m,"TAPG with name=%s already exists.  Clearing TPAPS" % name)
        result = existing_tapg
        AdminConfig.modify(existing_tapg, [['tunnelPeerAccessPointRefs', []]])
    else:
        sop(m,"Creating TAPG name=%s" % name)
        result = AdminConfig.create('TunnelAccessPointGroup', cgb_settings, [['name', name]])

    sop(m,"Exit. result=%s" % ( result ))
    return result


def setCGAPintoTAPG( tapg_id, cgap_id, ):
    """References a core group access point from a tunnel access point group"""
    m = "setCGAPintoTAPG:"
    sop(m,"Entry. tapg_id=%s cgap_id=%s" % ( tapg_id, cgap_id, ))

    # Get the list of CGAPS presently in the TAPG.
    cgap_string_list = AdminConfig.showAttribute(tapg_id, 'coreGroupAccessPointRefs')
    sop(m,"cgap_string_list=%s" % ( cgap_string_list ))

    # Convert it from a dumb string representation to a python list containing strings.
    cgap_list = stringListToList(cgap_string_list)
    sop(m,"cgap_list=%s" % ( cgap_list ))

    # Check whether the cgap_id already exists in the list.
    found = 0
    for cgap in cgap_list:
        sop(m,"Next cgap=%s" % ( cgap ))
        if cgap == cgap_id:
            sop(m,"Found match. CGAP already exists in TAPG. Do nothing.")
            found = 1
            break
        else:
            sop(m,"No match. Continuing. cgap=%s" % ( cgap ))

    # Add a reference to the cgap if necessary.
    if 0 == found:
        cgap_list.append(cgap_id)
        sop(m,"Appended new cgap_id to list. Modifying TAPG. cgap_list=%s" % ( cgap_list ))
        AdminConfig.modify(tapg_id, [['coreGroupAccessPointRefs', cgap_list]])

    sop(m,"Exit.")


def createTunnelPeerAccessPoint( name, cell, useSSL, ):
    """Creates a tunnel peer access point"""
    m = "createTunnelPeerAccessPoint:"
    sop(m,"Entry. name=" + name + " cell=" + cell + " useSSL=" + useSSL )

    result = None
    cgbSettings = _splitlines(AdminConfig.list( 'CoreGroupBridgeSettings' ))
    for bridge_id in cgbSettings:
        sop(m,"Creating Tunnel Peer Access Point in bridge %s" % ( bridge_id ))
        result = AdminConfig.create( 'TunnelPeerAccessPoint', bridge_id, [['name', name], ['cellName', cell], ['useSSL', useSSL]], )
        break

    sop(m,"Exit. result=%s" % ( result ))
    return result

def createTunnelPeerEndPoint( tpap, coregroup_name, host, port ):
    """Creates a peer end point in the specified Tunnel Peer Access Point."""
    m = "createTunnelPeerEndPoint:"
    sop(m,"Entry. tpap=%s coregroup_name=%s host=%s port=%d" % ( tpap, coregroup_name, host, port, ))

    # Find the TPAP.
    tpaps = _splitlines(AdminConfig.list( 'TunnelPeerAccessPoint' ))
    for tpap_id in tpaps:
        sop(m,"tpap_id=%s" % ( tpap_id ))
        tpap_name = getNameFromId(tpap_id)
        sop(m,"tpap_name=%s" % ( tpap_name ))
        if tpap_name == tpap:
            # Find the coregroup.
            sop(m,"Found tpap. Looking for core group %s" % ( coregroup_name ))
            cg_id_string_list = AdminConfig.showAttribute(tpap_id, 'peerCoreGroups')
            sop(m,"cg_id_string_list=%s" % ( cg_id_string_list ))
            cg_id_list = stringListToList(cg_id_string_list)
            sop(m,"cg_id_list=%s" % ( cg_id_list ))
            found = 0
            for cg_id in cg_id_list:
                sop(m,"Next cg_id=%s" % ( cg_id ))
                cg_name = AdminConfig.showAttribute(cg_id,'coreGroup')
                sop(m,"cg_name=%s" % (cg_name))
                if cg_name == coregroup_name:
                    sop(m,"Found desired coregroup already exists.")
                    found = 1
                    break
            if found == 0:
                sop(m,"Creating PeerCoreGroup object.")
                cg_id = AdminConfig.create('PeerCoreGroup',tpap_id,[['coreGroup',coregroup_name]])
            sop(m,"cg_id=%s" % ( cg_id ))
            # Find the EndPoint
            ep_id_string_list = AdminConfig.showAttribute(cg_id,'endPoints')
            sop(m,"ep_id_string_list=%s" % ( ep_id_string_list ))
            ep_id_list = stringListToList(ep_id_string_list)
            sop(m,"ep_id_list=%s" % ( ep_id_list ))
            found = 0
            for ep_id in ep_id_list:
                sop(m,"Next ep_id=%s" % (ep_id))
                ep_host = AdminConfig.showAttribute(ep_id,'host')
                ep_port = AdminConfig.showAttribute(ep_id,'port')
                # The following ugly conversion to strings is the only way I could get the test to compare properly. Argh.
                port_string = "%s" % ( port )
                ep_port_string = "%s" % ( ep_port )
                if host == ep_host and port_string == ep_port_string:
                    sop(m,"Found existing end point.  Doing nothing. host=%s port_string=%s" % ( host, port_string))
                    found = 1
                    break
                else:
                    sop(m,"No match. Continuing. host=%s port_string=%s ep_host=%s ep_port_string=%s" % ( host, port_string, ep_host, ep_port_string,))
            if found == 0:
                sop(m,"Creating new end point.")
                ep_id = AdminConfig.create( 'EndPoint', cg_id, [['host', host], ['port', port]] )
                sop(m,"ep_id=%s" % ( ep_id ))

    sop(m,"Exit.")
    return

def setTPAPintoTAPG( tapg, tpap ):
    """Sets a Tunnel Peer Access Point into a Tunnel Access Point Group"""
    m = "setTPAPintoTAPG:"
    sop(m,"Entry. tapg=%s tpap=%s" % ( tapg, tpap, ))

    requested_tpap_id = None
    tpapFound = 0
    tapgFound = 0

    tpaps = _splitlines(AdminConfig.list( 'TunnelPeerAccessPoint' ))
    sop(m,"tpaps=%s" % ( tpaps ))
    for tpap_id in tpaps:
        sop(m,"tpap_id=%s" % ( tpap_id ))
        tpap_name = getNameFromId(tpap_id)
        sop(m,"tpap_name=%s" % ( tpap_name ))
        if tpap_name == tpap:
            sop(m,"Found tpap. Saving it.")
            requested_tpap_id = tpap_id
            tpapFound = 1
            break

    if 0 == tpapFound:
        errmsg = "Exit. ERROR. Did not find tpap %s" % ( tpap )
        sop(m,errmsg)
        raise m + errmsg

    tapgs = _splitlines(AdminConfig.list( 'TunnelAccessPointGroup' ))
    sop(m,"tapgs=%s" % ( tapgs ))
    for tapg_id in tapgs:
        sop(m,"tapg_id=" + tapg_id)
        tapg_name = getNameFromId(tapg_id)
        sop(m,"tapg_name=" + tapg_name)
        if tapg_name == tapg:
            sop(m,"Found tapg. Modifying it.")
            AdminConfig.modify(tapg_id, [[ 'tunnelPeerAccessPointRefs', requested_tpap_id ]])
            tapgFound = 1
            break

    if 0 == tapgFound:
        errmsg = "Exit. ERROR. Did not find tapg %s." % ( tapg )
        sop(m,errmsg)
        raise m + errmsg

    sop(m,"Exit.")
    return


def deleteAllTunnelPeerAccessPoints():
    """Deletes all tunnel peer access points"""
    m = "deleteAllTunnelPeerAccessPoints:"
    sop(m,"Entry.")

    tpaps = _splitlines(AdminConfig.list( 'TunnelPeerAccessPoint' ))
    sop(m,"tpaps=%s" % ( tpaps ))

    for tpap_id in tpaps:
        sop(m,"tpap_id=%s" % ( tpap_id ))
        tpap_name = AdminConfig.showAttribute(tpap_id, "name")
        sop(m,"Deleting tpap_name %s" % ( tpap_name ))
        AdminConfig.remove( tpap_id )

    sop(m,"Exit.")
    return

def deleteAllCGBTunnels():
    """Convenience method gets rid of all TAPGs and TPAPs."""
    # CGBTunnels first appeared in 7.0.0.0.
    for nodename in listNodes():
        if versionLessThan(nodename, '7.0.0.0'):
            return
    deleteAllTunnelPeerAccessPoints()
    deleteAllTunnelAccessPointGroups()

def deleteAllCGBTunnelTemplates():
    """Convenience method gets rid of all tunnel templates"""
    # CGBTunnels first appeared in 7.0.0.0.
    for nodename in listNodes():
        if versionLessThan(nodename, '7.0.0.0'):
            return
    for tunnel in _splitlines(AdminConfig.list('TunnelTemplate')):
        AdminConfig.remove(tunnel)

def createCGBTunnelTemplate(name, tapg, usessl):
    """Create a CGB tunnel template.
    name: string, name to give the template
    tapg: string, name of the Tunnel Access Point Group to associate with it
    usessl: BOOLEAN, whether this tunnel template should use SSL
    Returns the config ID of the created template"""
    tapg_id = findTunnelAccessPointGroup(tapg)
    usessl_value = "false"
    if usessl:
        usessl_value = "true"
    cgbSettings = _splitlines(AdminConfig.list( 'CoreGroupBridgeSettings' ))
    # Not sure why the loop; copied from createPeerAccessPoint()
    for bridge_id in cgbSettings:
        template_id = AdminConfig.create('TunnelTemplate',
                                         bridge_id,
                                         [['name', name],
                                          ['useSSL', usessl_value],
                                          ['tunnelAccessPointGroupRef', tapg_id]])
    return template_id

############################################################
# application-related methods

def deleteApplicationByName( name ):
    """Delete the named application from the cell"""
    AdminApp.uninstall( name )

def listApplications():
    """Return list of all application names.
    Note: the admin console is included - it's called 'isclite' in v6.1, don't know on other versions"""
    if isND():
        nodename = getDmgrNodeName()
    else:
        nodename = listNodes()[0]  # there will only be one
    if versionAtLeast(nodename, "6.1.0.0"):
        return _splitlines(AdminApp.list( 'WebSphere:cell=%s' % getCellName() ))
    else:
        # for 6.0.2 support
        return _splitlines(AdminApp.list())

def isApplicationRunning(appname):
    """Returns a boolean which indicates whether the application is running.
    see http://publib.boulder.ibm.com/infocenter/wasinfo/v6r0/index.jsp?topic=/com.ibm.websphere.nd.doc/info/ae/ae/txml_adminappobj.html """
    mbean = AdminControl.queryNames('type=Application,name=%s,*' % ( appname ))
    if mbean:
        return True
    return False

def getClusterTargetsForApplication(appname):
    """Returns a python list of cluster names where the application is installed.
    For example, [ 'cluster1', 'cluster2' ] """
    return getTargetsForApplication(appname,"ClusteredTarget")

def getServerTargetsForApplication(appname):
    """Returns a python list of comma-delimited server name and
    node name pairs where the application is installed.
    For example, [ 'server1,node1', 'server2,node2' ] """
    return getTargetsForApplication(appname,"ServerTarget")

def getTargetsForApplication(appname, targettype):
    """Returns a python list of cluster names or comma-delimited server
    and node name pairs for targets where the application is installed.
    Specify targettype as 'ClusteredTarget' or 'ServerTarget'
    Returns  [ 'cluster1', 'cluster2' ]
    or       [ 'server1,node1', 'server2,node2' ] """
    m = "getTargetsForApplication:"
    sop(m,"Entry. appname=%s targettype=%s" % ( appname, targettype ))

    # Check parameter.
    if "ClusteredTarget" != targettype and "ServerTarget" != targettype:
        raise m + " ERROR. Parameter targettype must be ClusteredTarget or ServerTarget. actual=%s" % ( targettype )

    rc = []
    deployments = AdminConfig.getid("/Deployment:%s/" % ( appname ))
    if (len(deployments) > 0) :
        deploymentObj = AdminConfig.showAttribute(deployments, 'deployedObject')
        # sop(m,"deploymentObj=%s" % ( repr(deploymentObj) ))

        # First get the Target Mappings.  The 'target' attribute indicates whether
        # the target is a ServerTarget or a ClusterTarget.
        rawTargetMappings = AdminConfig.showAttribute(deploymentObj, 'targetMappings')
        # Convert the single string to a real python list containing strings.
        targetMappingList = stringListToList(rawTargetMappings)
        # sop(m, "\n\ntargetMappingList=%s" % ( repr(targetMappingList) ))

        # Next get the Deployment Targets. These contain the cluster name and/or server name and node name.
        rawDeploymentTargets = AdminConfig.showAttribute(deployments, 'deploymentTargets')
        # Convert the single string to a real python list containing strings.
        deploymentTargetList = stringListToList(rawDeploymentTargets)
        # sop(m, "\n\ndeploymentTargetList=%s" % ( repr(deploymentTargetList) ))

        # Handle each target mapping...
        for targetMapping in targetMappingList:
            targetMappingTarget = getObjectAttribute(targetMapping,"target")
            # sop(m,"\n\ntargetMapping=%s targetMappingTarget=%s" % ( targetMapping, targetMappingTarget ))

            if 0 <= targetMappingTarget.find(targettype):
                sop(m,"Match 1. TargetMapping has desired type. desired=%s actual=%s" % ( targettype, targetMappingTarget ))

                # Find the associated deployment target object.
                for deploymentTarget in deploymentTargetList:
                    current_deployment_target_name = getNameFromId(deploymentTarget)
                    sop(m,"deploymentTarget=%s current_deployment_target_name=%s" % ( deploymentTarget, current_deployment_target_name ))
                    if 0 <= deploymentTarget.find(targetMappingTarget):
                        sop(m,"Match 2. Found associated deployment target. deploymentTarget=%s targetMappingTarget=%s" % ( deploymentTarget, targetMappingTarget ))

                        # Extract the clustername and/or servername and nodename.
                        if "ClusteredTarget" == targettype:
                            clusterName = getObjectAttribute(deploymentTarget,"name")
                            sop(m,"Saving clusterName=%s" % ( clusterName ))
                            rc.append( clusterName )
                        else:
                            serverName = getObjectAttribute(deploymentTarget,"name")
                            nodeName = getObjectAttribute(deploymentTarget,"nodeName")
                            sop(m,"Saving serverName=%s nodeName=%s" % ( serverName, nodeName ))
                            rc.append( "%s,%s" % ( serverName, nodeName ))
                    else:
                        sop(m,"No match 2. Deployment target does not match. targetMappingTarget=%s deploymentTarget=%s" % ( targetMappingTarget, deploymentTarget ))
            else:
                sop(m,"No match 1. TargetMapping does not have desired type. desired=%s actual=%s" % ( targettype, targetMappingTarget ))

    else:
        sop(m, "No deployments found.")

    sop(m,"Exit. Returning list with %i elements: %s" % ( len(rc), repr(rc) ))
    return rc

def startAllApplications():
    """Start all applications on all their servers"""
    m = "startAllApplications:"
    appnames = listApplications()
    #sop(m,repr(appnames))
    for appname in appnames:
        # Don't start the admin console, it's always on
        if appname == 'isclite':
            pass
        elif appname == 'filetransfer':
            pass # this one seems important too, though I've only seen it on Windows
        else:
            sop(m,"Starting application %s" % appname)
            startApplication(appname)

def startApplication(appname):
    """Start the named application on all its servers.

    Note: This method assumes the application is installed on all servers.
    To start an application on an individual server, use startApplicationOnServer()
    To start an application on all members of a cluster, use startApplicationOnCluster()"""
    m = "startApplication:"
    sop(m,"Entry. appname=%s" % ( appname ))
    cellname = getCellName()
    servers = listServersOfType('APPLICATION_SERVER')
    for (nodename,servername) in servers:
        sop(m,"Handling cellname=%s nodename=%s servername=%s" % ( cellname, nodename, servername ))
        # Get the application manager MBean
        appManager = AdminControl.queryNames('cell=%s,node=%s,type=ApplicationManager,process=%s,*' %(cellname,nodename,servername))
        # start it
        sop(m,"Starting appname=%s" % ( appname ))
        AdminControl.invoke(appManager, 'startApplication', appname)
        # FIXME: not working on Base unmanaged server on z/OS for some reason
        # Not sure if it works anywhere since usually I just start the
        # servers after configuration and don't need to explicitly start
        # the applications
    sop(m,"Exit.")

def startApplicationOnServer(appname,nodename,servername):
    """Start the named application on one servers"""
    m = "startApplicationOnServer:"
    sop(m,"Entry. appname=%s nodename=%s servername=%s" % ( appname,nodename,servername ))
    cellname = getCellName()
    # Get the application manager MBean
    appManager = AdminControl.queryNames('cell=%s,node=%s,type=ApplicationManager,process=%s,*' %(cellname,nodename,servername))
    sop(m,"appManager=%s" % ( repr(appManager) ))
    # start it
    rc = AdminControl.invoke(appManager, 'startApplication', appname)
    sop(m,"Exit. rc=%s" % ( repr(rc) ))

def stopApplicationOnServer(appname,nodename,servername):
    """Stops the named application on one server."""
    m = "stopApplicationOnServer:"
    sop(m,"Entry. appname=%s nodename=%s servername=%s" % ( appname,nodename,servername ))
    cellname = getCellName()
    # Get the application manager MBean
    appManager = AdminControl.queryNames('cell=%s,node=%s,type=ApplicationManager,process=%s,*' %(cellname,nodename,servername))
    sop(m,"appManager=%s" % ( repr(appManager) ))
    # stop it
    rc = AdminControl.invoke(appManager, 'stopApplication', appname)
    sop(m,"Exit. rc=%s" % ( repr(rc) ))

def startApplicationOnCluster(appname,clustername):
    """Start the named application on all servers in named cluster"""
    m = "startApplicationOnCluster:"
    sop(m,"Entry. appname=%s clustername=%s" % ( appname, clustername ))
    server_id_list = listServersInCluster( clustername )
    for server_id in server_id_list:
        nodename = AdminConfig.showAttribute( server_id, "nodeName" )
        servername = AdminConfig.showAttribute( server_id, "memberName" )
        sop(m, "Starting application. application=%s cluster=%s node=%s server=%s" % (appname, clustername, nodename, servername) )
        startApplicationOnServer(appname,nodename,servername)
    sop(m,"Exit.")

def isApplicationReady(appname):
    """Returns True when app deployment is complete and ready to start.
       Returns False when the app is not ready or is not recognized.
       This method indicates when background processing is complete
       following an install, save, and sync.   
       This method is useful when installing really large EARs."""
    m = "isApplicationReady:"
    rc = False
    try:
        if 'true' == AdminApp.isAppReady(appname):
            sop(m,"App %s is READY. Returning True." % (appname))
            rc = True
        else:
            sop(m,"App %s is NOT ready. Returning False." % (appname))
    except:
        sop(m,"App %s is UNKNOWN. Returning False." % (appname))
    return rc

def stopApplicationOnCluster(appname,clustername):
    """Stops the named application on all servers in named cluster"""
    m = "stopApplicationOnCluster:"
    sop(m,"Entry. appname=%s clustername=%s" % ( appname, clustername ))
    server_id_list = listServersInCluster( clustername )
    for server_id in server_id_list:
        nodename = AdminConfig.showAttribute( server_id, "nodeName" )
        servername = AdminConfig.showAttribute( server_id, "memberName" )
        sop(m, "Stopping application. application=%s cluster=%s node=%s server=%s" % (appname, clustername, nodename, servername) )
        stopApplicationOnServer(appname,nodename,servername)
    sop(m,"Exit.")

def deleteAllApplications():
    """Delete all applications (except "built-in" ones like the Admin Console)"""
    m = "deleteAllApplications:"
    cellname = getCellName()
    appnames = listApplications()
    sop(m,"Applications=%s" % repr(appnames))
    for appname in appnames:
        # Don't delete the admin console :-)
        if appname == 'isclite':
            pass
        elif appname == 'filetransfer':
            pass # this one seems important too, though I've only seen it on Windows
        else:
            sop(m,"Deleting application %s" % appname)
            deleteApplicationByName( appname )

def deleteApplicationByNameIfExists( applicationName ):
    """Delete the application if it already exists.

    Return true (1) if it existed and was deleted.
    Return false (0) if it did not exist."""
    m = "deleteApplicationByNameIfExists:"
    sop(m,"Application Name = %s" % applicationName)
    apps = listApplications()
    sop(m,repr(apps))
    if applicationName in apps:
        sop(m,"Removing Application: %s" % repr(applicationName))
        deleteApplicationByName(applicationName)
        # Did exist and has been deleted, return true (1)
        return 1
    # Did not exist, did not delete, return false (0)
    return 0

def installApplication( filename, servers, clusternames, options ):
    """Install application and map to the named servers/clusters
    with the given options.

    "servers" is a list of dictionaries, each specifying a 'nodename'
    and 'servername' for one server to map the app to.

    "clusternames" is a list of cluster names to map the app to.


    options can be None, or else a list of additional options to pass.
    E.g. ['-contextroot', '/b2bua']
    See the doc for AdminApp.install for full details.
    """
    m = "installApplication:"

    sop(m,"filename=%s, servers=%s, clusternames=%s, options=%s" % (filename,repr(servers),repr(clusternames),options))
    targets = []
    cellname = getCellName()
    for server in servers:
        targets.append("WebSphere:cell=%s,node=%s,server=%s" % (cellname, server['nodename'], server['servername']))
    for clustername in clusternames:
        targets.append("WebSphere:cell=%s,cluster=%s" % (cellname,clustername))

    target = "+".join( targets )

    arglist = ['-target', target, ]

    # Set a default virtual host mapping if the caller does not specify one.
    if options != None:
        if not "-MapWebModToVH" in options:
            arglist.extend( ['-MapWebModToVH', [['.*', '.*', 'default_host']]] )

    # Append caller-specified options.
    if options != None:
        arglist.extend(options)

    sop(m,"Calling AdminApp.install of %s with arglist = %s" % ( filename, repr(arglist) ))
    AdminApp.install( filename, arglist )

def updateApplication(filename):
    """Update an application with a new ear file"""
    # We need to know the application name - it's in an xml file
    # in the ear
    import zipfile
    zf = zipfile.ZipFile(filename, "r")
    appxml = zf.read("META-INF/application.xml")
    zf.close()

    # parse the xml file
    # (cheat - it's simple)
    start_string = "<display-name>"
    end_string = "</display-name>"
    start_index = appxml.find(start_string) + len(start_string)
    end_index = appxml.find(end_string)

    appname = appxml[start_index:end_index].strip()

    AdminApp.update(appname,            # name of application
                    'app',              # type of update to do
                    # options:
                    ['-operation', 'update', # redundant but required
                     '-contents', filename,
                     ],
                    )

def setDeploymentAutoStart(deploymentname, enabled, deploymenttargetname=None):
    """Sets an application to start automatically, when the server starts.
    Specify enabled as a lowercase string, 'true' or 'false'.
    For example, setDeploymentAutoStart('commsvc', 'false')
    Returns the number of deployments which were found and set successfully.
    Raises exception if application is not found.

    You may optionally specify an explicit deployment target name, such as a server or cluster name.
    For example, setDeploymentAutoStart('commsvc', 'true',  deploymenttargetname='cluster1')
                 setDeploymentAutoStart('commsvc', 'false', deploymenttargetname='server1')
    If the deployment target name is not specified, autostart is set on all instances of the deployment.

    Ultimately, this method changes the 'enable' value in a deployment.xml file.  For example,
    <targetMappings xmi:id="DeploymentTargetMapping_1262640302437" enable="true" target="ClusteredTarget_1262640302439"/>
    """
    m = "setDeploymentAutoStart:"
    sop(m,"Entry. deploymentname=%s enabled=%s deploymenttargetname=%s" % ( deploymentname, repr(enabled), deploymenttargetname ))

    # Check arg
    if 'true' != enabled and 'false' != enabled:
        raise "Invocation Error. Specify enabled as 'true' or 'false'. enabled=%s" % ( repr(enabled) )

    numSet = 0
    deployments = AdminConfig.getid("/Deployment:%s/" % ( deploymentname ))
    if (len(deployments) > 0) :
        deploymentObj = AdminConfig.showAttribute(deployments, 'deployedObject')
        sop(m,"deploymentObj=%s" % ( repr(deploymentObj) ))

        # First get the Target Mappings.  These are the objects where we set enabled/disabled.
        rawTargetMappings = AdminConfig.showAttribute(deploymentObj, 'targetMappings')
        # Convert the single string to a real python list containing strings.
        targetMappingList = stringListToList(rawTargetMappings)
        sop(m, "targetMappingList=%s" % ( repr(targetMappingList) ))

        # Next get the Deployment Targets. These are the objects from which we determine the deployment target name.
        rawDeploymentTargets = AdminConfig.showAttribute(deployments, 'deploymentTargets')
        # Convert the single string to a real python list containing strings.
        deploymentTargetList = stringListToList(rawDeploymentTargets)
        sop(m, "deploymentTargetList=%s" % ( repr(deploymentTargetList) ))

        # Handle each target mapping...
        for targetMapping in targetMappingList:
            attr_target = getObjectAttribute(targetMapping,"target")
            sop(m,"targetMapping=%s attr_target=%s" % ( targetMapping, attr_target ))

            # Find the associated deployment target object.
            for deploymentTarget in deploymentTargetList:
                current_deployment_target_name = getNameFromId(deploymentTarget)
                sop(m,"deploymentTarget=%s current_deployment_target_name=%s" % ( deploymentTarget, current_deployment_target_name ))
                if -1 != deploymentTarget.find(attr_target):
                    sop(m,"Found associated deployment target.")
                    # Check whether this is the desired deployment target.
                    if None == deploymenttargetname or current_deployment_target_name == deploymenttargetname:
                        valueString = '[[enable "%s"]]' % ( enabled )
                        sop(m,"Setting autostart on desired deployment target. target_mapping=%s and value=%s" % ( targetMapping, valueString ))
                        AdminConfig.modify(targetMapping, valueString)
                        numSet += 1
                    else:
                        sop(m,"Not a desired deployment target.")
                else:
                    sop(m,"Deployment target does not match.")
    else:
        sop(m, "No deployments found.")

    sop(m,"Exit. Set %i deployments." % ( numSet ))
    return numSet


def getDeploymentAutoStart(deploymentname,deploymenttargetname):
    """Returns True (1) or False (0) to indicate whether the specified deployment
    is enabled to start automatically on the specified deployment target.
    Also returns False (0) if the deployment or target is not found, so be careful.
    For example:
        getDeploymentAutoStart('commsvc','server1')
        getDeploymentAutoStart('commsvc','cluster1')
    """
    m = "getDeploymentAutoStart:"
    sop(m,"Entry. deploymentname=%s deploymenttargetname=%s" % ( deploymentname, deploymenttargetname ))

    rc = 0
    found = 0
    deployments = AdminConfig.getid("/Deployment:%s/" % ( deploymentname ))
    if (len(deployments) > 0) :
        deploymentObj = AdminConfig.showAttribute(deployments, 'deployedObject')
        sop(m,"deploymentObj=%s" % ( repr(deploymentObj) ))

        # First get the Target Mappings.  These are the objects where we set enabled/disabled.
        rawTargetMappings = AdminConfig.showAttribute(deploymentObj, 'targetMappings')
        # Convert the single string to a real python list containing strings.
        targetMappingList = stringListToList(rawTargetMappings)
        sop(m, "targetMappingList=%s" % ( repr(targetMappingList) ))

        # Next get the Deployment Targets. These are the objects from which we determine the deployment target name.
        rawDeploymentTargets = AdminConfig.showAttribute(deployments, 'deploymentTargets')
        # Convert the single string to a real python list containing strings.
        deploymentTargetList = stringListToList(rawDeploymentTargets)
        sop(m, "deploymentTargetList=%s" % ( repr(deploymentTargetList) ))

        # Handle each target mapping...
        for targetMapping in targetMappingList:
            if found == 1:
                break
            attr_target = getObjectAttribute(targetMapping,"target")
            attr_enable = getObjectAttribute(targetMapping,"enable")
            sop(m,"targetMapping=%s attr_target=%s attr_enable=%s" % ( targetMapping, attr_target, attr_enable ))

            # Find the associated deployment target object.
            for deploymentTarget in deploymentTargetList:
                current_deployment_target_name = getNameFromId(deploymentTarget)
                sop(m,"deploymentTarget=%s current_deployment_target_name=%s" % ( deploymentTarget, current_deployment_target_name ))
                if -1 != deploymentTarget.find(attr_target):
                    sop(m,"Found associated deployment target.")
                    # Check whether this is the desired deployment target.
                    if current_deployment_target_name == deploymenttargetname:
                        if "true" == attr_enable:
                            rc = 1
                        sop(m,"Found desired deployment target. enable=%s rc=%i" % ( attr_enable, rc ))
                        found = 1
                        break
                    else:
                        sop(m,"Not a desired deployment target.")
                else:
                    sop(m,"Deployment target does not match.")
    else:
        sop(m, "No deployments found.")

    sop(m,"Exit. Returning rc=%i" % ( rc ))
    return rc

def getAdminAppViewValue(appname, keyname, parsename):
    """This helper method returns the value for the specified application
    and key name, as fetched by AdminApp.view().

    Parms: appname - the name of the application
           keyname - the name of the app parameter, eg CtxRootForWebMod
           parsename - the string used to parse results, eg Context Root:
    For example, getAdminApp.view('wussmaster',"-CtxRootForWebMod","Context Root:")

    This method is useful because AdminApp.view() returns a verbose
    multi-line string intended for human viewing and consumption, for example:
        CtxRootForWebMod: Specify the Context root of web module

        Configure values for context roots in web modules.

        Web module:  wussmaster
        URI:  wussmaster.war,WEB-INF/web.xml
        Context Root:  wussmaster
    This method returns a short string useful programmatically.

    Returns None if trouble is encountered."""
    m = "getAdminAppViewValue:"
    sop(m,"Entry. appname=%s keyname=%s parsename=%s" % ( appname, keyname, parsename ))

    # Fetch a verbose human-readable string from AdminApp.view()
    verboseString = AdminApp.view(appname, [keyname])
    sop(m,"verboseString=%s" % ( verboseString ))
    verboseStringList = _splitlines(verboseString)
    for str in verboseStringList:
        #sop("","str=>>>%s<<<" % ( str ))
        if str.startswith(parsename):
            resultString = str[len(parsename):].strip()
            sop(m,"Exit. Found value. Returning >>>%s<<<" % ( resultString ))
            return resultString

    sop(m,"Exit. Did not find value. Returning None.")
    return None

def getApplicationContextRoot(appname):
    """Returns the context root value for the specified application.
    Returns None if trouble is encountered."""
    return getAdminAppViewValue(appname, "-CtxRootForWebMod", "Context Root:")

def setApplicationContextRoot(appname, ctxroot):
    """Sets the Context Root value for an application.
    Uses default/wildcard values for the webmodule name and URI."""
    AdminApp.edit(appname, ['-CtxRootForWebMod',  [['.*', '.*', ctxroot]]])

def getApplicationVirtualHost(appname):
    """Returns the virtual host value for the specified application.
    Returns None if trouble is encountered."""
    return getAdminAppViewValue(appname, "-MapWebModToVH", "Virtual host:")

############################################################
# misc methods

def translateToCygwinPath(platform,mechanism,winpath):
    m = "translateToCygwinPath"
    if platform=='win' and mechanism=='ssh':
        windir = '/cygdrive'
        list1 = []
        found = winpath.find('\\')
        if found != -1:
            list2 = winpath.split('\\')
            for i in list2:
                list1.append(i.replace(':',''))
            for j in list1:
                windir += '/' + j
        return windir
    else:
        sop(m,"No translation neccessary. Platform=%s , Mechanism=%s" % (platform,mechanism))
        sop(m,"Returning original Windows path: %s" % (winpath))
        return winpath

base_nd_flag = ""
def isND():
    """Are we connected to a ND system"""
    global base_nd_flag
    if base_nd_flag == "":
        _whatEnv()
    return base_nd_flag == 'nd'

def isBase():
    """Are we connected to a base system"""
    global base_nd_flag
    if base_nd_flag == "":
        _whatEnv()
    return base_nd_flag == 'base'

def _whatEnv():
    """Not user callable
    Sets some flags that other things use"""
    m = "_whatEnv:"


    global base_nd_flag
    base_nd_flag = whatEnv()

def getCellName():
    """Return the name of the cell we're connected to"""
    # AdminControl.getCell() is simpler, but only
    # available if we're connected to a running server.
    cellObjects = getObjectsOfType('Cell')  # should only be one
    cellname = getObjectAttribute(cellObjects[0], 'name')
    return cellname

def getCellId(cellname = None):
    """Return the config object ID of the cell we're connected to"""
    if cellname == None:
        cellname = getCellName()
    return AdminConfig.getid( '/Cell:%s/' % cellname )

def getNodeName(node_id):
    """Get the name of the node with the given config object ID"""
    return getObjectAttribute(node_id, 'name')

def getNodeVersion(nodename):
    """Return the WebSphere version of the node - e.g. "6.1.0.0"  - only we're connected
    to a running process.
    If we ever need it to work when server is not running, we can get at least
    minimal information from the node-metadata.properties file in the node's
    config directory."""
    sop("getNodeVersion:", "AdminTask.getNodeBaseProductVersion('[-nodeName %s]')" %  nodename )
    version = AdminTask.getNodeBaseProductVersion(  '[-nodeName %s]' %  nodename   )
    return version

def getServerVersion(nodename,servername):
    """return a multi-line string with version & build info for the server.
    This only works if the server is running, since it has to access its mbean.
    If we ever need it to work when server is not running, we can get at least
    minimal information from the node-metadata.properties file in the node's
    config directory.

    Output looks like:
    IBM WebSphere Application Server Version Report
   ---------------------------------------------------------------------------

        Platform Information
        ------------------------------------------------------------------------

                Name: IBM WebSphere Application Server
                Version: 5.0


        Product Information
        ------------------------------------------------------------------------

                ID: BASE
                Name: IBM WebSphere Application Server
                Build Date: 9/11/02
                Build Level: r0236.11
                Version: 5.0.0


        Product Information
        ------------------------------------------------------------------------

                ID: ND
                Name: IBM WebSphere Application Server for Network Deployment
                Build Date: 9/11/02
                Build Level: r0236.11
                Version: 5.0.0

   ---------------------------------------------------------------------------
   End Report
   ---------------------------------------------------------------------------
   """
    mbean = AdminControl.queryNames('type=Server,node=%s,name=%s,*' % (nodename,servername))
    if mbean:
        return AdminControl.getAttribute(mbean, 'serverVersion')
    else:
        return "Cannot get version for %s %s because it's not running" % (nodename,servername)

def parseVersion(stringVersion):
    """Parses a version string like "6.1.0.3" and
       returns a python list of ints like [ 6,1,0,3 ]"""
    m = "parseVersion:"
    # sop(m,"Entry. stringVersion=%s" % ( stringVersion ))
    listVersion = []
    parts = stringVersion.split('.')
    for part in parts:
        # sop(m,"Adding part=%s" % part)
        listVersion.append(int(part))
    # sop(m,"Exit. Returning listVersion=%s" % ( listVersion ))
    return listVersion

def compareIntLists(a, b):
    """Compares two python lists containing ints.
       Handles arrays of different lengths by padding with zeroes.
       Returns -1 if array a is less than array b. For example,
          [6,0,0,13] < [6,1]
       Returns 0 if they're the same.  For example,
           [7,0] = [7,0,0,0]
       Returns +1 if array a is greater than array b.  For example,
           [8,0,0,12] > [8,0,0,11]
       Note: This method was fixed and completely rewritten 2011-0121"""
    m = "compareIntLists:"
    sop(m,"Entry. a=%s b=%s" % ( a, b, ))
    # Make both arrays the same length. Pad the smaller array with trailing zeroes.
    while (len(a) < len(b)):
        a.append(0)
    while (len(b) < len(a)):
        b.append(0)

    # Compare each element in the arrays.
    rc = cmp(a,b)

    sop(m,"Exit. Returning %i" % ( rc ))
    return rc

def versionAtLeast(nodename, stringVersion):
    """Returns true if the version we're running is greater than or equal to the version passed in.

       For example, pass in '6.1.0.13.
       If we're running 6.1.0.13, or 7.0.0.0, it returns true.
       If we're running 6.1.0.12, it returns false."""
    # m = "versionAtLeast:"
    # sop(m,"Entry. nodename=%s stringVersion=%s" % (nodename,stringVersion))
    x = compareIntLists(parseVersion(getNodeVersion(nodename)),parseVersion(stringVersion))
    # sop(m,"Exit. x=%i Returning %s" % ( x, (x>=0) ))
    return (x >= 0)

def versionLessThan(nodename, stringVersion):
    """Returns true if the version we're running is less than the version passed in."""
    return (not versionAtLeast(nodename,stringVersion))

def registerWithJobManager(jobManagerPort, managedNodeName, jobManagerHostname = None):
    """Registers the managed node with the job manager.

    Prereqs:
    - wsadmin must be connected to the AdminAgent or Dmgr for this to work.
    - The JobManager must be running.

    Args:
    jobManagerPort is a string containing the SOAP port of the running JobManager.
    managedNodeName is the node name of the dmgr, appserver, or secureproxy to be managed.
      For example, when registering a dmgr to the jobmgr, specify the dmgr node name.

    Results:
    Returns a long cryptic string of unknown purpose, eg:
    JobMgr01-JOB_MANAGER-59195a7e-fa61-4f48-93a6-be8dc8562447"""
    m = "registerWithJobManager:"
    sop(m,"Entry. jobManagerHostname=%s jobManagerPort=%s managedNodeName=%s" % (jobManagerHostname, jobManagerPort, managedNodeName))
    if None == jobManagerHostname:
        argString = '[-port %s -managedNodeName %s]' % (jobManagerPort, managedNodeName)
    else:
        argString = '[-host %s -port %s -managedNodeName %s]' % (jobManagerHostname, jobManagerPort, managedNodeName)
    sop(m,"Calling AdminTask.registerWithJobManager. argString=%s" % ( argString ))
    rc = AdminTask.registerWithJobManager( argString )
    sop(m,"Exit. rc=%s" % ( rc ))
    return rc

def submitJobCreateProxyServer(dmgrNodeName, proxyNodeName, proxyName):
    """Submits a job to create a proxy server.

    Prereqs:
    - The manager of the target (adminagent or dmgr) must have been registered with the JobManager.
    - wsadmin must be connected to the JobManager for this command to work."""
    m = "submitJobCreateProxyServer:"
    sop(m,"Entry. dmgrNodeName=%s proxyNodeName=%s proxyName=%s" % (dmgrNodeName, proxyNodeName, proxyName))
    jobToken = submitJob('createProxyServer', dmgrNodeName, '[[serverName %s] [nodeName %s]]' % ( proxyName, proxyNodeName ))
    sop(m,"Exit. Returning jobToken=%s" % ( jobToken ))
    return jobToken

def submitJobDeleteProxyServer(dmgrNodeName, proxyNodeName, proxyName):
    """Submits a job to delete a proxy server.

    Prereqs:
    - The manager of the target (adminagent or dmgr) must have been registered with the JobManager.
    - wsadmin must be connected to the JobManager for this command to work."""
    m = "submitJobDeleteProxyServer:"
    sop(m,"Entry. dmgrNodeName=%s proxyNodeName=%s proxyName=%s" % (dmgrNodeName, proxyNodeName, proxyName))
    jobToken = submitJob('deleteProxyServer', dmgrNodeName, '[[serverName %s] [nodeName %s]]' % ( proxyName, proxyNodeName ))
    sop(m,"Exit. Returning jobToken=%s" % ( jobToken ))
    return jobToken

def submitJobStartServer(dmgrNodeName, serverNodeName, serverName):
    """Submits a job to start a server.

    Prereqs:
    - The manager of the target (adminagent or dmgr) must have been registered with the JobManager.
    - wsadmin must be connected to the JobManager for this command to work."""
    m = "submitJobStartServer:"
    sop(m,"Entry. dmgrNodeName=%s serverNodeName=%s serverName=%s" % (dmgrNodeName, serverNodeName, serverName))
    jobToken = submitJob('startServer', dmgrNodeName, '[[serverName %s] [nodeName %s]]' % ( serverName, serverNodeName ))
    sop(m,"Exit. Returning jobToken=%s" % ( jobToken ))
    return jobToken

def submitJobStopServer(dmgrNodeName, serverNodeName, serverName):
    """Submits a job to stop a server.

    Prereqs:
    - The manager of the target (adminagent or dmgr) must have been registered with the JobManager.
    - wsadmin must be connected to the JobManager for this command to work."""
    m = "submitJobStopServer:"
    sop(m,"Entry. dmgrNodeName=%s serverNodeName=%s serverName=%s" % (dmgrNodeName, serverNodeName, serverName))
    jobToken = submitJob('stopServer', dmgrNodeName, '[[serverName %s] [nodeName %s]]' % ( serverName, serverNodeName ))
    sop(m,"Exit. Returning jobToken=%s" % ( jobToken ))
    return jobToken

def submitJob(jobType, targetNode, jobParams):
    """Submits a job to the Job Manager.

    Prereqs:
    - The manager of the target (adminagent or dmgr) must have been registered with the JobManager.
    - wsadmin must be connected to the JobManager for this command to work.

    Args:
    - String jobType - eg 'createProxyServer'
    - String targetNode - the node name where the job will be performed, eg ding3Node
    - String jobParams - parameters specific to the jobType
        For example, createProxyServer requires a string which looks like a list of lists: "[[serverName proxy1] [nodeName ding3Node]]"
    Results:
    - Returns a string jobToken, which is a big number.  eg, 119532775201403043
    - This number is useful to subsequently query status of the submitted job."""
    m = "submitJob:"
    sop(m,"Entry. jobType=%s targetNode=%s jobParams=%s" % (jobType, targetNode, jobParams))
    # wsadmin>jobToken = AdminTask.submitJob('[-jobType createProxyServer -targetList [ding3Node03] -jobParams "[serverName proxy01 nodeName ding3Node]"]')
    argString = '[-jobType %s -targetList [%s] -jobParams "%s"]' % (jobType, targetNode, jobParams)
    sop(m,"Calling AdminTask.submitJob with argString=%s" % ( argString ))
    jobToken = AdminTask.submitJob( argString )
    sop(m,"Exit. Returning jobToken=%s" % ( jobToken ))
    return jobToken

def getOverallJobStatus(jobToken):
    """Queries the JobManager for the status of a specific job.

    Prereqs:
    - wsadmin must be connected to the jobmanager.
    - A job has already been submitted.

    Arg:
    - jobToken is a string containing a big number.
    - The jobToken was returned from a submitJob command.

    Results:
    - Returns a python dictionary object with results.
    - For example: {  "NOT_ATTEMPTED": "0",
                      "state": "ACTIVE",
                      "FAILED":, "1",
                      "DISTRIBUTED": "0",  }"""
    m = "getOverallJobStatus:"
    sop(m,"Entry. jobToken'%s" % ( jobToken ))
    uglyResultsString = AdminTask.getOverallJobStatus('[-jobTokenList[%s]]' % ( jobToken ))
    resultsDict = stringListListToDict(uglyResultsString)
    sop(m,"Exit. resultsDict=%s" % ( repr(resultsDict) ))
    return resultsDict

def waitForJobSuccess(jobToken,timeoutSecs):
    """Repeatedly queries the JobManager for status of a specific job.

    Works for single jobs only.  Tests for SUCCEEDED=1
    Returns True if the job completes successfully.
    Returns False if the job fails, is rejected, or times out."""
    m = "waitForJobSuccess:"
    sop(m,"Entry. jobToken=%s timeoutSecs=%d" % (jobToken,timeoutSecs))

    # Calculate sleep times.
    # Inexact, but good enough.  Ignores the time it takes for the query to come back.
    if timeoutSecs < 240:  # up to 4 minutes, sleep 30 seconds each cycle.
        sleepTimeSecs = 30
        numSleeps = 1 + timeoutSecs / sleepTimeSecs
    else:   # over 4 minutes, sleep 8 times
        numSleeps = 8
        sleepTimeSecs = timeoutSecs / numSleeps
    sop(m,"sleepTimeSecs=%d numSleeps=%d" % ( sleepTimeSecs, numSleeps ))

    # Query repeatedly.
    while numSleeps > 0:
        # Sleep
        sop(m,"Sleeping %s seconds. numSleeps=%d" % ( sleepTimeSecs, numSleeps ))
        time.sleep(sleepTimeSecs)
        # Query
        statusDict = getOverallJobStatus(jobToken)
        if statusDict['SUCCEEDED'] == "1":
            sop(m,"Job succeeded. Returning true.")
            return True
        if statusDict['FAILED'] == "1":
            sop(m,"Job failed. Returning false.")
            return False
        if statusDict['REJECTED'] == "1":
            sop(m,"Job was rejected. Returning false.")
            return False
        # Next attempt
        numSleeps = numSleeps - 1

    # No luck.
    sop(m,"No success yet. Timeout expired. Returning false. statusDict=%s" % ( statusDict ))
    return False


def getNodeHostname(nodename):
    """Get the hostname of the named node"""
    return AdminConfig.showAttribute(getNodeId(nodename),'hostName')

def getShortHostnameForProcess(process):
    nodename = getNodeNameForProcess(process)
    hostname = getNodeHostname(nodename)
    if hostname.find('.') != -1:
        hostname = hostname[:hostname.find('.')]
    return hostname

def findNodeOnHostname(hostname):
    """Return the node name of a (non-dmgr) node on the given hostname, or None"""
    m = "findNodeOnHostname:"
    for nodename in listNodes():
        if hostname.lower() == getNodeHostname(nodename).lower():
            return nodename

    # Not found - try matching without domain - z/OS systems might not have domain configured
    shorthostname = hostname.split(".")[0].lower()
    for nodename in listNodes():
        shortnodehostname = getNodeHostname(nodename).split(".")[0].lower()
        if shortnodehostname == shorthostname:
            return nodename

    sop(m,"WARNING: Unable to find any node with the hostname %s (not case-sensitive)" % hostname)
    sop(m,"HERE are the hostnames that your nodes think they're on:")
    for nodename in listNodes():
        sop(m,"\tNode %s: hostname %s" % (nodename, getNodeHostname(nodename)))
    return None

def changeHostName(hostName, nodeName):
    """ This method encapsulates the actions needed to modify the hostname of a node.

    Parameters:
        hostName - The string value of a new host name.
        nodeName - The string value of the node whose host name will be changed.
    Returns:
        No return value
    """
    AdminTask.changeHostName(["-hostName", hostName, "-nodeName", nodeName])

def getNodePlatformOS(nodename):
    """Get the OS of the named node (not sure what format it comes back in).
    Some confirmed values: 'linux', 'windows', 'os390', 'solaris', 'hpux'
    I think these come from node-metadata.properties, com.ibm.websphere.nodeOperatingSystem;
    on that theory, AIX should return 'aix'.
    """
    return AdminTask.getNodePlatformOS('[-nodeName %s]' % nodename)

def getNodeNameList(platform=None,servertype=None):
    """Returns a list with node names which match the specified platform and server type.
    Parameter 'platform' may be linux, windows, os390, etc. It defaults to return all platforms.
    Parameter 'servertype' may be APPLICATION_SERVER, PROXY_SERVER, etc. It defaults to all types.
    For example, this returns a list of proxies on hpux:
        hpuxProxyList = getNodeNameList(platform='hpux',servertype='PROXY_SERVER')
    """
    m = "getNodeNameList: "
    #sop(m,"Entry. platform=%s servertype=%s" % ( platform, servertype ))

    nodelist = []
    serverlist = listServersOfType(servertype)
    #sop(m,"serverlist=%s" % ( repr(serverlist) ))
    for (nodename,servername) in serverlist:
        actual_platform = getNodePlatformOS(nodename)
        #sop(m,"nodename=%s servername=%s actual_platform=%s" % ( nodename, servername, actual_platform ))
        if None == platform or actual_platform == platform:
            if nodename not in nodelist:
                nodelist.append(nodename)
                #sop(m,"Saved node")

    #sop(m,"Exit. Returning nodelist=%s" % ( repr(nodelist) ))
    return nodelist

def getNodeId( nodename ):
    """Given a node name, get its config ID"""
    return AdminConfig.getid( '/Cell:%s/Node:%s/' % ( getCellName(), nodename ) )

def getNodeIdWithCellId ( cellname, nodename ):
     """Given a cell name and node name, get its config ID"""
     return AdminConfig.getid( '/Cell:%s/Node:%s/' % ( cellname, nodename ) )

def getNodeVariable(nodename, varname):
    """Return the value of a variable for the node -- or None if no such variable or not set"""
    return getWebSphereVariable(varname, nodename)

def getWasInstallRoot(nodename):
    """Return the absolute path of the given node's WebSphere installation"""
    return getWebSphereVariable("WAS_INSTALL_ROOT", nodename)

def getWasProfileRoot(nodename):
    """Return the absolute path of the given node's profile directory"""
    return getWebSphereVariable("USER_INSTALL_ROOT", nodename)

def getServerId(nodename,servername):
    """Return the config id for a Server."""
    #TODO: This mirrors the functionality of getServerByNodeAndName(). However, getServerId() is used elsewhere in the library.
    return getServerByNodeAndName(nodename, servername)

def getObjectByNodeServerAndName( nodename, servername, typename, objectname ):
    """Get the config object ID of an object based on its node, server, type, and name"""
    m = "getObjectByNodeServerAndName:"
    #sop(m,"Entry. nodename=%s servername=%s typename=%s objectname=%s" % ( repr(nodename), repr(servername), repr(typename), repr(objectname), ))
    node_id = getNodeId(nodename)
    #sop(m,"node_id=%s" % ( repr(node_id), ))
    all = _splitlines(AdminConfig.list( typename, node_id ))
    result = None
    for obj in all:
        #sop(m,"obj=%s" % ( repr(obj), ))
        name = AdminConfig.showAttribute( obj, 'name' )
        if name == objectname:
            #sop(m,"Found sought name=%s objectname=%s" % ( repr(name), repr(objectname), ))
            if -1 != repr( obj ).find( 'servers/' + servername + '|' ):
                #sop(m,"Found sought servername=%s" % ( repr(servername), ))
                if result != None:
                    raise AssertionError("FOUND more than one %s with name %s" % ( typename, objectname ))
                result = obj
    #sop(m,"Exit. result=%s" % ( repr(result), ))
    return result

def getObjectByNodeAndName( nodename, typename, objectname ):
    """Get the config object ID of an object based on its node, type, and name"""
    # This version of getObjectByName distinguishes by node,
    # which should disambiguate some things...
    node_id = getNodeId(nodename)
    all = _splitlines(AdminConfig.list( typename, node_id ))
    result = None
    for obj in all:
        name = AdminConfig.showAttribute( obj, 'name' )
        if name == objectname:
            if result != None:
                raise "FOUND more than one %s with name %s" % ( typename, objectname )
            result = obj
    return result

def getScopeId (scope, serverName, nodeName, clusterName):
    """useful when scope is a required parameter for a function"""
    if scope == "cell":
        try:
            result = getCellId()
        except:
            my_sep(sys.exc_info())
        # end except
    elif scope == "node":
        if nodeName == "":
            my_sep(sys.exc_info())
        else:
            try:
                result = getNodeId(nodeName)
            except:
                my_sep(sys.exc_info())
            # end except
        # end else
    elif scope == "server":
        if nodeName == "" or serverName == "":
            my_sep(sys.exc_info())
        else:
            try:
                result = getServerId(nodeName, serverName)
            except:
                my_sep(sys.exc_info())
            # end except
        # end else
    elif scope == "cluster":
        if clusterName == "":
            my_sep(sys.exc_info())
        else:
            try:
                result = getClusterId(clusterName)
            except:
                my_sep(sys.exc_info())
            # end except
        # end else
    else:  # Invalid scope specified
         my_sep(sys.exc_info())
    # end else

    return result

def getServerJvmByRegion(nodename,servername,region):
    """Return the config ID of the JVM object for this server by region.
       p.find('JavaProcessDef') returns 3 JavaVirtualMachine objects.
           1st object: ControlRegion
           2nd object: ServantRegion
           3rd object: AdjunctRegion
    """
    regionnumber = 0
    counter = 0

    # Define regions: 1 = control, 2 = servant, 3 = adjunct
    if region == 'control':
        regionnumber = 1
    elif region == 'servant':
        regionnumber = 2
    else:
        regionnumber = 3

    server_id = getServerId(nodename,servername)
    pdefs = _splitlist(AdminConfig.showAttribute(server_id, 'processDefinitions'))
    pdef = None
    for p in pdefs:
        if -1 != p.find("JavaProcessDef"):
            counter = counter + 1
            if regionnumber == counter:
                pdef = p
                break
    if pdef: # found Java ProcessDef
        return _splitlist(AdminConfig.showAttribute(pdef, 'jvmEntries'))[0]

def createJvmPropertyByRegion(nodename,servername,region,name,value):
    jvm = getServerJvmByRegion(nodename,servername,region)
    attrs = []
    attrs.append( [ 'name', name ] )
    attrs.append( ['value', value] )
    return removeAndCreate('Property', jvm, attrs, ['name'])


def getServerJvm(nodename,servername):
    """Return the config ID of the JVM object for this server"""
    server_id = getServerId(nodename,servername)
    # the pdefs come back as a string [item item item]
    pdefs = _splitlist(AdminConfig.showAttribute(server_id, 'processDefinitions'))
    pdef = None
    for p in pdefs:
        if -1 != p.find("JavaProcessDef"):
            pdef = p
            break
    if pdef: # found Java ProcessDef
        return _splitlist(AdminConfig.showAttribute(pdef, 'jvmEntries'))[0]

def getServerServantJvm(nodename,servername):
    """Return the config ID of the JVM object for this server"""
    server_id = getServerId(nodename,servername)
    # the pdefs come back as a string [item item item]
    #pdefs = _splitlist(AdminConfig.showAttribute(server_id, 'processDefinitions'))
    pdefs = AdminConfig.list('JavaVirtualMachine', server_id)
    return pdefs.splitlines()[1]

def getServerJvmExecution(nodename,servername):
    """Return the config ID of the JVM execution object  for this server"""
    server_id = getServerId(nodename,servername)
    # the jpdefs don't come back as a string [item item item]
    jpdef = AdminConfig.list('JavaProcessDef', server_id)
    if jpdef: # found Java ProcessDef
        return AdminConfig.showAttribute(jpdef, 'execution')

def getServerJvmProcessDef (nodename, servername):
    """This function returns the config ID for the Java Process Definition
       for the specified server.

    Input:

      - nodename - node name for the server whose Java Process Definition is
                   to be returned.

      - servername - name of the server whose Java Process Definition is
                     to be returned.

    Output:

      - The config ID for the Java Process Definition for the specified server.
    """

    m = 'getServerJvmProcessDef:'
    sop(m, 'Entering function')

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            # the jpdefs don't come back as a string [item item item]

            sop (m, "Calling AdminConfig.list to get JavaProcessDef")
            jpdef = AdminConfig.list('JavaProcessDef', serverID)
            sop (m, "Returned from AdminConfig.list")
            sop (m, "Exiting function...")
            return jpdef
        #endif
    #endif
#enddef

def setJvmExecutionUserGroup(nodename,servername,user,group):
    """Configure additional process execution settings

    Parameters:
        nodename - Name of node in String format.
        servername - Name of server in String format.
        user - Name of the user that the process runs as in String format.
        group - Name of the group that the process is a member of and runs as in String format.
    Returns:
        No return value
    """
    m = "setJvmExecutionRunAs: "
    sop(m,"Run as User:%s, Group:%s" % (user,group))
    execution = getServerJvmExecution(nodename,servername)

    params = [['runAsUser',user], ['runAsGroup',group]]
    AdminConfig.modify(execution,params)

def setSipStackProperties(nodename,servername,props):
    """SET properties on the SIP Stack object of a server
    For example, set MTU to set the SIP stack size.
    props should be a dictionary."""
    # The Stack object we want is a child of the SIPContainer
    m = "setSipStackProperties:"
    sop(m,"%s,%s,%s" % (nodename,servername,props))
    container = getObjectsOfType('SIPContainer', getServerId(nodename,servername))[0]
    #sop(m,"container=%s" % container)
    stack = getObjectsOfType('Stack', container)[0]
    #sop(m,"stack=%s" % stack)
    for key in props.keys():
        if key == 'sip_stack_timers':
            setSipStackTimers(stack,props[key])
        else:
            AdminConfig.modify(stack, [[key, props[key] ]])
            sop(m,"set %s=%s" % (key,props[key]))
    #sop(m,"Exit")

def setSipStackTimers(stack,props):
    """SET timer values on the SIP Stack object of a server
    Arg props should be a dictionary.
    For example, { 'timerT4': '3333' }"""
    m = "setSipStackTimers:"
    sop(m,"Entry. %s,%s" % (stack,props))

    # Get the Timers owned by the SIP Stack
    timers = getObjectsOfType('Timers', stack)[0]
    sop(m,"timers=%s" % timers)

    for key in props.keys():
        sop(m,"Setting timer. name=%s value=%s" % (key,props[key]))
        AdminConfig.modify(timers, [[key, props[key] ]])
    sop(m,"Exit")

# TODO: see also Commands for the ServerManagement group of the AdminTask object
# http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/topic/com.ibm.websphere.nd.doc/info/ae/ae/rxml_atservermanagement.html
# which has some higher-level JVM configuration commands we might be able to use
def setJvmProperty(nodename,servername,propertyname,value):
    """Set a particular JVM property for the named server.
Some useful examples:
        'maximumHeapSize': 512 ,
        'initialHeapSize':512,
        'verboseModeGarbageCollection':"true",
        'genericJvmArguments':"-Xgcpolicy:gencon -Xdump:heap:events=user -Xgc:noAdaptiveTenure,tenureAge=8,stdGlobalCompactToSatisfyAllocate -Xconcurrentlevel1 -Xtgc:parallel",

    """
    jvm = getServerJvm(nodename,servername)
    AdminConfig.modify(jvm, [[propertyname, value]])

def removeJvmProperty(nodename,servername,propertyname):
    jvm = getServerJvm(nodename,servername)
    if getNodePlatformOS(nodename) == "os390":
        jvm = getServerServantJvm(nodename,servername)
    findAndRemove('Property', [['name', propertyname]], jvm)

def createJvmProperty(nodename,servername,name,value):
    jvm = getServerJvm(nodename,servername)
    if getNodePlatformOS(nodename) == "os390":
        jvm = getServerServantJvm(nodename,servername)
    attrs = []
    attrs.append( [ 'name', name ] )
    attrs.append( ['value', value] )
    return removeAndCreate('Property', jvm, attrs, ['name'])

def createJvmEnvEntry (nodename, servername, name, value):
    """This function creates an Environment Entry on the Java Process Definition
       for the specified server.

    Input:

      - nodename - node name for the server where the Environment Entry is to be created.

      - servername - name of the server where the Environment Entry is to be created.

      - name - name of the Environment Entry that is to be created.

      - value - value of the Environment Entry that is to be created.

    Output:

      - The config ID for the newly-created Environment Entry.
    """

    m = 'createJvmEnvEntry:'
    sop(m, 'Entering function')

    sop (m, "Calling getServerJvmProcessDef() with nodename = %s and servername = %s." % (nodename, servername))
    jpdef = getServerJvmProcessDef(nodename, servername)
    sop (m, "Returned from getServerJvmProcessDef; returned jpdef = %s" % jpdef)

    attrs = []
    attrs.append( [ 'name', name ] )
    attrs.append( ['value', value] )

    sop (m, "Calling removeAndCreate; returning from this function with the value returned by removeAndCreate.")
    return removeAndCreate('Property', jpdef, attrs, ['name'])
#enddef


def modifyGenericJvmArguments(nodename,servername,value):
    jvm = getServerJvm(nodename,servername)
    AdminConfig.modify(jvm, [['genericJvmArguments', value]])

############################################################
# misc methods

def getObjectByName( typename, objectname ):
    """Get an object of a given type and name - WARNING: the object should be unique in the cell; if not, use getObjectByNodeAndName instead."""
    all = _splitlines(AdminConfig.list( typename ))
    result = None
    for obj in all:
        name = AdminConfig.showAttribute( obj, 'name' )
        if name == objectname:
            if result != None:
                raise "FOUND more than one %s with name %s" % ( typename, objectname )
            result = obj
    return result

def getEndPoint( nodename, servername, endPointName):
    """Find an endpoint on a given server with the given endpoint name and return endPoint object config ID"""
    node_id = getNodeId(nodename)
    if node_id == None:
        raise "Could not find node: name=%s" % nodename
    server_id = getServerId(nodename, servername)
    if server_id == None:
        raise "Could not find server: node=%s name=%s" % (nodename,servername)
    serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))

    serverName = AdminConfig.showAttribute( server_id, "name" )

    for serverEntry in serverEntries:
        #print "serverEntry: %s" % serverEntry
        sName = AdminConfig.showAttribute( serverEntry, "serverName" )
        #print "looking at server %s" % sName
        if sName == serverName:
            specialEndPoints = AdminConfig.showAttribute( serverEntry, "specialEndpoints" )
            # that returned a string like [ep1 ep2 ep3], sigh - strip the
            # [] and split on ' '
            specialEndPoints = specialEndPoints[1:len( specialEndPoints )-1].split( " " )
            #print "specialEndPoints=%s" % repr(specialEndPoints)

            for specialEndPoint in specialEndPoints:
                endPointNm = AdminConfig.showAttribute( specialEndPoint, "endPointName" )
                #print "endPointNm=%s" % endPointNm
                if endPointNm == endPointName:
                    #print "serverEntry = %s" % serverEntry
                    return AdminConfig.showAttribute( specialEndPoint, "endPoint" )
    # Don't complain - caller might anticipate this and handle it
    #print "COULD NOT FIND END POINT '%s' on server %s" % ( endPointName, serverName )
    return None

def getObjectNameList( typename ):
    """Returns list of names of objects of the given type"""
    m = "getObjectNameList:"
    #sop(m,"Entry. typename=%s" % ( repr(typename) ))
    ids = _splitlines(AdminConfig.list( typename ))
    result = []
    for id in ids:
        result.append(AdminConfig.showAttribute(id,"name"))
    #sop(m,"Exit. result=%s" % ( repr(result) ))
    return result

def deleteAllObjectsByName( objectname ):
    """Deletes all objects of the specified name."""
    m = "deleteAllObjectsByName:"
    #sop(m,"Entry. objectname=%s" % ( repr(objectname) ))
    obj_ids = getObjectNameList( objectname )
    for obj_id in obj_ids:
        sop(m,"Deleting %s: %s" % ( repr(objectname), repr(obj_id) ))
        id = getObjectByName( objectname, obj_id )
        sop(m,"Deleting id=%s" % ( repr(id) ))
        AdminConfig.remove( id )
    #sop(m,"Exit")

def findSpecialEndPoint( nodename, servername, port ):
    """Return endPoint object config ID"""
    node_id = getNodeId(nodename)
    if node_id == None:
        raise "Could not find node: name=%s" % nodename
    server_id = getServerId(nodename, servername)
    if server_id == None:
        raise "Could not find server: node=%s name=%s" % (nodename,servername)
    serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))

    serverName = AdminConfig.showAttribute( server_id, "name" )

    for serverEntry in serverEntries:
        #print "serverEntry: %s" % serverEntry
        sName = AdminConfig.showAttribute( serverEntry, "serverName" )
        #print "looking at server %s" % sName
        if sName == serverName:
            specialEndPoints = AdminConfig.showAttribute( serverEntry, "specialEndpoints" )
            # that returned a string like [ep1 ep2 ep3], sigh - strip the
            # [] and split on ' '
            specialEndPoints = specialEndPoints[1:len( specialEndPoints )-1].split( " " )
            #print "specialEndPoints=%s" % repr(specialEndPoints)

            for specialEndPoint in specialEndPoints:
                endPoint = AdminConfig.showAttribute(specialEndPoint, "endPoint")
                portnum = int(AdminConfig.showAttribute(endPoint, "port"))
                if portnum == port:
                    return specialEndPoint
    #print "COULD NOT FIND END POINT '%s' on server %s" % ( endPointName, serverName )
    return None

def getServerEndPointNames( nodename, servername ):
    """Return list of server end point names"""
    node_id = getNodeId(nodename)
    server_id = getServerId(nodename, servername)

    result = []
    serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))

    serverName = AdminConfig.showAttribute( server_id, 'name' )

    for serverEntry in serverEntries:
        #print "serverEntry: %s" % serverEntry
        sName = AdminConfig.showAttribute( serverEntry, "serverName" )
        if sName == serverName:
            specialEndPoints = AdminConfig.showAttribute( serverEntry, "specialEndpoints" )
            # that returned a string like [ep1 ep2 ep3], sigh - strip the
            # [] and split on ' '
            specialEndPoints = specialEndPoints[1:len( specialEndPoints )-1].split( " " )
            #print specialEndPoints

            for specialEndPoint in specialEndPoints:
                endPointNm = AdminConfig.showAttribute( specialEndPoint, "endPointName" )
                result.append( endPointNm )
            break
    return result

def getServerHostnamePort( nodename, servername, endPointName ):
    """Return [host,portnum] for the given endpoint.
    e.g. getServerHostnamePort(node_id, server_id, 'SIP_DEFAULTHOST')"""
    ePoint = getEndPoint(nodename = nodename,
                         servername = servername,
                         endPointName = endPointName )
    if ePoint == None:
        raise Exception("COULD NOT FIND END POINT %s" % endPointName)
    host = AdminConfig.showAttribute( ePoint, "host" )
    port = AdminConfig.showAttribute( ePoint, "port" )
    return ( host, port )

def getServerPort( nodename, servername, endPointName ):
    """Return port number for the given endpoint.
    e.g. getServerHostnamePort(node_id, server_id, 'SIP_DEFAULTHOST')"""
    return getServerHostnamePort(nodename,servername,endPointName)[1]

def setServerPort( nodename, servername, endPointName, port, hostname = None ):
    """Change a named port number on a server.
    E.g. setServerPort('xxxNode', 'proxy1', 'SIP_DEFAULTHOST', 5678).
    Also changes the hostname.  If hostname is None, sets it to "*" (all hosts)
    """

    # First see if port number is in use already
    specialEndPoint = findSpecialEndPoint(nodename = nodename,
                                          servername = servername,
                                          port = port)
    if specialEndPoint:
        name = AdminConfig.showAttribute(specialEndPoint, "endPointName")
        if name != endPointName:
            # DUP!
            raise Exception("ERROR: node %s, server %s: trying to set endpoint %s to port number %d, but endpoint %s is already using that port" % (nodename,servername,endPointName,port,name))


    ePoint = getEndPoint( nodename = nodename,
                          servername = servername,
                          endPointName = endPointName)
    if ePoint == None:
        raise Exception("COULD NOT FIND END POINT %s" % endPointName)
    AdminConfig.modify( ePoint, [['port', "%d"%port]] )
    if hostname == None:
        AdminConfig.modify( ePoint, [['host', '*']] )
    else:
        AdminConfig.modify( ePoint, [['host', hostname]] )

def getVirtualHostByName( virtualhostname ):
    """Return the id of the named VirtualHost"""
    hosts = AdminConfig.list( 'VirtualHost' )
    hostlist = _splitlines(hosts)
    for host_id in hostlist:
        name = AdminConfig.showAttribute( host_id, "name" )
        if name == virtualhostname:
            return host_id
    return None

def createVirtualHost(virtualhostname, templatename="default_host"):
    """Create a virtual host and return its ID.
    templatename might be e.g. "default_host" """
    m = "createVirtualHost:"
    sop(m,"virtualhostname=%s templatename=%s" % (virtualhostname,templatename))
    x = AdminConfig.listTemplates('VirtualHost')
    sop(m,"x=%s" % repr(x))
    templates = _splitlines(x)
    sop(m,"templates=%s" % repr(templates))
    # templates look like 'default_host(templates/default:virtualhosts.xml#VirtualHost_1)'
    template_id = None
    for t in templates:
        if t.startswith(templatename + "("):
            template_id = t
            break
    if template_id == None:
        raise "Cannot locate VirtualHost template named %s" % templatename

    sop(m,"template_id = %s" % template_id)
    return AdminConfig.createUsingTemplate('VirtualHost', getCellId(getCellName()), [['name', virtualhostname]], template_id)

def deleteVirtualHost( virtualhostname ):
    """Delete a Virtual Host. Return true if the specified host alias already exists"""
    host_id = getVirtualHostByName( virtualhostname )
    if host_id == None:
        return 0   # can't exist, no such virtual host
    AdminConfig.remove( host_id )
    return 1

def setVirtualHostMimeTypeForExtension(virtualhostname, newExtension, newMimeType):
    """Set MIME-Type for a specified file extension on a specified virtual host"""
    m = "setVirtualHostMimeTypeForExtension:"
    vh = getVirtualHostByName(virtualhostname)
    mimeTypes = AdminConfig.showAttribute(vh, 'mimeTypes')
    mimeTypeArray = mimeTypes[1:-1].split(" ")
    changeCount = 0
    for mimeType in mimeTypeArray:
        ext = AdminConfig.showAttribute(mimeType, 'extensions')
        if ext.find(newExtension) != -1:
            sop(m, "Modifying extensions=%s type=%s" % (newExtension, newMimeType))
            changeCount += 1
            return AdminConfig.modify(mimeType, [['type', newMimeType]])
    if changeCount == 0:
        sop(m, "Creating extensions=%s type=%s" % (newExtension, newMimeType))
        return AdminConfig.create('MimeEntry', vh, [['extensions', newExtension], ['type', newMimeType]])

def hostAliasExists( virtualhostname, aliashostname, port ):
    """Return true if the specified host alias already exists"""
    host_id = getVirtualHostByName( virtualhostname )
    if host_id == None:
        return 0   # can't exist, no such virtual host
    port = "%d" % int(port)   # force port to be a string
    aliases = AdminConfig.showAttribute( host_id, 'aliases' )
    #sop(m,"aliases=%s" % ( repr(aliases) ))
    aliases = aliases[1:-1].split( ' ' )
    #sop(m,"after split, aliases=%s" % ( repr(aliases) ))
    for alias in aliases:
        #sop(m,"alias=%s" % ( repr(alias) ))
        if alias != None and alias != '':
            # Alias is a HostAlias object
            h = AdminConfig.showAttribute( alias, 'hostname' )
            p = AdminConfig.showAttribute( alias, 'port' )
            if ( aliashostname, port ) == ( h, p ):
                # We're good - found what we need
                return 1
    return 0

def getHostAliasID( virtualhostname, aliashostname, port ):
    """Returns the ID string for the specified host alias, if it exists.
    Returns None if it does not exist.      
    Parms:  virtualhostname: "default_host", or "proxy_host", or etc.
            aliashostname:  "*", "fred", "fred.raleigh.ibm.com", etc
            port:  either a string or int: 5060, "5061", etc. """
    m = "getHostAliasID:"
    sop(m,"Entry. virtualhostname=%s aliashostname=%s port=%s" % ( virtualhostname, aliashostname, repr(port) ))
    host_id = getVirtualHostByName( virtualhostname )
    if host_id == None:
        sop(m,"Exit. Virtualhostname %s does not exist. Returning None." % ( virtualhostname ))
        return None   # can't exist, no such virtual host
    port = "%d" % int(port)   # force port to be a string
    aliases = AdminConfig.showAttribute( host_id, 'aliases' )
    #sop(m,"aliases=%s" % ( repr(aliases) ))
    aliases = aliases[1:-1].split( ' ' )
    #sop(m,"after split, aliases=%s" % ( repr(aliases) ))
    for alias in aliases:
        #sop(m,"Considering alias=%s" % ( repr(alias) ))
        if alias != None and alias != '':
            # Alias is a HostAlias object
            h = AdminConfig.showAttribute( alias, 'hostname' )
            p = AdminConfig.showAttribute( alias, 'port' )
            if aliashostname == h and port == p :
                # We're good - found what we need
                sop(m,"Exit. Found host alias. Returning id=%s" % ( alias ))
                return alias
    sop(m,"Exit. Did not find host alias. Returning None.")
    return None

def addHostAlias( virtualhostname, aliashostname, port ):
    """Add new host alias"""
    # Force port to be a string - could be string or int on input
    port = "%d" % int( port )

    print "adding host alias on %s: %s %s" % (virtualhostname, aliashostname, port)
    host_id = getVirtualHostByName(virtualhostname)
    if host_id == None:
        host_id = createVirtualHost(virtualhostname)
    new_alias = AdminConfig.create( 'HostAlias', host_id, [['hostname', aliashostname], ['port', port]] )
    print "alias added for virtualhost %s hostname %s port %s" % (virtualhostname,aliashostname,port)

    configured_port = getObjectAttribute(new_alias, 'port')
    if configured_port != port:
        raise "ERROR: requested host alias port %s but got %s" % (port,configured_port)
    else:
        print "wsadmin says the configured port is %s" % configured_port

    if not hostAliasExists(virtualhostname, aliashostname, port):
        raise "ERROR: host alias does not exist after creating it"

def ensureHostAlias( virtualhostname, aliashostname, port ):
    """Add host alias only if not already there"""
    m = "ensureHostAlias:"
    sop(m,"Entry. virtualhostname=%s aliashostname=%s port=%s" % ( repr(virtualhostname), repr(aliashostname), repr(port) ))
    if hostAliasExists(virtualhostname, aliashostname, port):
        return # no work to do

    # Force port to be a string - could be string or int on input
    port = "%d" % int( port )

    host_id = getVirtualHostByName( virtualhostname )
    if host_id == None:
        host_id = createVirtualHost(virtualhostname)
    addHostAlias( virtualhostname, aliashostname, port )
    if not hostAliasExists(virtualhostname, aliashostname, port):
        raise "ERROR: host alias does not exist after creating it"

def deleteHostAlias( virtualhostname, aliashostname, port ):
    """Deletes host alias"""
    m = "deleteHostAlias:"
    #sop(m,"Entry. virtualhostname=%s aliashostname=%s port=%s" % ( repr(virtualhostname), repr(aliashostname), repr(port) ))
    # Prevent dumb errors.
    if virtualhostname == 'admin_host':
        raise "deleteHostAlias: ERROR: You may not delete admin_host aliases lest the dmgr become incommunicado."
    # Force port to be a string - could be string or int on input
    port = "%d" % int( port )
    host_id = getVirtualHostByName( virtualhostname )
    #sop(m,"host_id = %s" % ( host_id ))
    if host_id != None:
        aliases = AdminConfig.showAttribute( host_id, 'aliases' )
        #sop(m,"aliases=%s" % ( repr(aliases) ))
        if aliases != None:
            aliases = aliases[1:-1].split( ' ' )
            #sop(m,"after split, aliases=%s" % ( repr(aliases) ))
            for alias_id in aliases:
                #sop(m,"alias_id=%s" % ( repr(alias_id) ))
                if alias_id != None and alias_id != '':
                    # Alias is a HostAlias object
                    h = AdminConfig.showAttribute( alias_id, 'hostname' )
                    p = AdminConfig.showAttribute( alias_id, 'port' )
                    if ( aliashostname, port ) == ( h, p ):
                        sop(m,"Deleting alias. virtualhostname=%s aliashostname=%s port=%s alias_id=%s" % ( virtualhostname, h, p, repr(alias_id) ))
                        AdminConfig.remove( alias_id )
                        return
        else:
            sop(m,"No aliases found.")
    else:
        sop(m,"host_id not found.")
    sop(m,"Exit. Warning: Nothing deleted. virtualhostname=%s aliashostname=%s port=%s" % ( repr(virtualhostname), repr(aliashostname), repr(port) ))

def deleteAndEnsureHostAlias( virtualhostname, aliashostname, port ):
    """Convenience method calls delete and ensure."""
    deleteHostAlias( virtualhostname, aliashostname, port )
    ensureHostAlias( virtualhostname, aliashostname, port )

def deleteAllHostAliases( virtualhostname ):
    """Deletes all host aliases for the given virtual host name.  Don't try it with admin_host."""
    m = "deleteAllHostAliases:"
    #sop(m,"Entry. virtualhostname=%s" % ( repr(virtualhostname) ))
    # Prevent dumb errors.
    if virtualhostname == 'admin_host':
        raise "deleteAllHostAliases: ERROR: You may not delete admin_host aliases lest the dmgr become incommunicado."
    host_id = getVirtualHostByName( virtualhostname )
    #sop(m,"host_id=%s" % ( repr(host_id) ))
    if host_id != None:
        aliases = AdminConfig.showAttribute( host_id, 'aliases' )
        #sop(m,"aliases=%s" % ( repr(aliases) ))
        if aliases != None:
            aliases = aliases[1:-1].split( ' ' )
            for alias_id in aliases:
                sop(m,"Deleting alias. alias_id=%s" % ( repr(alias_id) ))
                AdminConfig.remove( alias_id )
        else:
            sop(m,"No aliases found. Nothing to delete.")
    else:
        sop(m,"host_id not found. Nothing to delete.")
    #sop(m,"Exit.")

def createChain(nodename,servername,chainname,portname,hostname,portnumber,templatename):
    """Create a new transport chain.  You can figure out most of the needed arguments
    by doing this in the admin console once.  Write down the template name so you can use it here."""
    m = "createChain:"
    sop(m,"Entry. nodename=%s servername=%s chainname=%s templatename=%s" % ( nodename, servername, chainname, templatename ))
    # We'll need the transport channel service for that server
    server = getServerByNodeAndName(nodename,servername)
    if server is None:
        raise m + " Error: Could not find server. nodename=%s servername=%s" % (nodename,servername)
    sop(m,"Found config id for server. server=%s" % ( server, ))
    transportchannelservice = getObjectsOfType('TransportChannelService', scope = server)[0] # There should be only one
    sop(m,"Found config id for transport channel service. transportchannelservice=%s" % ( transportchannelservice, ))
    # Does the end point exist already?
    endpoint = getEndPoint(nodename,servername,portname)
    if endpoint is None:
        endpoint = AdminTask.createTCPEndPoint(transportchannelservice,
                                               '[-name %s -host %s -port %d]' % (portname,hostname,portnumber))
    sop(m,"Attempting to create transport chain at endpoint. endpoint=%s" % ( endpoint, ))
    result = AdminTask.createChain(transportchannelservice,
                          '[-template %s -name %s -endPoint %s]' % (templatename,chainname,endpoint))
    sop(m,"Resulting config id for transport chain. result=%s" % ( result, ))
    sop(m,"Exit.")
    return result


    # Example from command assistance:
    # AdminTask.createTCPEndPoint('(cells/poir1Cell01/nodes/poir1Node01/servers/p1server|server.xml#TransportChannelService_1171391977380)', '[-name NEWSIPPORTNAME -host * -port 5099]')
    # AdminTask.createChain('(cells/poir1Cell01/nodes/poir1Node01/servers/p1server|server.xml#TransportChannelService_1171391977380)', '[-template SIPContainer(templates/chains|sipcontainer-chains.xml#Chain_1) -name NEWSIPCHAIN -endPoint (cells/poir1Cell01/nodes/poir1Node01|serverindex.xml#NamedEndPoint_1171398842876)]')

def nodeIsIHS( nodename ):
    """Returns true if the node is IHS."""
    # Note: This method queries whether variable WAS_INSTALL_ROOT is defined.
    # This is a weak technique for identifying an IHS node.
    # Hopefully a more robust mechanism can be found in the future.
    return None == getWasInstallRoot(nodename)


def nodeIsDmgr( nodename ):
    """Return true if the node is the deployment manager"""
    return nodeHasServerOfType( nodename, 'DEPLOYMENT_MANAGER' )

def nodeIsUnmanaged( nodename ):
    """Return true if the node is an unmanaged node."""
    return not nodeHasServerOfType( nodename, 'NODE_AGENT' )

def nodeHasServerOfType( nodename, servertype ):
    node_id = getNodeId(nodename)
    serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))
    for serverEntry in serverEntries:
        sType = AdminConfig.showAttribute( serverEntry, "serverType" )
        if sType == servertype:
            return 1
    return 0

def getDmgrNode():
    """Return config id of the Dmgr node"""
    node_ids = _splitlines(AdminConfig.list( 'Node' ))
    for node_id in node_ids:
        nodename = getNodeName(node_id)
        if nodeIsDmgr(nodename):
            return node_id
    return None

def getDmgrNodeName():
    """Return node name of the Dmgr node"""
    return getNodeName(getDmgrNode())


def listNodes():
    """Return list of node names, excluding the dmgr node.
       Beware, this list will include any existing IHS nodes."""
    m = "listNodes:"
    node_ids = _splitlines(AdminConfig.list( 'Node' ))
    result = []
    for node_id in node_ids:
        nodename = getNodeName(node_id)
        if not nodeIsDmgr(nodename):
            result.append(nodename)
    if 0 == len(result):
        sop(m,"Warning. No non-manager nodes are defined!!!")
    return result


def listAppServerNodes():
    """Returns a list of nodes excluding dmgr and IHS nodes"""
    m = "listAppServerNodes:"
    node_ids = _splitlines(AdminConfig.list( 'Node' ))
    result = []
    for node_id in node_ids:
        nodename = getNodeName(node_id)
        if not nodeIsDmgr(nodename) and not nodeIsIHS(nodename):
            result.append(nodename)
    if 0 == len(result):
        sop(m,"Warning. No non-manager/non-IHS nodes are defined!!!")
    return result


def syncall():
    """Sync config to all nodes - return 0 on success, non-zero on error"""
    m = "wsadminlib.syncall"

    if whatEnv() == 'base':
        sop(m,"WebSphere Base, not syncing")
        return 0

    sop(m, "Start")

    returncode = 0

    nodenames = listNodes()
    for nodename in nodenames:
        # Note: listNodes() doesn't include the dmgr node - if it did, we'd
        # have to skip it
        # We do, however, have to skip unmanaged nodes.  These will show up
        # when there is a web server defined on a remote machine.
        if not nodeIsDmgr( nodename ) and not nodeIsUnmanaged( nodename ):
            sop(m,"Sync config to node %s" % nodename)
            Sync1 = AdminControl.completeObjectName( "type=NodeSync,node=%s,*" % nodename )
            if Sync1:
                rc = AdminControl.invoke( Sync1, 'sync' )
                if rc != 'true':  # failed
                    sop(m,"Sync of node %s FAILED" % nodename)
                    returncode = 1
            else:
                sop(m,"WARNING: was unable to get sync object for node %s - is node agent running?" % nodename)
                returncode = 2
    if returncode != 0:
        sop(m,"Syncall FAILED")
    sop(m,"Done")
    return returncode

"""
NOTES on variables we can use in log names.
These aren't available everywhere.

Distributed:
  ${LOG_ROOT} - all processes
  ${SERVER} - all processes
  ${SERVER_LOG_ROOT} - servers, but not node agent or dmgr

z/OS:
  ${SERVER} - node agent
  ${LOG_ROOT} - app server, dmgr,   nodeagent
  ${USER_INSTALL_ROOT} - all processes
  ${SERVER_LOG_ROOT} - app server, dmgr   ? nodeagent

"""

def enableServantRegion(nodename, servername, onoff):
    """Turn servant region on or off for a server on z/OS.
    No-op if not on z/OS.
    onoff should be a boolean (e.g. True or False, 1 or 0)."""
    m = "enableServantRegion"
    sop(m, "nodename=%(nodename)s servername=%(servername)s onoff=%(onoff)d" % locals())
    if getNodePlatformOS(nodename) != "os390":
        sop(m,"Not on z/OS, just returning")
        return
    # Get the serverinstance object for the server
    server_id = getServerByNodeAndName(nodename, servername)
    server_instance = getObjectsOfType('ServerInstance', server_id)[0]
    if onoff:
        setObjectAttributes(server_instance, maximumNumberOfInstances = "1")
    else:
        setObjectAttributes(server_instance, maximumNumberOfInstances = "0")
    sop(m,"Done")

def configureServerInstance(nodename, servername, enable, min, max):
    """Turn ServerInstance on or off for a server on z/OS and set minimumNumOfInstances and maximumNumberOfInstances
    No-op if not on z/OS.
    enable should be a boolean (e.g. True or False)."""
    attrlist = [ ['enableMultipleServerInstances', enable] ,[ 'minimumNumOfInstances',min] , ['maximumNumberOfInstances',max ] ]
    if getNodePlatformOS(nodename) != "os390":
        sop(m,"Not on z/OS, just returning")
        return
    # Get the serverinstance object for the server
    server_id = getServerByNodeAndName(nodename, servername)
    server_instance = getObjectsOfType('ServerInstance', server_id)[0]
    AdminConfig.modify(server_instance, attrlist)

def _setServerStream(nodename, servername, streamName,
                     filename, rolloverSize, maxNumberOfBackupFiles):
    """Intended for private use within wsadminlib only.
    Change characteristics of a server output stream.
    Used by e.g. setServerSysout"""

    m = '_setServerStream:'
    sop(m,'ENTRY: streamName=%s' % streamName)

    serverId = getServerId(nodename,servername)
    streamId = AdminConfig.showAttribute(serverId, streamName)

    if streamId == None or streamId == "":
        # Doesn't exist yet, create one with the default attributes
        # and point streamId at it
        default_attrs = [
            ['baseHour', 24],
            ['fileName', filename],
            ['formatWrites', 'true'],
            ['maxNumberOfBackupFiles', 1],
            ['messageFormatKind', 'BASIC'],
            ['rolloverPeriod', 24],
            ['rolloverSize', 1],
            ['rolloverType', 'SIZE'],
            ['suppressStackTrace', 'false'],
            ['suppressWrites', 'false'],
            ]
        # Create a streamredirect object
        # Note that a 4th arg is required here to distinguish which
        # StreamRedirect attribute of the server is being referred to.
        sop(m,"serverId=%s" % serverId)
        sop(m,"default_attrs=%s" % repr(default_attrs))
        sop(m,"streamName=%s" % streamName)
        streamId = AdminConfig.create('StreamRedirect', serverId, default_attrs,streamName)

    # modify attributes of the redirect stream
    AdminConfig.modify(streamId, [['fileName', filename],
                                  ['rolloverSize', rolloverSize],
                                  ['maxNumberOfBackupFiles', maxNumberOfBackupFiles]])

def setServerSysout(nodename, servername,
                    # ${SERVER_LOG_ROOT} is not defined on dmgr or node agent
                    # Hopefully ${LOG_ROOT} and ${SERVER} are defined everywhere
                    filename = '$' + '{LOG_ROOT}/' + '$' + '{SERVER}/SystemOut.log',
                    rolloverSize = 50,
                    maxNumberOfBackupFiles = 2):
    """Set the server's system out to go to a specified log, rollover, etc.
    Can be used on z/OS to send system out to a file instead of the job log in spool.
    Might also be useful to keep the system out log from getting too big."""

    """Default value of outputStreamRedirect on windows:

    """
    streamName = 'outputStreamRedirect'
    _setServerStream(nodename = nodename,
                     servername = servername,
                     streamName = streamName,
                     filename = filename,
                     rolloverSize = rolloverSize,
                     maxNumberOfBackupFiles = maxNumberOfBackupFiles)

def setServerSyserr(nodename, servername,
                    # ${SERVER_LOG_ROOT} is not defined on dmgr or node agent
                    # Hopefully ${LOG_ROOT} and ${SERVER} are defined everywhere
                    filename = '$' + '{LOG_ROOT}/' + '$' + '{SERVER}/SystemErr.log',
                    rolloverSize = 50,
                    maxNumberOfBackupFiles = 2):
    """Set the server's system out to go to a specified log, rollover, etc.
    Can be used on z/OS to send system out to a file instead of the job log in spool.
    Might also be useful to keep the system out log from getting too big."""

    """Default value of outputStreamRedirect on windows:

    """
    streamName = 'errorStreamRedirect'
    _setServerStream(nodename = nodename,
                     servername = servername,
                     streamName = streamName,
                     filename = filename,
                     rolloverSize = rolloverSize,
                     maxNumberOfBackupFiles = maxNumberOfBackupFiles)


def setServerTrace( nodename,servername, traceSpec="*=info", outputType="SPECIFIED_FILE",
                   maxBackupFiles=50, rolloverSize=50,
                    traceFilename='$'+'{LOG_ROOT}/' + '$' + '{SERVER}/trace.log' ):
    """Set the trace spec for a server.  Could be an app server or proxy server.
    By default also makes sure the trace goes to the usual file (even on z/OS),
    and sets up rollover.  Override the default values to change that."""
    m = "setServerTrace:"
    #sop(m,"Entry. nodename=%s servername=%s traceSpec=%s outputType=%s maxBackupFiles=%s rolloverSize=%s traceFilename=%s" % (nodename, servername, traceSpec, outputType, maxBackupFiles, rolloverSize, traceFilename))

    server_id = getServerId(nodename,servername)
    #sop(m,"server_id=%s" % ( repr( server_id ) ))
    if not server_id:
        raise "COULD NOT LOCATE SERVER: node=%s, name=%s" % (nodename,servername)
    #sop(m,"Getting trace service")
    tc = AdminConfig.list( 'TraceService', server_id )
    #sop(m,"Trace service is %s" % ( repr( tc ) ))
    sop(m,"Setting tracing on server %s/%s to %s" % (nodename,servername,traceSpec))

    AdminConfig.modify( tc, [['startupTraceSpecification', traceSpec]] )
    AdminConfig.modify( tc, [['traceOutputType', outputType]] )
    AdminConfig.modify( tc, [['traceLog', [['fileName', traceFilename],
                                           ['maxNumberOfBackupFiles', '%d' % maxBackupFiles],
                                           ['rolloverSize', '%d'%rolloverSize]]]] )


def setServerSIPAttributes(nodename, servername, attrs):
    """Set the SIP container attributes on the given server.
    attrs should be a dictionary of names and values.
    The attributes (and default values) are:
    [maxAppSessions 120000]
    [maxDispatchQueueSize 3200]
    [maxMessageRate 5000]
    [maxResponseTime 0]
    [statAveragePeriod 1000]
    [statUpdateRange 10000]

    You can also set 'threadPool' to the name of a pool and the Right Thing will happen.
    """
    m = "setServerSIPAttributes:"
    # Get the SIPContainer
    server_id = getServerId(nodename,servername)
    container_id = AdminConfig.list('SIPContainer', server_id)
    # Modify the given settings
    for k in attrs.keys():
        v = attrs[k]
        sop(m,"Setting SIP container attribute %s=%s" % (k,v))
        if k == 'threadPool':
            threadPoolId = getObjectByNodeServerAndName(nodename=nodename,
                                                        servername = servername,
                                                        typename = 'ThreadPool',
                                                        objectname = v)
            AdminConfig.modify(container_id, [[k,threadPoolId]])
        else:
            AdminConfig.modify(container_id, [[k,v]])

def setChannelCustomProperty(nodename, servername, name, value, channelType, endPointName = None, channelName = None):
    """Set a custom property on a channel.  Identify the channel by:
    channelType: e.g. "TCPInboundChannel", "SSLInboundChannel", "UDPInboundChannel", etc.
    endPointName: e.g. "SIP_DEFAULT_HOST", "SIP_DEFAULTHOST_SECURE", etc.
    channelName: e.g. "HTTP_1", "HTTP_2"
    One of endPointName or channelName is required.

    name,value are the name and value of the custom property"""
    m = "setChannelCustomProperty:"

    if not endPointName and not channelName:
        raise Exception("ERROR: must specify endPointName or channelName")

    sop(m,"Entry. Setting channel custom property %s=%s on %s/%s channelType=%s endPointName=%s channelName=%s" % (name,value,nodename,servername,channelType, endPointName, channelName))

    # Find the channel object
    server_id = getServerId(nodename,servername)
    channels = _splitlines(AdminConfig.list(channelType, server_id))
    foundChannel = None
    for channel in channels:
        #sop(m,"For loop: channel=%s actualEndPointName=%s actualChannelName=%s" % ( channel, AdminConfig.showAttribute(channel, "endPointName"), AdminConfig.showAttribute(channel, "name") ))
        if endPointName:
            if endPointName != AdminConfig.showAttribute(channel, "endPointName"):
                #sop(m,"No match with endPointname. desired=%s actual=%s" % ( endPointName, AdminConfig.showAttribute(channel, "endPointName") ))
                continue
        if channelName:
            if channelName != AdminConfig.showAttribute(channel, "name"):
                #sop(m,"No match with channelname. desired=%s actual=%s" % ( channelName, AdminConfig.showAttribute(channel, "name") ))
                continue
        foundChannel = channel
        break
    if not foundChannel:
        raise Exception("ERROR: channel not found")
    #sop(m,"Found match. Setting property.")
    # Is property present already?
    properties = _splitlines(AdminConfig.list('Property', foundChannel))
    for p in properties:
        pname = AdminConfig.showAttribute(p, "name")
        if pname == name:
            # Already exists, just change value
            AdminConfig.modify(p, [['value', value]])
            return
    # Does not exist, create and set
    p = AdminConfig.create('Property', foundChannel, [['name', name],['value', value]])

def getSipContainerCustomProperty(nodename, servername, propname):
    """Returns the Value of the specified custom property of the SIP Container, or None if there is none by that name."""
    m = "getSipContainerCustomProperty:"
    sop(m,"Entry. nodename=" + nodename + " servername=" + servername + " propname=" + propname)
    server_id = getServerId(nodename,servername)
    container_id = AdminConfig.list('SIPContainer', server_id)
    propvalue = getObjectCustomProperty(container_id, propname)
    sop(m,"Exit. Returning propvalue=" + propvalue)
    return propvalue

def setSipContainerCustomProperty(nodename, servername, propname, propvalue):
    sop("setSipContainerCustomProperty", "Setting custom sip property %s=%s" % (propname,propvalue))
    server_id = getServerId(nodename,servername)
    container_id = AdminConfig.list('SIPContainer', server_id)
    setCustomPropertyOnObject(container_id, propname, propvalue)

def setSipProxyCustomProperty(nodename, servername, propname, propvalue):
    """Set a custom property on the SIP proxy with the given nodename and servername (name of the proxy)"""
    m = "setSipProxyCustomProperty:"
    # We have to find the ProxySettings object for this proxy
    sid = getSIPProxySettings(nodename,servername)
    sop(m,"ProxySettingsObject id = %s" % sid)
    # It has a properties attribute, so now we can use setCustomPropertyOnObject
    # on it
    setCustomPropertyOnObject(sid, propname, propvalue)

def modifySessionPersistenceMode(proxyname,persistencemode):
    """modify 'session persistence mode' of proxy server to 'DATA_REPLICATION'"""
    proxy=AdminConfig.getid('/Server:%s/' % proxyname)
    components1=AdminConfig.showAttribute(proxy,'components')
    comps1=components1[1:-1].split(" ")
    for comp1 in comps1:
        if comp1.find('ApplicationServer')!=-1:
            components2=AdminConfig.showAttribute(comp1,'components')
            comps2=components2[1:-1].split(" ")
            for comp2 in comps2:
                if comp2.find('WebContainer')!=-1:
                    services=AdminConfig.showAttribute(comp2,'services')
                    servs=services[1:-1].split(" ")
                    for serv in servs:
                        AdminConfig.modify(serv,[['sessionPersistenceMode',persistencemode]])

def getObjectCustomProperty(object_id, propname):
    """Return the VALUE of the specified custom property of the server, or None if there is none by that name.
    This is to be used only on objects that
    store their custom properties as a list of Property objects in their 'properties'
    attribute.  This includes, for example, application servers and SIP containers.
    There are other objects that store J2EEResourceProperty objects instead;
    don't use this for those.

    Intended for private use within wsadminlib only.
    Write wrappers to be called externally.
    """

    x = AdminConfig.showAttribute(object_id,'properties')
    if len(x[1:-1]) == 0:
        return None  # no properties set yet
    #print "value of properties attribute=%s" % x
    # This can be of the format "[foo(value) bar(baz)]" where the values are "foo(value)",
    # "bar(baz)".  It also seems to be able to be just "foo(value)"
    if x.startswith("["):
        propsidlist = _splitlist(x)
    else:
        propsidlist = _splitlist('[' + x + ']')
    #print "List of properties = %s" % repr(propsidlist)
    for id in propsidlist:
        #print "id=%s" % id
        name = AdminConfig.showAttribute(id, 'name')
        if name == propname:
            return AdminConfig.showAttribute(id, 'value')
    return None

def setCustomPropertyOnObject(object_id, propname, propvalue):
    """Set a custom property on an object - this is to be used only on objects that
    store their custom properties as a list of Property objects in their 'properties'
    attribute.  This includes, for example, application servers and SIP containers.
    There are other objects that store J2EEResourceProperty objects instead;
    don't use this for those.

    Intended for private use within wsadminlib only.
    Write wrappers to be called externally.
    """

    # Does it exist?
    value = getObjectCustomProperty(object_id,propname)
    if value != None:
        # Exists - need change?
        if value == propvalue:
            return  # already set the way we want
        # Need to change
        propsidlist = AdminConfig.showAttribute(object_id,'properties')[1:-1].split(' ')
        for id in propsidlist:
            name = AdminConfig.showAttribute(id, 'name')
            if name == propname:
                # NOTE:  this is currently failing when propvalue
                # has a space in it, so workaround by deleting and recreating the property, which
                # seems to work
                #AdminConfig.modify(id, ['value', propvalue])
                AdminConfig.remove(id)
                AdminConfig.modify(object_id, [['properties', [[['name', propname], ['value', propvalue]]]]])
                return
        raise Exception("Could not find property %s in server %s %s even though it seemed to be there earlier" % (propname,nodename,servername))
    else:
        # Need to create property
        AdminConfig.modify(object_id, [['properties', [[['name', propname], ['value', propvalue]]]]])

def setServerPMI(nodename, servername, enable, initialSpecLevel, syncUpdate):
    """Set the PMI settings for a given server.
    enabled should be 'true' or 'false'.
    syncUpdate should be 'true' or 'false'."""
    server_id = getServerId(nodename,servername)
    if not server_id:
        raise "COULD NOT LOCATE SERVER: node=%s, name=%s" % (nodename,servername)
    pmi = AdminConfig.list('PMIService', server_id)
    AdminConfig.modify(pmi, [['synchronizedUpdate', syncUpdate],['enable', enable], ['statisticSet', initialSpecLevel]])

def setServerPMIforDynacache(nodename, servername, enable, initialSpecLevel):
    """Set the PMI settings for a given server.
    enabled should be 'true' or 'false'."""
    server_id = getServerId(nodename,servername)
    if not server_id:
        raise "COULD NOT LOCATE SERVER: node=%s, name=%s" %(nodename,servername)
    pmi = AdminConfig.list('PMIService', server_id)
    #AdminConfig.modify(pmi, [['enable', enable], ['initialSpecLevel',initialSpecLevel]])
    AdminConfig.modify(pmi, [['enable','true'],['initialSpecLevel','cacheModule=H'],['statisticSet','custom']])

    pmi = AdminConfig.list('PMIModule', server_id)

    pmiList = AdminConfig.showAttribute(pmi,'pmimodules')
    pmiList = pmiList.replace("[","")
    pmiList = pmiList.replace("]","")
    pmiList = pmiList.split(" ")

    for n in pmiList:
        cur = AdminConfig.showAttribute(n,'moduleName')
        if cur == "cacheModule":
            wam = n
    AdminConfig.modify(wam, [['enable','2,1,29,30,32,23,31,21,22,26,2,34,24,1,28,36,35,27,25,12,10,11,9,6,17,5,4,18,8,16,14,15,13,7']])
    pmiList1 = AdminConfig.showAttribute(wam,'pmimodules')
    pmiList1 = pmiList1.replace("[","")
    pmiList1 = pmiList1.replace("]","")
    pmiList1 = pmiList1.split(" ")

    for n1 in pmiList1:
        cur1 = AdminConfig.showAttribute(n1,'moduleName')
    AdminConfig.modify(n1, [['enable','2,1,29,30,32,23,31,21,22,26,2,34,24,1,28,36,35,27,25,12,10,11,9,6,17,5,4,18,8,16,14,15,13,7']])

    pmiList2 = AdminConfig.showAttribute(n1,'pmimodules')
    pmiList2 = pmiList2.replace("[","")
    pmiList2 = pmiList2.replace("]","")
    pmiList2 = pmiList2.split(" ")

    for n2 in pmiList2:
        cur2 = AdminConfig.showAttribute(n2,'moduleName')
        AdminConfig.modify(n2, [['enable','2,1,29,30,32,23,31,21,22,26,2,34,24,1,28,36,35,27,25,12,10,11,9,6,17,5,4,18,8,16,14,15,13,7']])

def setCustomProperty(object_id, name, value):
    """FIXME: not done yet

    Set a custom property on the object with the given ID.
    Violates our usual convention of not making our callers provide object IDs, just because
    otherwise we'd need too many variations on this method.
    If the custom property already exists, its value is changed.
    Configuring custom properties for resource environment entries using scripting

    Configuring new custom properties using scripting
    http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/topic/com.ibm.websphere.base.doc/info/aes/ae/txml_mailcustom.html

    http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/topic/com.ibm.websphere.nd.doc/info/ae/ae/txml_envcustom.html
    """

    propSet = AdminConfig.showAttribute(object_id, 'propertySet')
    # This should work to create (though untested) - but what if it exists already?
    # We need to detect that and do the right thing
    AdminConfig.create('J2EEResourceProperty', propSet, [[name,value]])

def addOrSetCustomPropertyToDictionary(newName, newValue, dict):
    """Adds or sets a custom_property object into the supplied dictionary.

    Note: This method only changes the supplied dictionary. It does not communicate with a dmgr.

    The key of the customer properties in the dictionary must be 'custom_properties'.
    Each custom property must contain a 'name' and 'value'.  For example:
       'custom_properties': [ { 'name': 'http.cache.synchronousInvalidate',  'value': 'TRUE' },
                              { 'name': 'http.cache.useSystemTime'        ,  'value': 'TRUE' },
                              { 'name': 'http.compliance.via'             ,  'value': 'TRUE' }, ]

    If no custom properties exist in the dictionary, this method adds a list of custom_properties.
    If the custom property name does not exist in the list, this method adds the name and value.
    If the custom property name already exists, this method overrides the value.
    """
    m = "addOrSetCustomPropertyToDictionary: "
    sop(m,"Entry. newName=%s newValue=%s" % ( newName, newValue ))
    custom_prop_object = { 'name': newName,  'value': newValue }

    if 'custom_properties' in dict.keys():
        custom_properties = dict['custom_properties']
        sop(m,"custom_properties are already instantiated.")
        found_name = 0
        for custom_property in custom_properties:
            name = custom_property['name']
            value = custom_property['value']
            sop(m,"existing: name=%s value=%s" % (name,value))
            if name == newName:
                sop(m,"Found existing custom property matching specified name.  Replacing value.")
                custom_property['value'] = newValue
                found_name = 1
                break
        if found_name == 0:
            sop(m,"Did not find existing custom property matching specified name.  Adding prop.")
            custom_properties.append( custom_prop_object )

    else:
        sop(m,"custom_properties are not already defined. Adding.")
        dict['custom_properties'] = [ custom_prop_object, ]

    # Racap (debug)
    # custom_properties = dict['custom_properties']
    # for custom_property in custom_properties:
    #     sop(m,"Recap: custom_property=%s" % (repr(custom_property)))

    sop(m,"Exit.")

def addAttributeToChannelList(channellist, channelType, channelName, attributename, attributevalue):
    """Adds channel attributes to the supplied list.

      Note: This method only changes the supplied list. It does not communicate with a dmgr.

      For example, it creates the following:
      'channel_attributes': [{ 'channelType': 'HTTPInboundChannel',
                               'channelName': 'HTTP_2',
                               'attributename': 'persistentTimeout',
                               'attributevalue': '15' }, ]
      """
    m = "addAttributeToChannelList: "
    # sop(m,"Entry. channelType=%s channelName=%s attributename=%s attributevalue=%s" % ( channelType, channelName, attributename, attributevalue, ))

    # Create a new dictionary object to specify the attribute.
    new_attribute_dict = { 'channelType': channelType,
                           'channelName': channelName,
                           'attributename': attributename,
                           'attributevalue': attributevalue, }
    # sop(m,"new_attribute_dict=%s" % ( new_attribute_dict ))

    # Find and remove any existing instances of the specified attribute from the list.
    for old_attribute_dict in channellist:
        # sop(m,"old_attribute_dict=%s" % ( old_attribute_dict ))
        if 'channelType' in old_attribute_dict.keys() and 'channelName' in old_attribute_dict.keys()and 'attributename' in old_attribute_dict.keys():
            # sop(m,"old attribute contains key 'channelType', 'channelName', and 'attributename'")
            if channelType == old_attribute_dict['channelType'] and channelName == old_attribute_dict['channelName'] and attributename == old_attribute_dict['attributename']:
                sop(m,"Found old attribute matchine specified new attribute. Removing old attribute from list. channelType=%s channelName=%s attributename=%s attributevalue=%s" % ( channelType, channelName, attributename, attributevalue, ))
                channellist.remove(old_attribute_dict)
            # else:
            #     sop(m,"Leaving old attribute intact in list.")

    # Add the new attribute to the list.
    sop(m,"Adding the new attribute to the list. channelType=%s channelName=%s attributename=%s attributevalue=%s" % ( channelType, channelName, attributename, attributevalue, ))
    channellist.append(new_attribute_dict)
    # sop(m,"Exit. channellist=%s" % ( repr(channellist) ))


def getShortHostnameFromNodename(nodename):
    """Extracts the short hostname from a node name.

    Relies upon the convention that node names usually start
    with the short hostname followed by the string 'Node',
    for example 'ding4Node01'.

    Returns the short hostname if the string 'Node' is present
    and if the short hostname is more than 3 characters in length.
    Otherwise returns the full node name.
    """
    #print 'getShortHostnameFromNodename: Entry. nodename=' + nodename
    shn = nodename
    ix = nodename.find('Node')
    #print 'getShortHostnameFromNodename: ix=%d' % (ix)
    if ix != -1 :
        #print 'getShortHostnameFromNodename: nodename contains Node'
        if ix > 2 :
            shn = nodename[0:ix]
    #print 'getShortHostnameFromNodename: Exit. shn=' + shn
    return shn

def getNameFromId(obj_id):
    """Returns the name from a wsadmin object id string.

    For example, returns PAP_1 from the following id:
    PAP_1(cells/ding6Cell01|coregroupbridge.xml#PeerAccessPoint_1157676511879)

    Returns the original id string if a left parenthesis is not found.
    """
    # print "getNameFromId: Entry. obj_id=" + obj_id
    name = obj_id
    ix = obj_id.find('(')
    # print "getNameFromId: ix=%d" % (ix)
    if ix != -1 :
        name = obj_id[0:ix]

    # print "getNameFromId: Exit. name=" + name
    return name

def getNumberFromId(obj_id):
    """Returns the long decimal number (as a string) from the end of a wsadmin object id string.

    For example, returns 1157676511879 from the following id:
    PAP_1(cells/ding6Cell01|coregroupbridge.xml#PeerAccessPoint_1157676511879)

    Returns the original id string if the ID string can not be parsed.
    """
    longnum = obj_id
    if longnum.endswith(')'):
        # Strip off the trailing parenthesis
        len_orig = len(longnum)
        longnum = longnum[:len_orig-1]
        # Find the index of the last underscore.
        ix = longnum.rfind('_')
        if -1 != ix:
            # Extract the number.
            longnum = longnum[ix+1:]
    return longnum

def propsToLists(propString):
    """Helper method converts a flat string which looks like a list of properties into a real list of lists.

    Expects input of the form   [ [key0 value0] [key1 value1] ]
    Returns output of the form  [['key0', 'value0'], ['key1', 'value1']]
    Returns the original string if it does not look like a list."""
    m = "propsToLists: "
    sop(m,"Entry. propString=%s" % ( propString, ))

    # Check for leading and trailing square brackets.
    if not (propString.startswith( '[ [' ) and propString.endswith( '] ]' )):
        raise "%s ERROR: propString does not start and end with two square brackets. propString=%s" % ( m, propString, )

    # Strip off the leading and trailing square brackets.
    propString = propString[3:(len(propString) - 3)]
    sop(m,"Stripped leading/trailing square brackets. propString=%s" % ( propString, ))

    # Convert the single long string to a list of strings.
    stringList = propString.split('] [')
    sop(m,"Split string into list. stringList=%s" % ( stringList, ))

    # Convert each enclosed string into a list of strings.
    listList = []
    for prop in stringList:
        sop(m,"prop=%s" % ( prop, ))
        keyValueList = prop.split(' ')
        sop(m,"keyValueList=%s" % ( keyValueList, ))
        listList.append(keyValueList)

    sop(m,"Exit. returning list %s" % ( repr(listList), ))
    return listList

def propsToDictionary(propString):
    """Helper method converts a flat string which looks like a list of properties
    into a dictionary of keys and values.

    Expects input of the form   [ [key0 value0] [key1 value1] ]
    Returns output of the form  { 'key0': 'value0', 'key1': 'value1', }
    Raises an exception if the original string does not look like a list of properties."""
    m = "propsToDictionary: "
    # print "%s Entry. propString=%s" % ( m, propString, )

    # Check for leading and trailing square brackets.
    if not (propString.startswith( '[ [' ) and propString.endswith( '] ]' )):
        raise "%s ERROR: propString does not start and end with two square brackets. propString=%s" % ( m, propString, )

    # Strip off the leading and trailing square brackets.
    propString = propString[3:(len(propString) - 3)]
    # print "%s Stripped leading/trailing square brackets. propString=%s" % ( m, propString, )

    # Convert the single long string to a list of strings.
    stringList = propString.split('] [')
    # print "%s Split string into list. stringList=%s" % ( m, stringList, )

    # Transfer each enclosed key and value string into a dictionary
    dict = {}
    for prop in stringList:
        # print "%s prop=%s" % ( m, prop, )
        keyValueList = prop.split(' ')
        # print "%s keyValueList=%s" % ( m, keyValueList, )
        dict[keyValueList[0]] = keyValueList[1]

    # print "%s Exit. returning dictionary %s" % ( m, repr(dict), )
    return dict

def dictToList(dict):
    """Helper method converts a dictionary into a list of lists.

    Expects input of the form  { 'key0': 'value0', 'key1': 'value1', }
    Returns output of the form   [ [key0, value0], [key1, value1] ]"""
    m = "dictToList: "
    # sop(m,"Entry. dict=%s" % ( dict ))

    # Handle all key:value pairs in the dictionary.
    listList = []
    for k,v in dict.items():
        # sop(m,"k=%s v=%s" % ( k, v, ))
        # sop(m,"keyValueList=%s" % ( keyValueList ))
        listList.append([k,v])

    # sop(m,"Exit. Returning list %s" % ( listList ))
    return listList

def stringListToList(stringList):
    """Nuisance helper method converts a string which looks like a list into a list of strings.

    Expects string input of the form [(cells/ding/...0669) (cells/ding/...6350) (cells/ding/...6291)]
    Returns string output of the form  ['(cells/ding/...0669)', '(cells/ding/...6350)', '(cells/ding/...6291)']
    Note: string.split does not work because of the square brackets."""
    m = "stringListToList:"
    sop(m,"Entry. stringList=%s" % ( stringList ))

    # Dummy check.
    if not (stringList.startswith( '[' ) and stringList.endswith( ']' )):
        raise "%s ERROR: stringList does not start and end with square brackets. stringList=%s" % ( m, stringList, )

    # Strip off the leading and trailing square brackets.
    stringList = stringList[1:(len(stringList) - 1)]

    # Get rid of the leading and trailing square brackets.
    listList = stringList.split()

    sop(m,"Exit. listList=%s" % (listList))
    return listList

def stringListListToDict(stringListList):
    """Yet another nuisance helper method to convert jacl-looking strings to jython objects.
    Input: String of the form:  [ [NOT_ATTEMPTED 0] [state ACTIVE] [FAILED 1] [DISTRIBUTED 0] ]
    Output: Dictionary of the form:  {  "NOT_ATTEMPTED": "0",
                                        "state": "ACTIVE",
                                        "FAILED":, "1",
                                        "DISTRIBUTED": "0",  }"""
    m = "stringListListToDict:"
    # sop(m,"Entry. stringListList=%s" % ( stringListList ))

    # Dummy check.
    if not (stringListList.startswith( '[' ) and stringListList.endswith( ']' )):
        raise "%s ERROR: stringListList does not start and end with square brackets. stringListList=%s" % ( m, stringListList )

    # Strip off the leading and trailing square brackets. And strip off whitespace
    stringListList = stringListList[1:(len(stringListList) - 1)].strip()
    # sop(m,"stringListList=%s" % ( stringListList ))

    dict = {}
    indexOpen = stringListList.find('[')
    indexClose = stringListList.find(']')
    # sop(m,"indexOpen=%d indexClose=%d" % ( indexOpen, indexClose ))
    while -1 != indexOpen and -1 != indexClose and indexOpen < indexClose:
        firstListString = stringListList[indexOpen:(1+indexClose)].strip()
        stringListList = stringListList[(1+indexClose):].strip()
        # sop(m,"firstListString=>>>%s<<< stringListList=>>>%s<<<" % ( firstListString, stringListList ))

        # Check first list.
        if not (firstListString.startswith( '[' ) and firstListString.endswith( ']' )):
            raise "%s ERROR: firstListString does not start and end with square brackets. firstListString=%s" % ( m, firstListString )
        # Strip off the leading and trailing square brackets. Do not strip off whitespace; otherwise, a possible null value is lost. e.g. [serverId ]
        firstListString = firstListString[1:(len(firstListString) - 1)]
        # sop(m,"firstListString=>>>%s<<< " % ( firstListString ))
        # Get the key and value.
        splitList = firstListString.split(' ',1)
        if 2 != len(splitList):
            raise "%s ERROR: unexpected contents in first list. Must be of form key space value. firstListString=%s" % ( m, firstListString )
        key = splitList[0].strip()
        value = splitList[1].strip()
        # sop(m,"key=%s value=%s" % ( key, value ))
        # Add to dictionary.
        dict[key] = value
        # sop(m,"dict=%s" % ( repr(dict) ))

        # Next element
        indexOpen = stringListList.find('[')
        indexClose = stringListList.find(']')
        # sop(m,"indexOpen=%d indexClose=%d" % ( indexOpen, indexClose ))

    # sop(m,"Exit. dict=%s" % ( repr(dict) ))
    return dict

def listOfStringsToJaclString(listOfStrings):
    """Nuisance method works around yet-another behavior in wsadmin/jython.

    Input: A jython list object containing jython lists containing strings, for example:
        [['name',  'pa1'],['fromPattern',"(.*)\.com(.*)"],['toPattern',  "$1.edu$2"]]
    Output: A jython list object containing one big string which looks like jacl, for example:
        ['[name "pa1"][fromPattern "(.*)\.com(.*)"][toPattern "$1.edu$2"]']
    Raises an exception if the input lists do not contain pairs of strings."""
    m = "listOfStringsToJaclString:"
    sop(m,"Entry. listOfStrings=%s" % (listOfStrings))

    jaclString = ""
    for innerList in listOfStrings:
        sop(m,"innerList=%s" % ( innerList ))
        if 2 != len(innerList):
            raise m + "ERROR. Inner list does not contain required 2 elements. innerList=%s" % ( innerList )
        key = innerList[0]
        value = innerList[1]
        sop(m,"key=%s value=%s" % ( key, value ))
        jaclString = '%s[%s "%s"]' % ( jaclString, key, value )
        sop(m,"jaclString=%s" % ( jaclString ))

    # jaclStringList = [jaclString]

    sop(m,"Exit. Returning list containing jaclString=%s" % ( jaclString ))
    return [jaclString]


def getChannelByName(nodename, servername, channeltype, channelname):
    """ Helper method returns a ChannelID or None.

    channeltype is of the form: 'HTTPInboundChannel'
    channelname is of the form: 'HTTP_1', 'HTTP_2', etc."""
    m = "getChannelByName:"
    #sop(m,"Entry: nodename=%s servername=%s channeltype=%s channelname=%s" % ( nodename, servername, channeltype, channelname, ))

    server = getServerByNodeAndName(nodename,servername)
    #sop(m,"server=%s" % ( server, ))

    tcs = _splitlines(AdminConfig.list('TransportChannelService', server))[0]
    #sop(m,"tcs=%s" % ( tcs, ))

    channel_list = _splitlines(AdminConfig.list(channeltype, tcs))
    channelFound = False
    for channel in channel_list:
        channel_name = getNameFromId(channel)
        sop(m,"channel_name=%s" % ( channel_name ))
        if channelname == channel_name:
            sop(m,"Exit. Success. Found desired channel. nodename=%s servername=%s channeltype=%s channelname=%s" % ( nodename, servername, channeltype, channelname, ))
            return channel
    sop(m,"Exit. Error. Channel not found. Returning None. nodename=%s servername=%s channeltype=%s channelname=%s" % ( nodename, servername, channeltype, channelname, ))
    return None

def setChannelAttribute(nodename, servername, channeltype, channelname, attributename, attributevalue, ):
    """ Sets an attribute of a channel object.

    channelname is of the form: 'HTTP_1', 'HTTP_2', etc."""
    m = "setChannelAttribute:"
    channel = getChannelByName(nodename, servername, channeltype, channelname)
    if None == channel:
        raise m + " Error. Channel not found. nodename=%s servername=%s channeltype=%s channelname=%s" % ( nodename, servername, channeltype, channelname, )
    try:
        AdminConfig.modify(channel, [[attributename, attributevalue]])
        print m + " Exit. Success. Set channel attribute. nodename=%s servername=%s channeltype=%s channelname=%s attributename=%s attributevalue=%s" % ( nodename, servername, channeltype, channelname, attributename, repr(attributevalue), )
        return 0
    except:
        raise m + " Error. Could not set channel attribute. nodename=%s servername=%s channeltype=%s channelname=%s attributename=%s attributevalue=%s" % ( nodename, servername, channeltype, channelname, attributename, repr(attributevalue), )

############################################################
# Methods related to Application Server Services

def configureAppProfilingService (nodename, servername, enable, compatmode):
    """ This function configures the Application Profiling Service for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose Application Profiling Service is to be configured.
        enable - specifies whether the Application Profiling Service is to be enabled or disabled.
                 Valid values are 'true' and 'false'.
        compatmode - specifies whether the 5.x compatibility mode is to be enabled or disabled.
                     Valid values are 'true' and 'false'.

    """

    m = "configureAppProfilingService:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "ApplicationProfileService"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            APServiceID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; APServiceID = %s" % APServiceID)

            attrs = []
            attrs.append( [ 'enable', enable ] )
            attrs.append( [ 'compatibility', compatmode ] )
            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (APServiceID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef


def configureTransactionService (nodename, servername, asyncResponseTimeout, clientInactivityTimeout, maximumTransactionTimeout, totalTranLifetimeTimeout, optionalParmsList=[]):
    """ This function configures the Transaction Service for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose Transaction Service is to be configured.
        asyncResponseTimeout - Specifies the amount of time, in seconds, that the server waits for an inbound Web Services
                               Atomic Transaction (WS-AT) protocol response before resending the previous WS-AT protocol message.
        clientInactivityTimeout - Specifies the maximum duration, in seconds, between transactional requests from a remote client.
        maximumTransactionTimeout - Specifies, in seconds, the upper limit of the transaction timeout for transactions that
                                    run in this server. This value should be greater than or equal to the value specified for the
                                    total transaction timeout.
        totalTranLifetimeTimeout - The default maximum time, in seconds, allowed for a transaction that is started on this
                                   server before the transaction service initiates timeout completion.
        optionalParmsList - A list of name-value pairs for other Transaction Service parameters.  Each name-value pair should be
                            specified as a list, so this parameter is actually a list of lists.  The following optional
                            parameters can be specified:

                              - 'LPSHeuristicCompletion' - Specifies the direction that is used to complete a transaction that
                                                           has a heuristic outcome; either the application server commits or
                                                           rolls back the transaction, or depends on manual completion by the
                                                           administrator.  Valid values are 'MANUAL', 'ROLLBACK', 'COMMIT'.
                              - 'httpProxyPrefix' - Select this option to specify the external endpoint URL information to
                                                    use for WS-AT and WS-BA service endpoints in the field.
                              - 'httpsProxyPrefix' - Select this option to select the external endpoint URL information to
                                                     use for WS-AT and WS-BA service endpoints from the list.
                              - 'enableFileLocking' - Specifies whether the use of file locks is enabled when opening
                                                      the transaction service recovery log.  Valid values are 'true' and 'false'.
                              - 'transactionLogDirectory' - Specifies the name of a directory for this server where the
                                                            transaction service stores log files for recovery.
                              - 'enableProtocolSecurity' - Specifies whether the secure exchange of transaction service
                                                           protocol messages is enabled.  Valid values are 'true' and 'false'.
                              - 'heuristicRetryWait' - Specifies the number of seconds that the application server waits
                                                       before retrying a completion signal, such as commit or rollback,
                                                       after a transient exception from a resource manager or remote partner.
                              - 'enableLoggingForHeuristicReporting' - Specifies whether the application server logs
                                                                       about-to-commit-one-phase-resource events from
                                                                       transactions that involve both a one-phase commit
                                                                       resource and two-phase commit resources.  Valid values
                                                                       are 'true' and 'false'.
                              - 'acceptHeuristicHazard' - Specifies whether all applications on this server accept the
                                                          possibility of a heuristic hazard occurring in a two-phase
                                                          transaction that contains a one-phase resource.  Valid values
                                                          are 'true' and 'false'.
                              - 'heuristicRetryLimit' - Specifies the number of times that the application server retries
                                                        a completion signal, such as commit or rollback.

        Here is an example of how the 'optionalParmsList" argument could be built by the caller:

        optionalParmsList = []
        optionalParmsList.append( [ 'heuristicRetryWait', '300' ] )
        optionalParmsList.append( [ 'heuristicRetryLimit', '5' ] )
    """

    m = "configureTransactionService:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "TransactionService"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            transServiceID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; transServiceID = %s" % transServiceID)

            attrs = []
            attrs.append( [ 'asyncResponseTimeout', asyncResponseTimeout ] )
            attrs.append( [ 'clientInactivityTimeout', clientInactivityTimeout ] )
            attrs.append( [ 'propogatedOrBMTTranLifetimeTimeout', maximumTransactionTimeout ] )
            attrs.append( [ 'totalTranLifetimeTimeout', totalTranLifetimeTimeout ] )

            if optionalParmsList != []:
                attrs = attrs + optionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (transServiceID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef


def configureORBService (nodename, servername, requestTimeout, locateRequestTimeout, useServerThreadPool='false', optionalParmsList=[]):
    """ This function configures the Object Request Broker Service for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose ORB Service is to be configured.
        requestTimeout - Specifies the number of seconds to wait before timing out on a request message.
                         Valid range is 0 - the largest integer recognized by Java.
        locateRequestTimeout - Specifies the number of seconds to wait before timing out on a LocateRequest message.
                               Valid range is 0 - 300.
        useServerThreadPool - Specifies whether the ORB uses thread pool settings from the server-defined thread pool
                              ('true') or the thread pool attribute of the ORB object ('false').  Valid values are
                              'true' and 'false'.
        optionalParmsList - A list of name-value pairs for other ORB Service parameters.  Each name-value pair should be
                            specified as a list, so this parameter is actually a list of lists.  The following optional
                            parameters can be specified:

                              - 'forceTunnel' - Controls how the client ORB attempts to use HTTP tunneling.
                                                Valid values are 'never', 'whenrequired', 'always'.
                              - 'tunnelAgentURL' - Specifies the Web address of the servlet to use in support of HTTP tunneling.
                              - 'connectionCacheMinimum' - Specifies the minimum number of entries in the ORB connection cache.
                                                           Valid range is any integer that is at least 5 less than the value
                                                           specified for the Connection cache maximum property.
                              - 'connectionCacheMaximum' - Specifies the maximum number of entries that can occupy the ORB
                                                           connection cache before the ORB starts to remove inactive connections
                                                           from the cache. Valid range is 10 - largest integer recognized by Java.
                              - 'requestRetriesDelay' - Specifies the number of milliseconds between request retries.
                                                        Valid range is 0 to 60000.
                              - 'requestRetriesCount' - Specifies the number of times that the ORB attempts to send a request
                                                        if a server fails. Retrying sometimes enables recovery from transient
                                                        network failures.  Valid range is 1 - 10.
                              - 'commTraceEnabled' - Enables the tracing of ORB General Inter-ORB Protocol (GIOP) messages.
                                                     Valid values are 'true' and 'false'.
                              - 'noLocalCopies' - Specifies how the ORB passes parameters. If enabled, the ORB passes
                                                  parameters by reference instead of by value, to avoid making an object copy.
                                                  Valid values are 'true' and 'false'.

        Here is an example of how the 'optionalParmsList" argument could be built by the caller:

        optionalParmsList = []
        optionalParmsList.append( [ 'requestRetriesDelay', '3000' ] )
        optionalParmsList.append( [ 'requestRetriesCount', '5' ] )
        optionalParmsList.append( [ 'commTraceEnabled', 'true' ] )
    """

    m = "configureORBService:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "ObjectRequestBroker"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            ORBServiceID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; ORBServiceID = %s" % ORBServiceID)

            attrs = []
            attrs.append( [ 'requestTimeout', requestTimeout ] )
            attrs.append( [ 'locateRequestTimeout', locateRequestTimeout ] )
            attrs.append( [ 'useServerThreadPool', useServerThreadPool ] )

            if optionalParmsList != []:
                attrs = attrs + optionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (ORBServiceID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef


def configureI18NService (nodename, servername, enable):
    """ This function configures the Internationalization (I18N) Service for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose I18N Service is to be configured.
        enable - specifies whether the I18N Service is to be enabled or disabled.
                 Valid values are 'true' and 'false'.

    """

    m = "configureI18NService:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "I18NService"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            I18NServiceID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; I18NServiceID = %s" % I18NServiceID)

            attrs = []
            attrs.append( [ 'enable', enable ] )
            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (I18NServiceID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef


def configureJPAService (nodename, servername, persistenceProvider, JTADSJndiName, nonJTADSJndiName ):

    """ This function configures the Java Persistence API (JPA) Service for the specified server
        (for WebSphere Application Server Version 7.0 and above).

        Function parameters:

          nodename - the name of the node on which the server to be configured resides.
          servername - the name of the server whose JPA Service is to be configured.
          persistenceProvider - Specifies the default persistence provider for the application server
                                container.
          JTADSJndiName - Specifies the JNDI name of the default JTA data source used by persistence
                          units for the application server container.
          nonJTADSJndiName - Specifies the JNDI name of the default non-JTA data source used by
                             persistence units for the application server container.

        Return Value:

          This function returns the config ID of the JavaPersistenceAPIService for the
          specified server (regardless of whether or not the object existed already).
    """

    m = "configureJPAService:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "JavaPersistenceAPIService"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            JPAServiceID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; JPAServiceID = %s" % JPAServiceID)

            attrs = []
            attrs.append( [ 'defaultPersistenceProvider', persistenceProvider ] )
            attrs.append( [ 'defaultJTADataSourceJNDIName', JTADSJndiName ] )
            attrs.append( [ 'defaultNonJTADataSourceJNDIName', nonJTADSJndiName ] )

            if JPAServiceID == "":
                sop (m, "The JavaPersistenceAPIService for server %s on node %s does not exist and will be created." % (servername, nodename))
                sop (m, "Calling AdminConfig.create with the following parameters: %s" % attrs)
                JPAServiceID = AdminConfig.create (serviceName, serverID, attrs)
                sop (m, "Returned from AdminConfig.create; JPAServiceID = %s" % JPAServiceID)
            else:
                sop (m, "The JavaPersistenceAPIService for server %s on node %s already exists and will be modified." % (servername, nodename))
                sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
                AdminConfig.modify (JPAServiceID, attrs)
                sop (m, "Returned from AdminConfig.modify")
            #endif

            sop (m, "Exiting function...")
            return JPAServiceID
        #endif
    #endif
#endDef

############################################################
# Orb specific utilities

def getOrbId( nodename, servername ):
    """Returns the ID string for the ORB of the specified server or proxy."""
    m = "getOrbId:"
    sop(m,"Entry. nodename=%s servername=%s" % ( nodename, servername ))

    # Get the ID string for the specified server or proxy.
    server_id = getServerId(nodename, servername)
    sop(m,"server_id=%s" % ( server_id ))

    # Get the ID string for the ORB.
    orb_id = AdminConfig.list( "ObjectRequestBroker", server_id )

    sop (m, "Exit. Returning orb_id=%s" % ( orb_id ))
    return orb_id

def getOrbCustomProperty( nodename, servername, propname ):
    """Returns the current value of the specified property
    in the orb of the specified server or proxy."""
    m = "getOrbCustomProperty:"
    sop(m,"Entry. nodename=%s servername=%s propname=%s" % ( nodename, servername, propname ))

    # Get the ID string for the specified server or proxy.
    orb_id = getOrbId( nodename, servername )
    sop(m,"orb_id=%s" % ( orb_id ))

    # Get the custom property.
    propvalue = getObjectCustomProperty(orb_id, propname)

    sop(m,"Exit. Returning propvalue=%s" % ( propvalue ))
    return propvalue

def setOrbCustomProperty( nodename, servername, propname, propvalue ):
    """Sets the specified custom property 
    in the orb of the specified server or proxy."""
    m = "setOrbCustomProperty:"
    sop(m,"Entry. nodename=%s servername=%s propname=%s propvalue=%s" % ( nodename, servername, propname, propvalue ))

    # Get the ID string for the specified server or proxy.
    orb_id = getOrbId( nodename, servername )
    sop(m,"orb_id=%s" % ( orb_id ))

    # Set the property.
    setCustomPropertyOnObject(orb_id, propname, propvalue)
    sop(m,"Exit. Successfully set %s=%s" % ( propname, propvalue ))

def deleteOrbCustomProperty( nodename, servername, propname ):
    """Deletes the specified custom property from 
    the orb of the specified server or proxy."""
    m = "deleteOrbCustomProperty:"
    sop(m,"Entry. nodename=%s servername=%s propname=%s" % ( nodename, servername, propname ))

    # Get the ID string for the specified server or proxy.
    orb_id = getOrbId( nodename, servername )
    sop(m,"orb_id=%s" % ( orb_id ))

    # Does it exist?
    propvalue = getObjectCustomProperty(orb_id, propname)
    if propvalue != None:
        # Exists
        # sop(m,"Exists.")
        propsidlist = AdminConfig.showAttribute(orb_id,'properties')[1:-1].split(' ')
        for id in propsidlist:
            name = AdminConfig.showAttribute(id, 'name')
            # sop(m,"id=%s name=%s" % ( id, name ))
            if name == propname:
                AdminConfig.remove(id)
                sop(m,"Exit. Successfully removed propname=%s" % ( propname ))
                return
    sop(m,"Exit. Property propname=%s does not exist." % ( propname ))

############################################################
# Shared Library and Class Loader methods

def createSharedLibrary(libname, jarfile):
    """Creates a shared library on the specified cell with the given name and jarfile"""
    m = "createSharedLibrary:"
    #sop(m,"Entry. Create shared library. libname=%s jarfile=%s" % (repr(libname), repr(jarfile) ))
    cellname = getCellName()
    cell_id = getCellId(cellname)
    #sop(m,"cell_id=%s " % ( repr(cell_id), ))
    result = AdminConfig.create('Library', cell_id, [['name', libname], ['classPath', jarfile]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def createSharedLibraryIsolated (libname, jarfile, isolated):
    """Creates an isolated shared library on the specified cell with the given name and jarfile"""
    m = "createSharedLibraryIsolated:"
    #sop(m,"Entry. Create shared library isolated. libname=%s jarfile=%s isolated=%s" % (repr(libname), repr(jarfile), repr(isolated) ))
    cellname = getCellName()
    cell_id = getCellId(cellname)
    #sop(m,"cell_id=%s " % ( repr(cell_id), ))
    result = AdminConfig.create('Library', cell_id, [['name', libname], ['classPath', jarfile], ['isolatedClassLoader', isolated]])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def associateSharedLibrary (libname, appname, warname):
    """Associates the installed shared library with an EAR"""
    m = "associateSharedLibrary:"
    #sop(m,"Entry. Associate the shared library with an EAR. libname=%s appname=%s warname=%s " % ( repr(libname), repr(appname), repr(warname) ))
    #deployments = AdminConfig.getid('/Deployment:'+appname+'/')
    #deploymentObject = AdminConfig.showAttribute(deployments, 'deployedObject')
    #classloader = AdminConfig.showAttribute(deploymentObject, 'classloader')
    #sop(m,"classloader=%s " % ( repr(classloader), ))
    if warname == '':
        #get the classloader for the app
        classloader = _getApplicationClassLoader(appname)
    else:
        #get the classloader for the war
        classloader = _getWebModuleClassLoader(appname, warname)
    result = AdminConfig.create('LibraryRef', classloader, [['libraryName', libname], ['sharedClassloader', 'true']])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def deleteSharedLibrary(libname):
    """Delete shared library with specified name"""
    m = "deleteSharedLibrary:"
    #sop(m,"Entry. ")
    sharedlibs = _splitlines(AdminConfig.list('Library'))
    #sop(m,"sharedlibs=%s " % ( repr(sharedlibs), ))
    for lib in sharedlibs:
        if lib.startswith(libname):
            AdminConfig.remove(lib)
    #sop(m,"Exit. ")

def _getWebModuleClassLoader(appname, modulename):
    """Gets the classloader for the web module"""
    m = "_getWebModuleClassLoader:"
    #sop(m,"Entry. Gets the classloader for the web module. appname=%s modulename=%s " % ( repr(appname), repr(modulename) ))
    deployments = AdminConfig.getid('/Deployment:'+appname+'/')
    deploymentObject = AdminConfig.showAttribute(deployments, 'deployedObject')
    for webmoduledeployment in getObjectAttribute(deploymentObject, 'modules'):
      uri = getObjectAttribute(webmoduledeployment, 'uri')
      if uri == modulename:
        found=True
        break
    if not found:
        raise Exception("INTERNAL ERROR: Unable to find WebModuleDeployment object for module %s in application %s" % (modulename, appname))
    classloader = AdminConfig.showAttribute(webmoduledeployment, 'classloader')
    if None == classloader or "" == classloader:
        classloader = AdminConfig.create('Classloader', webmoduledeployment, [])
    return classloader

def _getApplicationClassLoader(appname):
    """Gets the classloader for the web module"""
    m = "_getWebModuleClassLoader:"
    #sop(m,"Entry. Gets the classloader for the web module. appname=%s modulename=%s " % ( repr(appname), repr(modulename) ))
    deployments = AdminConfig.getid('/Deployment:'+appname+'/')
    deploymentObject = AdminConfig.showAttribute(deployments, 'deployedObject')
    classloader = AdminConfig.showAttribute(deploymentObject, 'classloader')
    return classloader

def deleteAllSharedLibraries():
    """Deletes all shared libraries"""
    m = "deleteAllSharedLibraries:"
    #sop(m,"Entry. ")
    sharedlibs = _splitlines(AdminConfig.list('Library'))
    #sop(m,"sharedlibs=%s " % ( repr(sharedlibs), ))
    for lib in sharedlibs:
        AdminConfig.remove(lib)
    #sop(m,"Exit. ")

def createSharedLibraryClassloader(nodename, servername, libname):
    """Creates a classloader on the specified appserver and associates it with a shared library"""
    m = "createSharedLibraryClassloader:"
    #sop(m,"Entry. Create shared library classloader. nodename=%s servername=%s libname=%s " % ( repr(nodename), repr(servername), repr(libname) ))
    server_id = getServerByNodeAndName(nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    appserver = AdminConfig.list('ApplicationServer', server_id)
    #sop(m,"appserver=%s " % ( repr(appserver), ))
    classloader = AdminConfig.create('Classloader', appserver, [['mode', 'PARENT_FIRST']])
    #sop(m,"classloader=%s " % ( repr(classloader), ))
    result = AdminConfig.create('LibraryRef', classloader, [['libraryName', libname], ['sharedClassloader', 'true']])
    #sop(m,"Exit. result=%s" % ( repr(result), ))

def setClassloaderToParentLast(appname):
    deployments = AdminConfig.getid("/Deployment:%s/" % (appname) )
    deploymentObject = AdminConfig.showAttribute(deployments, "deployedObject")
    AdminConfig.modify(deploymentObject, [['warClassLoaderPolicy', 'SINGLE']])
    classloader = AdminConfig.showAttribute(deploymentObject, "classloader")
    AdminConfig.modify(classloader, [['mode', 'PARENT_LAST']])
    modules = AdminConfig.showAttribute(deploymentObject, "modules")
    arrayModules = modules[1:len(modules)-1].split(" ")
    for module in arrayModules:
        if module.find('WebModuleDeployment') != -1:
            AdminConfig.modify(module, [['classloaderMode', 'PARENT_LAST']])

def deleteAllClassloaders(nodename, servername):
    """Deletes all classloaders for the given server"""
    m = "deleteAllClassloaders:"
    #sop(m,"Entry. Deleting All Classloaders. nodename=%s servername=%s " % ( repr(nodename), repr(servername) ))
    server_id = getServerByNodeAndName(nodename, servername )
    #sop(m,"server_id=%s " % ( repr(server_id), ))
    appserver = AdminConfig.list('ApplicationServer', server_id)
    #sop(m,"appserver=%s " % ( repr(appserver), ))
    classloaders = _splitlist(AdminConfig.showAttribute(appserver, 'classloaders'))
    #sop(m,"classloaders=%s " % ( repr(classloaders), ))
    if len(classloaders) > 0:
        for classloader in classloaders:
            AdminConfig.remove(classloader)
    #sop(m,"Exit. ")

###############################################################################
# Custom Service methods

def getCustomService(nodename, servername):
    server_id = getServerByNodeAndName( nodename, servername )
    return AdminConfig.list('CustomService', server_id)

def removeCustomService(nodename,servername,propertyname):
    server_id = getServerByNodeAndName(nodename,servername)
    findAndRemove('CustomService',[['displayName', propertyname]],server_id)

def createCustomService(nodename,servername,name,cpath,cname,enabled):
    server_id = getServerByNodeAndName(nodename,servername)
    attrs = []
    attrs.append( [ 'displayName', name ] )
    attrs.append( ['classpath', cpath] )
    attrs.append( ['classname', cname] )
    attrs.append( ['enable', enabled] )
    return removeAndCreate('CustomService', server_id, attrs, ['displayName'])

############################################################
# Replication Domain methods

def createReplicationDomain(domainname, numberofreplicas):
    """Creates a replication domain with the specified name and replicas.
    Set number of replicas to -1 for whole-domain replication.
    Returns the config id of the DataReplicationDomain object."""
    m = "createReplicationDomain:"
    #sop(m,"Entry. Create replication domain. domainname=%s numberofreplicas=%s " % (repr(domainname), repr(numberofreplicas) ))
    cell_id = getCellId()
    #sop(m,"cell_id=%s " % ( repr(cell_id), ))
    domain = AdminConfig.create('DataReplicationDomain', cell_id, [['name', domainname]])
    #sop(m,"domain=%s " % ( repr(domain), ))
    domainsettings = AdminConfig.create('DataReplication', domain, [['numberOfReplicas', numberofreplicas]])
    #sop(m,"domainsettings=%s " % ( repr(domainsettings), ))
    #sop(m,"Exit. ")
    return domain

def deleteAllReplicationDomains():
    """Deletes all data replication domains"""
    m = "deleteAllReplicationDomains:"
    #sop(m,"Entry. ")
    domains = _splitlines(AdminConfig.list('DataReplicationDomain'))
    #sop(m,"domains=%s " % ( repr(domains), ))
    for domain in domains:
        AdminConfig.remove(domain)
    #sop(m,"Exit. ")

def setSessionReplication(objectid, domainname, dataReplicationMode):
    """Given an object which can have a SessionManager - e.g. an ApplicationConfig object, or
    a WebContainer object - set up data replication
    using the named domain (which ought to exist, of course).
    dataReplicationMode should be one of 'SERVER', 'CLIENT', 'BOTH'
    """

    m = "setSessionReplication:"
    #sop(m, "Entry: objectid=%s, domainname=%s, dataReplicationMode=%s" % (objectid, domainname, dataReplicationMode))

    # Look for a session manager object
    sessionmanagers = getObjectsOfType('SessionManager', objectid)
    if 0 == len(sessionmanagers):
        AdminConfig.create('SessionManager', objectid,[])
        sessionmanagers = getObjectsOfType('SessionManager', objectid)
    sesmgr = sessionmanagers[0]
    # Look for a DRS settings object
    drssettingsobjects = getObjectsOfType('DRSSettings', sesmgr)
    if 0 == len(drssettingsobjects):
        AdminConfig.create('DRSSettings', sesmgr, [['messageBrokerDomainName', domainname]])
        drssettingsobjects = getObjectsOfType('DRSSettings', sesmgr)
    drs = drssettingsobjects[0]
    setObjectAttributes(sesmgr, sessionPersistenceMode = 'DATA_REPLICATION')
    setObjectAttributes(drs, messageBrokerDomainName = domainname,
                        dataReplicationMode = dataReplicationMode)

def setServerSessionReplication(nodename, servername, domainname, dataReplicationMode):
    """For the given server, set the server session replication settings and enable replication
    using the named domain (which ought to exist, of course).
    dataReplicationMode should be one of 'SERVER', 'CLIENT', 'BOTH'.
    Always sets DATA_REPLICATION as the session persistence mode.
    """
    m = "setServerSessionReplication:"
    #sop(m, "Entry: nodename=%s,servername=%s,domainname=%s,dataReplicationMode=%s" % (nodename, servername, domainname, dataReplicationMode))

    server_id = getServerByNodeAndName(nodename, servername)
    webcontainer = getObjectsOfType('WebContainer', server_id)[0]
    setSessionReplication(webcontainer, domainname, dataReplicationMode)

############################################################
# Misc methods
def _getApplicationDeploymentObject(applicationname):
    """Return the application deployment object for the named application,
    or None if not found"""
    depobjects = getObjectsOfType('ApplicationDeployment')
    found = False
    for dep in depobjects:
        # These config IDs look like:
        # (cells/poir3DmgrCell/applications/Dynamic Cache Monitor.ear/deployments/Dynamic Cache Monitor|deployment.xml#ApplicationDeployment_1184174220132)
        #                                                                         ---------------------
        # where the underlined part is the application name
        parts = dep.split("|")   # parts[0] is now the part up to the |
        parts = parts[0].split("/")
        thisappname = parts[-1]  # the last bit
        if thisappname == applicationname:
            return dep
    return None

def _getApplicationConfigObject(applicationname, createifneeded = True):
    """Return the application config object for the named application.
    Throws exception if the application is not found.
    Returns None if createifneeded is False and there isn't one yet."""
    # Get the application deployment object for the application
    dep = _getApplicationDeploymentObject(applicationname)
    if None == dep:
        raise Exception("setApplicationSessionReplication: Cannot find application named '%s'" % applicationname)
    # Look for an application config object under the deployment object - might not be one yet
    appconfs = getObjectsOfType('ApplicationConfig', dep)
    if 0 == len(appconfs):
        if not createifneeded:
            return None
        AdminConfig.create('ApplicationConfig', dep,[])
        appconfs = getObjectsOfType('ApplicationConfig', dep)
    if len(appconfs) > 1:
        raise Exception("INTERNAL ERROR: found more than one ApplicationConfig object for application %s" % applicationname)
    return appconfs[0]

def _getWebModuleConfigObject(applicationname, modulename, createifneeded = True):
    # Find the WebModuleDeployment object
    # First we need the application deployment object
    app_deploy = _getApplicationDeploymentObject(applicationname)
    # Now the webmoduledeployment object we want should be referenced in 'modules'
    found = False
    for w in getObjectAttribute(app_deploy, 'modules'):
        uri = getObjectAttribute(w, 'uri')
        if uri == modulename:
            found = True
            break
    if not found:
        raise Exception("INTERNAL ERROR: Unable to find WebModuleDeployment object for module %s in application %s" % (modulename, applicationname))
    webmoduledeployment = w
    # See if there's a WebModuleConfig object
    w = getObjectsOfType('WebModuleConfig', webmoduledeployment)
    if len(w) > 1:
        raise Exception("INTERNAL ERROR: more than 1 web module config object found")
    if len(w) == 0 and not createifneeded:
        return None
    if len(w) == 0:
        AdminConfig.create('WebModuleConfig', webmoduledeployment, [])
        w = getObjectsOfType('WebModuleConfig', webmoduledeployment)
    return w[0]

def _getEJBModuleConfigurationObject(applicationname, modulename, createifneeded = True):
    # Find the EJBModuleDeployment object
    # First we need the application deployment object
    app_deploy = _getApplicationDeploymentObject(applicationname)
    # Now the ejbmoduledeployment object we want should be referenced in 'modules'
    found = False
    for w in getObjectAttribute(app_deploy, 'modules'):
        uri = getObjectAttribute(w, 'uri')
        if uri == modulename:
            found = True
            break
    if not found:
        raise Exception("INTERNAL ERROR: Unable to find EJBModuleDeployment object for module %s in application %s" % (modulename, applicationname))
    ejbmoduledeployment = w
    # See if there's a EJBModuleConfiguration object
    w = getObjectsOfType('EJBModuleConfiguration', ejbmoduledeployment)
    if len(w) > 1:
        raise Exception("INTERNAL ERROR: more than 1 ejb module config object found")
    if len(w) == 0 and not createifneeded:
        return None
    if len(w) == 0:
        AdminConfig.create('EJBModuleConfiguration', ejbmoduledeployment, [])
        w = getObjectsOfType('EJBModuleConfiguration', ejbmoduledeployment)
    return w[0]

def setWebModuleSessionReplication(applicationname, modulename, domainname, dataReplicationMode):
    """For the named application's named web module, override the server session replication settings
    and enable replication
    using the named domain (which ought to exist, of course).
    dataReplicationMode should be one of 'SERVER', 'CLIENT', 'BOTH'

    The module name is the name of the .jar or .war.  You can see this when in the "manage modules"
    admin panel as the first part of the URI column, e.g. if the URI is "Increment.jar,META-INF/ejb-jar.xml"
    then the module name is "Increment.jar"
    """
    webmoduleconfig = _getWebModuleConfigObject(applicationname, modulename, True)
    setSessionReplication(webmoduleconfig, domainname, dataReplicationMode)

def setEJBModuleSessionReplication(applicationname, modulename, domainname, dataReplicationMode):
    """For the named application's named EJB module, override the server session replication settings
    and enable replication
    using the named domain (which ought to exist, of course).
    dataReplicationMode should be one of 'SERVER', 'CLIENT', 'BOTH'

    The module name is the name of the .jar or .war.  You can see this when in the "manage modules"
    admin panel as the first part of the URI column, e.g. if the URI is "Increment.jar,META-INF/ejb-jar.xml"
    then the module name is "Increment.jar"
    """
    ejbmoduleconfig = _getEJBModuleConfigurationObject(applicationname, modulename, True)

    setObjectAttributes(ejbmoduleconfig, enableSFSBFailover = 'true')
    drssettings = getObjectAttribute(ejbmoduleconfig, 'drsSettings')
    if drssettings == None:
        AdminConfig.create('DRSSettings', ejbmoduleconfig, [['messageBrokerDomainName', domainname]])
    else:
        setObjectAttributes(drssettings, messageBrokerDomainName = domainname)

def setApplicationSessionReplication(applicationname, domainname, dataReplicationMode):
    appconf = _getApplicationConfigObject(applicationname)
    setSessionReplication(appconf, domainname, dataReplicationMode)

############################################################
# web container methods

def setWebContainerSessionManagerAttribs( nodename, servername, attrdict ):
    """Sets the WebContainer SessionManager attributes.

    This method supports setting of multiple attributes during one call.
    attrdict is a python dictionary containing strings with attribute names and values.
    For example attrdict = { 'enableUrlRewriting': 'true',
                             'maxWaitTime': '35', }"""
    m = "setWebContainerSessionManagerAttribs:"
    #sop(m,"Entry. nodename=%s servername=%s attrdict=%s" % ( nodename, servername, repr(attrdict)))
    server_id = getServerId(nodename,servername)
    if server_id == None:
        raise m + " Error: Could not find server. servername=%s nodename=%s" % (nodename,servername)
    #sop(m,"server_id=%s" % server_id)
    sessmgr_id_list = getObjectsOfType('SessionManager', server_id)
    #sop(m,"sessmgr_id_list=%s" % ( repr(sessmgr_id_list)) )
    if len(sessmgr_id_list) == 1:
        attrlist = dictToList(attrdict)
        sessmgr_id = sessmgr_id_list[0]
        sop(m,"Setting attributes. sessmgr_id=%s attrlist=%s" % ( sessmgr_id, repr(attrlist) ))
        AdminConfig.modify(sessmgr_id, attrlist)
    else:
        raise m + "ERROR Server has an unexpected number of session manager object(s). sessmgr_id_list=%s" % ( repr(sessmgr_id_list) )
    #sop(m,"Exit.")

def setWebContainerCustomProperty(nodename, servername, propname, propvalue):
    m = "setWebContainerCustomProperty:"
    sop(m, "Entry. Setting custom property %s=%s" % (propname,propvalue))
    server_id = getServerByNodeAndName( nodename, servername )
    sop(m,"server_id=%s " % ( repr(server_id), ))
    webcontainer_id = AdminConfig.list('WebContainer', server_id)
    sop(m,"webcontainer_id=%s " % ( repr(webcontainer_id), ))
    setCustomPropertyOnObject(webcontainer_id, propname, propvalue)

def setWebContainerDefaultVirtualHostName(nodename, servername, virtualHostName):
    """Set the default virtual host name for the web container."""
    m = "setWebContainerDefaultVirtualHostName:"
    sop(m, "Entry. Setting Default Virtual Host Name %s" % (virtualHostName))
    webcontainer_id = getWebcontainer(nodename, servername)
    sop(m,"webcontainer_id=%s " % ( repr(webcontainer_id), ))
    result = AdminConfig.modify(webcontainer_id, [['defaultVirtualHostName', virtualHostName]])
    sop(m,"Exit. result=%s" % ( repr(result), ))

def getWebcontainer(nodename, servername):
    server_id = getServerByNodeAndName( nodename, servername )
    return AdminConfig.list('WebContainer', server_id)

def removeWebContainerProp(nodename,servername,propertyname):
    wc = getWebcontainer(nodename,servername)
    findAndRemove('Property', [['name', propertyname]], wc)

def createWebContainerProp(nodename,servername,name,value):
    wc = getWebcontainer(nodename,servername)
    attrs = []
    attrs.append( [ 'name', name ] )
    attrs.append( ['value', value] )
    return removeAndCreate('Property', wc, attrs, ['name'])

def configureWebContainerMBeanStopTransports(nodename,servername):
    mbean = AdminControl.completeObjectName('WebSphere:*,type=WebContainer,cell='+getCellName()+',node='+nodename+',process='+servername)
    AdminControl.invoke(mbean,'stopTransports')

def configureWebContainerMBeanStartTransports(nodename,servername):
    mbean = AdminControl.completeObjectName('WebSphere:*,type=WebContainer,cell='+getCellName()+',node='+nodename+',process='+servername)
    AdminControl.invoke(mbean,'startTransports')

def modifyUrlRewriting(nodename,servername,enabled):
    server_id = getServerByNodeAndName(nodename,servername)
    sessionmanager = AdminConfig.list('SessionManager', server_id)
    AdminConfig.modify(sessionmanager, [['enableUrlRewriting',enabled], ['enableSSLTracking', 'false'], ['enableProtocolSwitchRewriting', 'false']])

def modifyCookies(nodename,servername,enabled,maxAgeInSec=10):
    server_id = getServerByNodeAndName(nodename,servername)
    sessionmanager = AdminConfig.list('SessionManager', server_id)
    AdminConfig.modify(sessionmanager, [['enableCookies',enabled]])
    cookie_id = AdminConfig.list('Cookie',sessionmanager)
    AdminConfig.modify(cookie_id,[['maximumAge', maxAgeInSec]])

def configureServerARD(cellName, nodeName, serverName, allowAsyncRequestDispatching, asyncIncludeTimeout):
    """allowAsyncRequestDispatching is a boolean; default is false
    asyncIncludeTimeout is an integer to describe seconds; default is 60000"""
    server_id = getServerByNodeAndName(nodeName,serverName)
    webcontainer = AdminConfig.list('WebContainer', server_id)
    AdminConfig.modify(webcontainer, [['allowAsyncRequestDispatching', allowAsyncRequestDispatching],['asyncIncludeTimeout', asyncIncludeTimeout]])

def configureDeploymentARD(appName, asyncRequestDispatchType):
    """asyncRequestDispatchType can be: DISABLED, SERVER_SIDE, or CLIENT_SIDE"""
    deployments = AdminConfig.getid("/Deployment:%s/" % (appName) )
    deployedObject = AdminConfig.showAttribute(deployments, 'deployedObject')
    AdminConfig.modify(deployedObject, [['asyncRequestDispatchType', asyncRequestDispatchType]])

############################################################
# misc methods
def getSopTimestamp():
    """Returns the current system timestamp in a nice internationally-generic format."""
    # Assemble the formatting string in pieces, so that some code libraries do not interpret
    # the strings as special keywords and substitute them upon extraction.
    formatting_string = "[" + "%" + "Y-" + "%" + "m" + "%" + "d-" + "%" + "H" + "%" + "M-" + "%" + "S00]"
    return time.strftime(formatting_string)

DEBUG_SOP=0
def enableDebugMessages():
    """
    Enables tracing by making future calls to the sop() method actually print messages.
    A message will also be printed to notify the user that trace messages will now be printed.
    """
    global DEBUG_SOP
    DEBUG_SOP=1
    sop('enableDebugMessages', 'Verbose trace messages are now enabled; future debug messages will now be printed.')

def disableDebugMessages():
    """
    Disables tracing by making future calls to the sop() method stop printing messages.
    If tracing is currently enabled, a message will also be printed to notify the user that future messages will not be printed.
    (If tracing is currently disabled, no message will be printed and no future messages will be printed).
    """
    global DEBUG_SOP
    sop('enableDebugMessages', 'Verbose trace messages are now disabled; future debug messages will not be printed.')
    DEBUG_SOP=0

def sop(methodname,message):
    """Prints the specified method name and message with a nicely formatted timestamp.
    (sop is an acronym for System.out.println() in java)"""
    global DEBUG_SOP
    if(DEBUG_SOP):
        timestamp = getSopTimestamp()
        print "%s %s %s" % (timestamp, methodname, message)

def emptyString(strng):
    """Returns True if the string is null or empty."""
    if None == strng or "" == strng:
        return True
    return False

##############################################################################
# Trust Association

def setTrustAssociation(truefalse):
    """Set Trust Association in the cell on or off"""
    ta = _splitlines(AdminConfig.list('TrustAssociation'))[0]
    if truefalse:
        arg = 'true'
    else:
        arg= 'false'
    AdminConfig.modify(ta, [['enabled', arg]])

def getTrustAssociation():
    """Return true or false - whether Trust Association is enabled"""
    ta = _splitlines(AdminConfig.list('TrustAssociation'))[0]
    value = AdminConfig.showAttribute(ta,'enabled')
    return value == 'true'

#-------------------------------------------------------------------------------
# setup attribute values for AuthenticationMechanism using LTPA ConfigId
#-------------------------------------------------------------------------------
def _doAuthenticationMechanism(domainHostname):
    """Turn on LTPA authentication - be sure to set up an LDAP user
    registry first -- see doLDAPUserRegistry"""
    global AdminConfig
    ltpaId = _getLTPAId()
    attrs1 = [["singleSignon", [["requiresSSL", "false"], ["domainName", domainHostname], ["enabled", "true"]]]]

    if len(ltpaId) > 0:
        try:
            AdminConfig.modify(ltpaId, attrs1)
        except:
            print "AdminConfig.modify(%s,%s) caught an exception" % (ltpaId,repr(attrs1))
            raise
    else:
        raise "LTPA configId was not found"
    return

def _getLDAPUserRegistryId():
    """get LDAPUserRegistry id"""
    global AdminConfig
    try:
        ldapObject = AdminConfig.list("LDAPUserRegistry")
        if len(ldapObject) == 0:
            print "LDAPUserRegistry ConfigId was not found"
            return
        ldapUserRegistryId = _splitlines(ldapObject)[0]
        #print "Got LDAPUserRegistry ConfigId is " + ldapUserRegistryId
        return ldapUserRegistryId
    except:
        print "AdminConfig.list('LDAPUserRegistry') caught an exception"
        raise
    return

def _getLTPAId():
    """get LTPA config id"""
    global AdminConfig
    try:
        ltpaObjects = AdminConfig.list("LTPA")
        if len(ltpaObjects) == 0:
            print "LTPA ConfigId was not found"
            return
        ltpaId = _splitlines(ltpaObjects)[0]
        #print "Got LTPA ConfigId is " + ltpaId
        return ltpaId
    except:
        print "AdminConfig.list('LTPA') caught an exception"
        raise
        return None

def _getSecurityId():
    global AdminControl, AdminConfig
    cellName = getCellName()
    try:
        param = "/Cell:" + cellName + "/Security:/"
        secId = AdminConfig.getid(param)
        if len(secId) == 0:
            print "Security ConfigId was not found"
            return None

        #print "Got Security ConfigId is " + secId
        return secId
    except:
        print "AdminConfig.getid(%s) caught an exception" % param
        return None

def _doLDAPUserRegistry(ldapServerId,
                        ldapPassword,
                        ldapServer,
                        ldapPort,
                        baseDN,
                        primaryAdminId,
                        bindDN,
                        bindPassword,
                        type = "IBM_DIRECTORY_SERVER",
                        searchFilter = None,
                        sslEnabled = None,
                        sslConfig = None,
                        ):
    m = "_doLDAPUserRegistry:"
    sop(m,"ENTRY")

    attrs2 = [["primaryAdminId", primaryAdminId],
              ["realm", ldapServer+":"+ldapPort],
              ["type", type],
              ["baseDN", baseDN],
              ["reuseConnection", "true"],
              ["hosts", [[["host", ldapServer],
                          ["port", ldapPort]]]]]

    if ldapServerId == None:
        # Use automatically generated server ID
        attrs2.extend( [["serverId", ""],
                        ["serverPassword","{xor}"],
                        ["useRegistryServerId","false"],
                        ] )
    else:
        # use specified server id
        attrs2.extend([["serverId", ldapServerId],
                       ["serverPassword", ldapPassword],
                       ["useRegistryServerId","true"],
                      ])
    if bindDN != None:
        attrs2.append( ["bindDN", bindDN] )
    if bindPassword != None:
        attrs2.append( ["bindPassword", bindPassword] )

    if sslEnabled != None:
        attrs2.append( ["sslEnabled", sslEnabled] )
    if sslConfig != None:
        attrs2.append( ["sslConfig", sslConfig] )

    ldapUserRegistryId = _getLDAPUserRegistryId()
    if len(ldapUserRegistryId) > 0:
        try:
            hostIdList = AdminConfig.showAttribute(ldapUserRegistryId, "hosts")
            sop(m, "hostIdList=%s" % repr(hostIdList))
            if len(hostIdList) > 0:
                hostIdLists = stringListToList(hostIdList)
                sop(m, "hostIdLists=%s" % repr(hostIdLists))
                for hostId in hostIdLists:
                    sop(m, "Removing hostId=%s" % repr(hostId))
                    AdminConfig.remove(hostId)
                    sop(m, "Removed hostId %s\n" % hostId)
            try:
                sop(m,"about to modify ldapuserregistry: %s" % repr(attrs2))
                AdminConfig.modify(ldapUserRegistryId, attrs2)
            except:
                sop(m, "AdminConfig.modify(%s,%s) caught an exception\n" % (ldapUserRegistryId,repr(attrs2)))
                raise
            # update search filter if necessary
            if searchFilter != None:
                try:
                    origSearchFilter = AdminConfig.showAttribute(ldapUserRegistryId,"searchFilter")
                except:
                    sop(m, "AdminConfig.showAttribute(%s, 'searchFilter') caught an exception" % ldapUserRegistryId)
                    raise
                try:
                    updatedValues = dictToList(searchFilter)
                    sop(m,"About to update searchFilter: %s" % repr(updatedValues))
                    AdminConfig.modify(origSearchFilter, updatedValues)
                except:
                    sop(m, "AdminConfig.modify(%s,%s) caught an exception\n" % (origSearchFilter,repr(updatedValues)))
                    raise
        except:
            sop(m, "AdminConfig.showAttribute(%s, 'hosts') caught an exception" % ldapUserRegistryId)
            raise
    else:
        sop(m, "LDAPUserRegistry ConfigId was not found\n")
    return

def _doGlobalSecurity(java2security = "false"):
    ltpaId = _getLTPAId()
    ldapUserRegistryId = _getLDAPUserRegistryId()
    securityId = _getSecurityId()

    attrs3 = [["activeAuthMechanism", ltpaId], ["activeUserRegistry", ldapUserRegistryId], ["enabled", "true"], ["enforceJava2Security", java2security]]
    if (len(securityId) > 0) or (len(ltpaId) > 0) or (len(ldapUserRegistryId) > 0):
        try:
            AdminConfig.modify(securityId, attrs3)
        except:
            print "AdminConfig.modify(%s,%s) caught an exception\n" % (securityId,repr(attrs3))
    else:
        print "Any of the Security, LTPA or LDAPUserRegistry ConfigId was not found\n"
    return

def enableLTPALDAPSecurity(server, port, baseDN,
                           primaryAdminId,
                           serverId = None, serverPassword = None,
                           bindDN = None, bindPassword = None,
                           domain = "",
                           type = 'IBM_DIRECTORY_SERVER',
                           searchFilter = None,
                           sslEnabled = None,
                           sslConfig = None,
                           enableJava2Security = "false"):
    """Set up an LDAP user registry, turn on WebSphere admin security."""

    m = "enableLTPALDAPSecurity:"
    sop(m,"Entry: server=%s port=%s type=%s" % (server, "%d"%int(port), type))

    _doAuthenticationMechanism(domain)
    _doLDAPUserRegistry(primaryAdminId = primaryAdminId,
                        ldapServerId = serverId,
                        ldapPassword = serverPassword,
                        ldapServer = server,
                        ldapPort = "%d"%int(port),
                        baseDN = baseDN,
                        bindDN = bindDN,
                        bindPassword = bindPassword,
                        type = type,
                        searchFilter = searchFilter,
                        sslEnabled = sslEnabled,
                        sslConfig = sslConfig,
                        )
    _doGlobalSecurity(enableJava2Security)


def isGlobalSecurityEnabled():
    """ Check if Global Security is enabled. If security is enabled return True else False"""
    m = "isGlobalSecurityEnabled"
    sop(m,"Entry: Checking if Global Security is enabled...")
    securityon = AdminTask.isGlobalSecurityEnabled()
    sop(m,"Global Security enabled: %s" % (securityon))
    return securityon

def enableKerberosSecurity(kdcHost, krb5Realm, kdcPort, dns, serviceName, encryption,
                           enabledGssCredDelegate, allowKrbAuthForCsiInbound, allowKrbAuthForCsiOutbound,
                           trimUserName, keytabPath, configPath):
    cmd = "-realm "+krb5Realm
    if encryption != None:
        cmd += " -encryption "+encryption
    cmd += " -krbPath "+configPath+" -keytabPath "+keytabPath+" -kdcHost "+kdcHost
    if kdcPort != None:
        cmd += " -kdcPort "+kdcPort
    cmd += " -dns "+dns

    # Do not create duplicate config files to avoid file already exist exception.
    # Fix to use java classes and not the jython io.path package so it works under WAS/ESB/WPS 6.0.2.
    import java.io.File
    file = java.io.File(configPath)
    if not file.exists():
        m = "enableKerberosSecurity"
        sop(m, "Creating %s" % configPath)
        AdminTask.createKrbConfigFile(cmd)

    cmd = "-enabledGssCredDelegate "+enabledGssCredDelegate
    cmd += " -allowKrbAuthForCsiInbound "+allowKrbAuthForCsiInbound
    cmd += " -allowKrbAuthForCsiOutbound "+allowKrbAuthForCsiOutbound
    cmd += " -krb5Config "+configPath
    cmd += " -krb5Realm "+krb5Realm
    cmd += " -serviceName "+serviceName
    cmd += " -trimUserName "+trimUserName
    AdminTask.createKrbAuthMechanism(cmd)

def isAdminSecurityEnabled():
    """Determine if admin security is enabled - returns True or False"""
    securityId = _getSecurityId()
    enabled = AdminConfig.showAttribute(securityId, 'enabled')
    return enabled == "true"

def setJava2Security(onoff):
    """Given a boolean (0 or 1, False or True), set java 2 security for the cell"""
    securityId = _getSecurityId()
    if onoff:
        value = "true"
    else:
        value = "false"
    setObjectAttributes(securityId, enforceJava2Security = value)

def enableJava2Security():
    """Enables java 2 security"""
    AdminTask.setAdminActiveSecuritySettings('-enforceJava2Security true')

def disableJava2Security():
    """Disables java 2 security"""
    AdminTask.setAdminActiveSecuritySettings('-enforceJava2Security false')

def setDigestAuthentication(enableAuthInt = 'null',
                            disableMultipleNonce = 'null',
                            nonceMaxAge = 'null',
                            hashedCreds = 'null',
                            hashedCredsRealm = 'null'):
    """Set the specified digest authentication settings, any unspecified settings are left unchanged"""
    digestAuth = AdminConfig.list('DigestAuthentication')

    # if digestAuth object doesn't exist, create it on the LTPA object
    if digestAuth == '':
        LTPA = AdminConfig.list('LTPA')
        if LTPA == '':
            raise "Could not find LTPA object on which to create Digest Auth settings"
        digestAuth = AdminConfig.create('DigestAuthentication', LTPA, '')

    # Create the list of settings to modify, done this way to allow unspecified options to be unchanged
    modifyString = '['
    if enableAuthInt != 'null':
        modifyString += '[enableDigestAuthenticationIntegrity "' + enableAuthInt + '"] '
    if disableMultipleNonce != 'null':
        modifyString += '[disableMultipleUseOfNonce "' + disableMultipleNonce + '"] '
    if nonceMaxAge != 'null':
        modifyString += '[nonceTimeToLive "' + nonceMaxAge + '"] '
    if hashedCreds != 'null':
        modifyString += '[hashedCreds "' + hashedCreds + '"] '
    if hashedCredsRealm != 'null':
        modifyString += '[hashedRealm "' + hashedCredsRealm + '"] '
    modifyString += ']'

    #print "Modify String: " + modifyString

    AdminConfig.modify(digestAuth, modifyString)


#-------------------------------------------------------------------------------
# setup attribute values to disable security using Security ConfigId
#-------------------------------------------------------------------------------
def setAdminSecurityOff():
    """Turn off admin security"""
    securityId = _getSecurityId()
    attrs4 = [["enabled", "false"]]
    if len(securityId) > 0:
        try:
            AdminConfig.modify(securityId, attrs4)
        except:
            print "AdminConfig.modify(%s,%s) caught an exception" % (securityId,repr(attrs4))
    else:
        print "Security configId was not found"
    return

def updateWASPolicy(wasPolicyFilePath, localWASPolicy):
    """ This method updates the specified was.policy
    Parameters:
        wasPolicyFilePath - Configuration file path to an application's was.policy
        localWASPolicy - new was.policy
    Returns:
        no return value
    """
    obj=AdminConfig.extract(wasPolicyFilePath,"was.policy")
    AdminConfig.checkin(wasPolicyFilePath, localWASPolicy,  obj)

##############################################################################

def serverStatus():
    """Display the status of all nodes, servers, clusters..."""
    print "Server status"
    print "============="

    nodes = _splitlines(AdminConfig.list( 'Node' ))
    for node_id in nodes:
        nodename = getNodeName(node_id)
        hostname = getNodeHostname(nodename)
        platform = getNodePlatformOS(nodename)
        if nodeIsDmgr(nodename):
            print "NODE %s on %s (%s) - Deployment manager" % (nodename,hostname,platform)
        else:
            print "NODE %s on %s (%s)" % (nodename,hostname,platform)
            serverEntries = _splitlines(AdminConfig.list( 'ServerEntry', node_id ))
            for serverEntry in serverEntries:
                sName = AdminConfig.showAttribute( serverEntry, "serverName" )
                sType = AdminConfig.showAttribute( serverEntry, "serverType" )
                isRunning = isServerRunning(nodename,sName)
                if isRunning: status = "running"
                else: status = "stopped"
                print "\t%-18s %-15s %s" % (sType,sName,status)

    appnames = listApplications()
    print "APPLICATIONS:"
    for a in appnames:
        if a != 'isclite' and a != 'filetransfer':
            print "\t%s" % a

def setApplicationSecurity(onoff):
    """Turn application security on or off.
    Note: You also need to set up an LTPA authentication mechanism,
    the application needs to have security enabled, and you need to
    map security roles to users.
    And it simply won't work if you don't have admin security turned on,
    no matter what."""
    securityId = _getSecurityId()
    if onoff:
        attrs = [["appEnabled", "true"]]
    else:
        attrs = [["appEnabled", "false"]]
    if len(securityId) > 0:
        try:
            AdminConfig.modify(securityId, attrs)
        except:
            print "AdminConfig.modify(%s,%s) caught an exception" % (securityId, repr(attrs))
    else:
        print "Security configId was not found"
    return

def createThreadPool(nodename, servername, poolname, minThreads, maxThreads):
    """Create a new thread pool in the given server with the specified settings"""

    # Get the thread pool manager for this server
    server_id = getServerId(nodename, servername)
    tpm = AdminConfig.list('ThreadPoolManager', server_id)
    # Add the pool
    AdminConfig.create('ThreadPool', tpm, [['minimumSize', minThreads],['maximumSize',maxThreads],['name',poolname]])

############################################################
def startall():
    """Start all servers, clusters, apps, etc
    NOT IMPLEMENTED YET"""
    raise "Not implemented yet"
    startAllServerClusters()
    startUnclusteredServers()
    startAllProxyServers()
    # FIXME - get list of applications and start any not already started
    # FIXME - what else?

############################################################

def saveAndSync():
    """Save config changes and sync them - return 0 on sync success, non-zero on failure"""
    m = "save:"
    sop(m, "AdminConfig.queryChanges()")
    changes = _splitlines(AdminConfig.queryChanges())
    for change in changes:
        sop(m, "  "+change)
    rc = 0
    sop(m, "AdminConfig.getSaveMode()")
    mode = AdminConfig.getSaveMode()
    sop(m, "  "+mode)
    sop(m, "AdminConfig.save()")
    AdminConfig.save()
    sop(m, "  Save complete!")
    rc = syncall()
    return rc

def saveAndSyncAndPrintResult():
    """Save config changes and sync them - prints save.result(0) on sync success, save.result(non-zero) on failure"""
    rc = saveAndSync()
    print "save.result(%s)" % (rc)

def save():
    """Save config changes and sync them - return 0 on sync success, non-zero on failure"""
    return saveAndSync()

def reset():
    """Reset config changes (allows throwing away changes before saving - just quitting your script without saving usually does the same thing)"""
    AdminConfig.reset()

##############################################################
# BLA related operations

def createEmptyBLA(blaname):
    """ Create and empty BLA with the name provided by the user. Return 0 if the load succeeds, otherwise return -1 """
    try:
        AdminTask.createEmptyBLA('[-name %s]' % (blaname))
        return 0
    except:
        print sys.exc_type, sys.exc_value
        return -1

def deleteBLA(blaname):
    """ Delete a BLA from the dmgr by its ID (i.e myBLA). Return 0 if the delete succeeds,otherwise return -1 """
    m = "deleteBLA: "
    sop(m,"ENTRY: %s" % blaname)
    try:
        AdminTask.deleteBLA('[-blaID %s]' % blaname)
        return 0
    except:
        #print sys.exc_type, sys.exc_value
        traceback.print_exc()
        return -1

def doesBLAExist(blaname):
    """Return True if the BLA exists"""
    m = "doesBLAExist: "
    sop(m,"ENTRY: %s" % blaname)
    blas = AdminTask.listBLAs(['-blaID',blaname])
    sop(m,"listBLAs returns: %s" % repr(blas))
    return len(blas) > 0

# Info about addCompUnit:

#     // MapTargets
#     public static final int COL_TARGET = 1;

#     // CUOptions
#     public static final int COL_CU_PARENTBLA = 0;
#     public static final int COL_CU_BACKINGID = 1;
#     public static final int COL_CU_NAME = 2;
#     public static final int COL_CU_DESC = 3;
#     public static final int COL_CU_STARTINGWEIGHT = 4;
#     public static final int COL_CU_STARTEDONDISTRIBUTED = 5;
#     public static final int COL_CU_RESTARTBEHAVIORONUPDATE = 6;

#
def addAppSrvCompUnit(blaname,assetname,assetversion,proxyname,cellname,nodename,servername,applicationname,enableLogging):
    # NOTE: not using assetversion - no longer required and appears to cause addCompUnit to fail.
    # Left it in the method's arg list to avoid breaking users of this method.
    m = "addAppSrvCompUnit"
    sop(m,"ENTRY")
    arg = '-blaID %s -cuSourceID assetname=%s -CUOptions [[WebSphere:blaname=%s WebSphere:assetname=%s %s "" 1 false DEFAULT ""]] -MapTargets [[.* %s]] -CustomAdvisorCUOptions [[type=AppServer,cellName=%s,nodeName=%s,serverName=%s,applicationName=%s,isEnableLogging=%s]]' % (blaname, assetname,blaname,assetname,assetname, proxyname,cellname,nodename,servername,applicationname,enableLogging)
    try:
        AdminTask.addCompUnit(arg)
    except:
        sop(m, "addCompUnit threw an exception:")
        trace_back.print_exc()
        return -1
    sop(m, "success")
    return 0

def addClusterCompUnit(blaname,assetname,assetversion,proxyname,cellname,clustername,applicationname,enableLogging):
    # NOTE: not using assetversion - no longer required and appears to cause addCompUnit to fail.
    # Left it in the method's arg list to avoid breaking users of this method.
    m = "addClusterCompuUnit"
    arg = '-blaID %s -cuSourceID assetname=%s -CUOptions [[WebSphere:blaname=%s WebSphere:assetname=%s %s "" 1 false DEFAULT ""]] -MapTargets [[.* %s]] -CustomAdvisorCUOptions [[type=Cluster,clusterName=%s,cellName=%s,applicationName=%s,isEnableLogging=%s]]' % (blaname,assetname, blaname,assetname,assetname, proxyname,clustername,cellname,applicationname,enableLogging)
    sop(m,"AdminTask.addCompUnit(%s)" % repr(arg))
    try:
        AdminTask.addCompUnit(arg)
        return 0
    except:
        sop(m, "addCompUnit threw an exception:")
        trace_back.print_exc()
        return -1

# Haven't tried this yet - once addClusterCompUnit() is working,
# modify this using, that as a model.
def addGSCCompUnit(blaname,assetname,assetversion,proxyname,clustername,enableLogging):
    # NOTE: not using assetversion - no longer required and appears to cause addCompUnit to fail.
    # Left it in the method's arg list to avoid breaking users of this method.
    try:
        AdminTask.addCompUnit('-blaID %s -cuSourceID assetname=%s -CUOptions [[WebSphere:blaname=%s WebSphere:assetname=%s %s "" 1 false DEFAULT ""]] -MapTargets [[.* %s]] -CustomAdvisorCUOptions [[type=GSC,clusterName=%s,isEnableLogging=%s]]' % (blaname,assetname,blaname,assetname,assetname, proxyname,clustername,enableLogging))
        return 0
    except:
        sop(m, "addCompUnit threw an exception:")
        trace_back.print_exc()
        return -1

def addCompUnitToServer(blaname,assetname,nodename,servername):
    # Add CompUnit to a specified server
    m = "addCompUnitToServer"
    arg = '[-blaID WebSphere:blaname=%s -cuSourceID WebSphere:assetname=%s -CUOptions [[WebSphere:blaname=%s WebSphere:assetname=%s %s "" 1 false DEFAULT]] -MapTargets [[default WebSphere:node=%s,server=%s]]]' % (blaname,assetname,blaname,assetname,assetname,nodename,servername)
    sop(m,"AdminTask.addCompUnit(%s)" % repr(arg))
    try:
        AdminTask.addCompUnit(arg)
        return 0
    except:
        sop(m, "addCompUnit threw an exception:")
        trace_back.print_exc()
        return -1

def listCompUnits(blaname):
    """Return a list of the names of the composition units in a bla.  """
    m = "listCompUnits: "
    sop(m,blaname)
    if doesBLAExist(blaname):
        x = AdminTask.listCompUnits(['-blaID', blaname])
        sop(m,x)
        return _splitlines(x)
    return []  # no comp units if no bla

def deleteCompUnit(blaname,assetname):
    """ Delete a CompUnit by its blaID, compUnitID and Edition. Return 0 if the delete succeeds,otherwise return -1 """
    m = "deleteCompUnit: "
    sop(m,"%s %s" % (blaname,assetname))
    try:
        AdminTask.deleteCompUnit('[-blaID %s -cuID %s]' % (blaname, assetname))
        return 0
    except:
        print sys.exc_type, sys.exc_value
        return -1

def importAsset(filepath):
    """ Import an asset by specifying its location (i.e./home/myfilter.jar). Return 0 if the import succeeds, otherwise return -1 """
    m = "importAsset: "
    sop(m,filepath)
    try:
        AdminTask.importAsset('[-source %s -storageType FULL]' % (filepath))
        return 0
    except:
        print sys.exc_type, sys.exc_value
        return -1

def doesAssetExist(filepath):
    """Return true if the asset exists IN THE CONFIGURATION"""
    m = "doesAssetExist: "
    sop(m,filepath)
    assets = AdminTask.listAssets(['-assetID',filepath])
    sop(m,"returned %s" % repr(assets))
    return len(assets) > 0

def deleteAsset(filepath):
    """ Delete an asset by specifying its name (i.e. myfilter.jar). Return 0 if the delete succeeds, otherwise return -1 """
    m = "deleteAsset: "
    sop(m,filepath)
    try:
        AdminTask.deleteAsset('[-assetID %s]' % (filepath))
        return 0
    except:
        #print sys.exc_type, sys.exc_value
        traceback.print_exc()
        return -1

def assetExist(filename):
    """ Check if an asset exist. Return 0 if the asset does not exist,otherwise exit the program """
    assetlist = glob.glob(filename)

    """ number of assets found  """
    numofassets = len(assetlist)
    if numofassets!=0:
        print
        print "ERROR while importing file %s" % (filename)
        print "Can't import and existing asset"
        print
        sys.exit(1)
    else:
        return 0

def startBLA(name):
    """ Start a BLA given its name """
    try:
        AdminControl.getType()
        AdminTask.startBLA('-blaID %s' % name)
        return 0
    except:
        print sys.exc_type, sys.exc_value
        return -1

def stopBLA(name):
    """ Stop a BLA given its name """
    try:
        AdminControl.getType()
        AdminTask.stopBLA('-blaID ' + name)
        return 0
    except:
        print sys.exc_type, sys.exc_value
        return -1

# BLA methods .... these are not tested or complete

def addBLAToCluster(name, source, clusterName):
    """ Adds a BLA and composition unit for BLA to a machine cluster

    Parameters:
        name - Name of BLA to create in String format
        source - File system path to the source file for the application in String format
        clusterName - Name of the cluster to install BLA to in String format.
    Returns:
        0 if installation successful, -1 otherwise
    """
    m = "addBLAToCluster"
    sop(m,"ENTRY: name=%(name)s source=%(source)s clusterName=%(clusterName)s" % locals())
    try:
      blaID = AdminTask.createEmptyBLA('-name ' + name)
      assetID = AdminTask.importAsset('-source ' + source + ' -storageType FULL')
      cuID = AdminTask.addCompUnit('[-blaID ' + blaID + ' -cuSourceID ' + assetID + ' -MapTargets [[.* cluster='+clusterName+']]]')
      save()
    except:
      print 'Error adding CU to BLA on cluster. Exception information', sys.exc_type, sys.exc_value
      return -1
    return 0

def addBLAToClusterWithOptions(name, source, clusterName, optionalArgs):
    """ Adds a BLA and composition unit for BLA to a machine cluster with the ability specify optional args

    Parameters:
        name - Name of BLA to create in String format
        source - File system path to the source file for the application in String format
        clusterName - Name of the cluster to install BLA to in String format.
        optionalArgs - any optional args to run the addCU cmd with. ex "-stepName [[stepValue1 stepValue2]]"
    Returns:
        0 if installation successful, -1 otherwise
    """
    m = "addBLAToClusterWithOptions"
    sop(m,"ENTRY: name=%(name)s source=%(source)s clusterName=%(clusterName)s optionalArgs=%(optionalArgs)s" % locals())
    try:
      blaID = AdminTask.createEmptyBLA('-name ' + name)
      assetID = AdminTask.importAsset('-source ' + source + ' -storageType FULL')
      cuID = AdminTask.addCompUnit('[-blaID ' + blaID + ' -cuSourceID ' + assetID + ' -MapTargets [[.* cluster='+clusterName+']]' + optionalArgs + ']')
      save()
    except:
      print 'Error adding CU to BLA on cluster. Exception information', sys.exc_type, sys.exc_value
      return -1
    return 0

################################################################

def grepTag(filepath, expression):
    """ search for a given pattern (expression) in a given file (filepath). Return 0 if the pattern was found, otherwise return 1 """
    command = "grep " + expression + " " + filepath
    print command
    tag = os.system(command)
    return tag

def ensureDirectory(path):
    """Verifies or creates a directory tree on the local filesystem."""
    m = "ensureDirectory:"
    sop(m,"Entry. path=%s" % ( path ))
    if not os.path.exists(path):
        sop(m,"Path does not exist. Creating it.")
        os.makedirs(path)
    else:
        sop(m,"Path exists.")

###############################################################################

def viewFilters(cellname, nodename, proxyname, filterprotocol, filterpointname):
    """ Display a subset of filters and its ordinal values """
    typename = "ProxyServerFilterBean"
    mbean = AdminControl.queryNames('cell=%s,type=%s,node=%s,process=%s,*' % (cellname, typename, nodename, proxyname))
    print AdminControl.invoke(mbean, 'viewFilters', '[%s %s]' % (filterprotocol, filterpointname))

def viewAllFilter(cellname,nodename,proxyname):
    """ Display the list of filters and its ordinal values """
    typename = "ProxyServerFilterBean"
    mbean = AdminControl.queryNames('cell=%s,type=%s,node=%s,process=%s,*' % (cellname,typename,nodename,proxyname))
    print AdminControl.invoke(mbean,'viewAllFilters')

def modifyFilterOrdinal(cellname,nodename,proxyname,filtername,newordinal):
    """ Modify the ordinal of a custom filter. Return 0 if update succeeds, otherwise return -1 """
    try:
        typename = "ProxyServerFilterBean"
        mbean = AdminControl.queryNames('cell=%s,type=%s,node=%s,process=%s,*' % (cellname,typename,nodename,proxyname))
        AdminControl.invoke(mbean,'modifyOrdinal','[%s %s]' % (filtername,newordinal))
        return 0
    except:
        return -1

def verifyOrdinal(cellname,nodename,proxyname,filtername,filterordinal):
    """ Check if a given filter and its ordinal matches an installed filter's name and ordinal. Return 0 if a match is found, otherwiser return -1 """
    typename = "ProxyServerFilterBean"
    mbean = AdminControl.queryNames('cell=%s,type=%s,node=%s,process=%s,*' % (cellname,typename,nodename,proxyname))
    vaf = AdminControl.invoke( mbean,'viewAllFilters')

    """ return a copy of the string with trailing characters removed """
    filters = vaf.strip()

    """ initializing temporary arrays and return code """
    rc = -999
    b = []
    b2 = []
    b3 = []
    b4 = []

    """ split the string were ',' and '\n' occurs """
    b = re.split('[, \n]', filters)

    """ search each element of b where '(*)' occurs """
    for x in b:
        bool = re.search('[(*)]', x)

        """ if '(*)' occurs, then split that entry and save it in b3"""
        if(bool != None):
            b2 = re.split('[(\*)]', x)
            empty = ''

            if b2[0] == empty:
                b3.append(b2[3])

    """ split each element of b3 where ':' occurs """
    for x in b3:
        b4 = re.split('[:]', x)

        """ get installed filter's name and ordinal """
        instfiltername = b4[0]
        instfilterordinal = b4[1]

        """ check if the intalled filter's name and ordinal matches the filter's name and ordinal provided by the user  """
        if filtername == instfiltername and filterordinal == instfilterordinal:
            rc = 0
            break
        else:
            rc = -1

    return rc

###############################################################################
# JMS Resource Management

def enableService(serviceName, scope):
    """ This method encapsulates the base action of enabling a service for use in WAS.

    Parameters:
        serviceName - Name of the service to be enabled in String format
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "enableService: "
    #--------------------------------------------------------------------
    # Get a list of service objects. There should only be one.
    # If there's more than one, use the first one found.
    #--------------------------------------------------------------------
    services = _splitlines(AdminConfig.list(serviceName, scope))
    if (len(services) == 0):
        sop(m, "The %s service could not be found." % serviceName)
        return
    #endIf
    serviceID = services[0]

    #--------------------------------------------------------------------
    # Set the service enablement
    #--------------------------------------------------------------------
    if (AdminConfig.showAttribute(serviceID, "enable") == "true"):
        sop(m, "The %s service is already enabled." % serviceName)
    else:
        AdminConfig.modify(serviceID, [["enable", "true"]] )
    #endElse
#endDef

def deleteAllSIBuses():
    """Deletes all SIB bus objects in the configuration."""
    m = "deleteAllSIBuses: "
    #sop(m,"Entry.")

    try:
        bus_list = _splitlines(AdminTask.listSIBuses())
        #sopY(m,"bus_list=%s" % ( bus_list ))

        for bus in bus_list:
            #sop(m,"bus=%s" % ( bus ))
            bus_name = getNameFromId(bus)
            sop(m,"Deleting bus_name=%s" % ( bus_name ))
            AdminTask.deleteSIBus('[-bus %s ]' % (bus_name))
    except AttributeError:
            return
    #sop(m,"Exit.")

def addSIBusMember(clusterName, nodeName, serverName, SIBusName):
    """ This method encapsulates the actions needed to add a server-node or a cluster to a bus.
    It will scan the member list to determine if cluster or server-node is already on the specified
    bus. If not, it will add the member.

    Parameters:
        clusterName - Name of the cluster to add onto bus in String format. If value is "none", server-node will be added instead of cluster
        nodeName - Name of node containing server to add onto bus in String format.
        serverName - Name of server to add onto bus in String format.
        SIBusName - Name of bus to add member onto in String format.
    Returns:
        No return value
    """
    m = "addSIBusMember: "

    if(clusterName == "none"):
        for member in _splitlines(AdminTask.listSIBusMembers(["-bus", SIBusName])):
            memberNode = AdminConfig.showAttribute(member, "node")
            memberServer = AdminConfig.showAttribute(member, "server")
            if((memberNode == nodeName)  and (memberServer == serverName)):
                sop(m, "The bus member already exists.")
                return
            #endIf
        #endFor
        AdminTask.addSIBusMember(["-bus", SIBusName, "-node", nodeName, "-server", serverName])
    else:
        for member in _splitlines(AdminTask.listSIBusMembers(["-bus", SIBusName])):
            memberCluster = AdminConfig.showAttribute(member, "cluster")
            if(memberCluster == clusterName):
                sop(m, "The bus member already exists.")
                return
            #endIf
        #endFor

        AdminTask.addSIBusMember(["-bus", SIBusName, "-cluster", clusterName])
    #endElse
#endDef

def createSIBus(clusterName, nodeName, serverName, SIBusName, scope, busSecurity=""):
    """ This method encapsulates the actions needed to create a bus on the current cell.
    It will check to see if the bus already exists. If not, it will add the member.

    Parameters:
        clusterName - Name of the cluster to add onto bus in String format. If value is "none", server-node will be added instead of cluster
        nodeName - Name of node containing server to add onto bus in String format.
        serverName - Name of server to add onto bus in String format.
        SIBusName - Name of bus to create in String format.
        scope - Identification of object (such as server or node) in String format.
        busSecurity - Set this option to TRUE to enforce the authorization policy for the bus
    Returns:
        No return value
    """
    m = "createSIBus: "
    #--------------------------------------------------------------------
    # Create a bus in the current cell.  As well as creating a bus, the
    # createSIBus command will create a default topic space on the bus.
    #--------------------------------------------------------------------
    bus = AdminConfig.getid('/SIBus:%s' % SIBusName)

    if((len(bus) == 1) or (len(bus) == 0)):
        params = ["-bus", SIBusName, "-description", "Messaging bus for testing"]
        if(not(busSecurity == "")):
            params.append("-busSecurity")
            params.append(busSecurity)
        else:
            # Use deprecated -secure option for compatibility with prior versions of this script.
            params.append("-secure")
            params.append("FALSE")
        #endElse
        AdminTask.createSIBus(params)
    else:
        sop(m, "The %s already exists." % SIBusName)
    #endElse

    #--------------------------------------------------------------------
    # Add SI bus member
    #--------------------------------------------------------------------
    addSIBusMember(clusterName, nodeName, serverName, SIBusName)

    #--------------------------------------------------------------------
    # Enable SIB service
    #--------------------------------------------------------------------
    enableService("SIBService", scope)
#endDef

def createSIBus_ext(SIBusName, desc, busSecurity, interAuth, medAuth, protocol, discard, confReload, msgThreshold ):
    """ This method encapsulates the actions needed to create a bus on the current cell.
    It will check to see if the bus already exists. If not, it will create the bus.

    Parameters:
        SIBusName - Name of bus to create in String format.
        desc - Descriptive information about the bus.
        busSecurity - Enables or disables bus security.
        interAuth - Name of the authentication alias used to authorize communication between messaging engines on the bus.
        medAuth - Name of the authentication alias used to authorize mediations to access the bus.
        protocol - The protocol used to send and receive messages between messaging engines, and between API clients and messaging engines.
        discard - Indicate whether or not any messages left in a queue's data store should be discarded when the queue is deleted.
        confReload - Indicate whether configuration files should be dynamically reloaded for this bus.
        msgThreshold - The maximum number of messages that any queue on the bus can hold.
    Returns:
        No return value
    """
    m = "createSIBus_ext: "
    sop (m, "Entering createSIBus_ext function...")
    #--------------------------------------------------------------------
    # Create a bus in the current cell.
    #--------------------------------------------------------------------
    bus = AdminConfig.getid('/SIBus:%s' % SIBusName)
    if((len(bus) == 1) or (len(bus) == 0)):
        params = ["-bus", SIBusName, "-description", desc, "-interEngineAuthAlias", interAuth, "-mediationsAuthAlias", medAuth, "-protocol", protocol, "-discardOnDelete", discard, "-configurationReloadEnabled", confReload, "-highMessageThreshold", msgThreshold ]
        if(not(busSecurity == "")):
            params.append("-busSecurity")
            params.append(busSecurity)
        else:
            # Use deprecated -secure option for compatibility with prior versions of this script.
            params.append("-secure")
            params.append("FALSE")
        #endElse
        AdminTask.createSIBus(params)
    else:
        sop(m, "The %s already exists." % SIBusName)
    #endElse
#endDef


def addSIBusMember_ext (clusterName, nodeName, serverName, SIBusName, messageStoreType, logSize, logDir, minPermStoreSize, minTempStoreSize, maxPermStoreSize, maxTempStoreSize, unlimPermStoreSize, unlimTempStoreSize, permStoreDirName, tempStoreDirName, createDataSrc, createTables, authAlias, schemaName, dataSrcJNDIName ):

    """ This method encapsulates the actions needed to add a server-node or a cluster to a bus.
    It will scan the member list to determine if cluster or server-node is already on the specified
    bus. If not, it will add the member.

    Parameters:
        clusterName - Name of the cluster to add onto bus in String format. If value is "none", server-node will be added instead of cluster
        nodeName - Name of node containing server to add onto bus in String format.
        serverName - Name of server to add onto bus in String format.
        SIBusName - Name of bus to add member onto in String format.
        messageStoreType - Indicates whether a filestore or datastore should be used.  Valid values are 'filestore' and 'datastore'.
        logSize - The size, in megabytes, of the log file.
        logDir - The name of the log files directory.
        minPermStoreSize - The minimum size, in megabytes, of the permanent store file.
        minTempStoreSize - The minimum size, in megabytes, of the temporary store file.
        maxPermStoreSize - The maximum size, in megabytes, of the permanent store file.
        maxTempStoreSize - The maximum size, in megabytes, of the temporary store file.
        unlimPermStoreSize -'true' if the permanent file store size has no limit; 'false' otherwise.
        unlimTempStoreSize - 'true' if the temporary file store size has no limit; 'false' otherwise.
        permStoreDirName - The name of the store files directory for permanent data.
        tempStoreDirName - The name of the store files directory for temporary data.
        createDataSrc - When adding a server to a bus, set this to true if a default datasource is required. When adding a cluster to a bus, this parameter must not be supplied.
        createTables - Select this option if the messaging engine creates the database tables for the data store. Otherwise, the database administrator must create the database tables.
        authAlias - The name of the authentication alias used to authenticate the messaging engine to the data source.
        schemaName - The name of the database schema used to contain the tables for the data store.
        dataSrcJNDIName - The JNDI name of the datasource to be referenced from the datastore created when the member is added to the bus.
    Returns:
        No return value
    """
    m = "addSIBusMember: "
    sop (m, "Entering addSIBusMember function...")

    params = "-bus", SIBusName

    if(clusterName == "none"):
        for member in _splitlines(AdminTask.listSIBusMembers(["-bus", SIBusName])):
            memberNode = AdminConfig.showAttribute(member, "node")
            memberServer = AdminConfig.showAttribute(member, "server")
            if((memberNode == nodeName)  and (memberServer == serverName)):
                sop(m, "The bus member already exists.")
                return
            #endIf
        #endFor
        params += "-node", nodeName, "-server", serverName
    else:
        for member in _splitlines(AdminTask.listSIBusMembers(["-bus", SIBusName])):
            memberCluster = AdminConfig.showAttribute(member, "cluster")
            if(memberCluster == clusterName):
                sop(m, "The bus member already exists.")
                return
            #endIf
        #endFor
        params += "-cluster", clusterName
    #endElse

    if (messageStoreType.lower() == 'filestore'):
        params += "-fileStore",
        params += "-logSize", logSize, "-logDirectory", logDir,
        params += "-minPermanentStoreSize", minPermStoreSize, "-minTemporaryStoreSize", minTempStoreSize,
        params += "-unlimitedPermanentStoreSize", unlimPermStoreSize, "-unlimitedTemporaryStoreSize", unlimTempStoreSize,

        if (unlimPermStoreSize == 'false'):
            params += "-maxPermanentStoreSize", maxPermStoreSize,
        #endIf

        if (unlimTempStoreSize == 'false'):
            params += "-maxTemporaryStoreSize", maxTempStoreSize,
        #endif
        params += "-permanentStoreDirectory", permStoreDirName, "-temporaryStoreDirectory", tempStoreDirName
    else:
        if (messageStoreType.lower() == 'datastore'):
            params += "-dataStore",
            if(clusterName == "none"):
                params += "-createDefaultDatasource", createDataSrc,
            params += "-datasourceJndiName", dataSrcJNDIName,
            params += "-createTables", createTables,
            params += "-authAlias", authAlias, "-schemaName", schemaName
        #endIf
    #endIf

    AdminTask.addSIBusMember(params)

#endDef


def removeSIBusMember (SIBusName, nodeName, serverName):
    """ This method encapsulates the actions needed to remove a bus member from the named bus.

    Parameters:
        SIBusName - Name of bus to remove member from in String format.
        nodeName - Name of node containing server to remove member from in String format.
        serverName - Name of server to remove member from in String format.
    Returns:
        No return value
    """
    AdminTask.removeSIBusMember(["-bus", SIBusName, "-node", nodeName, "-server", serverName])


def modifySIBusMemberPolicy (SIBusName, clusterName, enableAssistance, policyName):
    """ This method encapsulates the actions needed to modify a bus member policy.

    Parameters:
        SIBusName - Name of bus to modify in String format.
        clusterName - Name of the cluster to modify in String format.
        enableAssistance - TRUE | FALSE in String format.
        policyName - HA | SCALABILITY | SCALABILITY_HA | CUSTOM in String format.
    Returns:
        No return value
    """
    if enableAssistance.upper() == 'TRUE':
        AdminTask.modifySIBusMemberPolicy(["-bus", SIBusName, "-cluster", clusterName, "-enableAssistance", enableAssistance, "-policyName", policyName])
    else:
        AdminTask.modifySIBusMemberPolicy(["-bus", SIBusName, "-cluster", clusterName, "-enableAssistance", enableAssistance])


def createSIBJMSConnectionFactory(clusterName, serverName, jmsCFName, jmsCFJNDI, jmsCFDesc, jmsCFType, SIBusName, provEndPoints, scope, authAlias=""):
    """ This method encapsulates the actions needed to create a JMS Connection Factory for handling connections between communicators and queues.

    Parameters:
        clusterName - Name of the cluster to associate connection factory with in String format. If value is "none", server will be used instead of cluster
        serverName - Name of server to associate connection factory with in String format.
        jmsCFName - Name to use for connection factory in String format.
        jmsCFJNDI - JNDI Identifier to use for connection factory in String format.
        jmsCFDesc - Description of the connection factory in String format.
        jmsCFType - Type of the connection factory in String format
        SIBusName - Name of bus to associate connection factory with in String format.
        provEndPoints - Provider of connection factory in String format
        scope - Identification of object (such as server or node) in String format.
        authAlias - Authentication alias for connection factory in String format
    Returns:
        No return value
    """
    m = "createSIBJMSConnectionFactory: "
    #--------------------------------------------------------------------
    # Create SIB JMS connection factory
    #--------------------------------------------------------------------
    if(clusterName == "none"):
        jmsCF = AdminConfig.getid('/Server:%s/J2CResourceAdapter:SIB JMS Resource Adapter/J2CConnectionFactory:%s' % (serverName,jmsCFName))
    else:
        jmsCF = AdminConfig.getid('/ServerCluster:%s/J2CResourceAdapter:SIB JMS Resource Adapter/J2CConnectionFactory:%s' % (clusterName,jmsCFName))
    #endElse
    if (len(jmsCF) != 0):
        sop(m, "The %s JMS connection factory already exists." % jmsCFName)
        return
    #endIf

    #--------------------------------------------------------------------
    # Create the SIB JMS connection factory
    #--------------------------------------------------------------------
    params = ["-name", jmsCFName, "-jndiName", jmsCFJNDI, "-busName", SIBusName, "-description", jmsCFDesc]
    if(not(jmsCFType == "")):
        params.append("-type")
        params.append(jmsCFType)
    #endIf
    if(not(provEndPoints == "")):
        params.append("-providerEndPoints")
        params.append(provEndPoints)
    #endIf
    if(not(authAlias == "")):
        params.append("-containerAuthAlias")
        params.append(authAlias)
    #endIf

    AdminTask.createSIBJMSConnectionFactory(scope, params)
#endDef

def createSIBJMSQueue(jmsQName, jmsQJNDI, jmsQDesc, SIBQName, scope):
    """ This method encapsulates the actions needed to create a JMS Queue for messages.

    Parameters:
        jmsQName - Name to use for queue in String format.
        jmsQJNDI - JNDI Identifier to use for queue in String format.
        jmsQDesc - Description of the queue in String format.
        SIBQName - Queue Name value used in protocol in String format
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "createSIBJMSQueue: "
    #--------------------------------------------------------------------
    # Create SIB JMS queue
    #--------------------------------------------------------------------
    for queue in _splitlines(AdminTask.listSIBJMSQueues(scope)):
        name = AdminConfig.showAttribute(queue, "name")
        if (name == jmsQName):
            sop(m, "The %s SIB JMS queue already exists." % jmsQName)
            return
        #endIf
    #endFor

    params = ["-name", jmsQName, "-jndiName", jmsQJNDI, "-description", jmsQDesc, "-queueName", SIBQName]
    AdminTask.createSIBJMSQueue(scope, params)
#endDef

def createSIBQueue(clusterName, nodeName, serverName, SIBQName, SIBusName):
    """ This method encapsulates the actions needed to create a queue for the Service Integration Bus.

    Parameters:
        clusterName - Name of the cluster to associate queue with in String format. If value is "none", server-node will be used instead of cluster
        nodeName - Name of node containing server to associate queue with in String format.
        serverName - Name of server to associate queue with in String format.
        SIBQName - Name of queue to create in String format
        SIBusName - Name of bus to associate queue with in String format.
    Returns:
        No return value
    """
    m = "createSIBQueue: "
    #--------------------------------------------------------------------
    # Create SIB queue
    #--------------------------------------------------------------------
    for queue in _splitlines(AdminConfig.list("SIBQueue")):
        identifier = AdminConfig.showAttribute(queue, "identifier")
        if (identifier == SIBQName):
            sop(m, "The %s SIB queue already exists." % SIBQName)
            return
        #endIf
    #endFor

    #--------------------------------------------------------------------
    # Create SIB queue
    #--------------------------------------------------------------------
    if(clusterName == "none"):
        params = ["-bus", SIBusName, "-name", SIBQName, "-type", "Queue", "-node", nodeName, "-server", serverName]
    else:
        params = ["-bus", SIBusName, "-name", SIBQName, "-type", "Queue", "-cluster", clusterName]
    #endElse
    AdminTask.createSIBDestination(params)
#endDef

def createSIBJMSTopic(jmsTName, jmsTJNDI, jmsTDesc, SIBTName, SIBTopicSpace, scope):
    """ This method encapsulates the actions needed to create a JMS Topic.

    Parameters:
        jmsTName - Name of topic in String format
        jmsTJNDI - JNDI Identifier of topic in String format
        jmsTDesc - Description of topic in String format
        SIBTName - Topic name value used in SIB in String format
        SIBTopicSpace - Topic space value used in SIB in String format
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "createSIBJMSTopic: "
    #--------------------------------------------------------------------
    # Create SIB JMS topic
    #--------------------------------------------------------------------
    for topic in _splitlines(AdminTask.listSIBJMSTopics(scope)):
        name = AdminConfig.showAttribute(topic, "name")
        if (name == jmsTName):
            sop(m, "The %s SIB JMS topic already exists." % jmsTName)
            return
        #endIf
    #endFor

    params = ["-name", jmsTName, "-jndiName", jmsTJNDI, "-description", jmsTDesc, "-topicName", SIBTName, "-topicSpace", SIBTopicSpace]
    AdminTask.createSIBJMSTopic(scope, params)
#endDef

def createSIBTopic(clusterName, nodeName, serverName, SIBTName, SIBusName):
    """ This method encapsulates the actions needed to create a topic for the Service Integration Bus.

    Parameters:
        clusterName - Name of the cluster to associate topic with in String format. If value is "none", server-node will be used instead of cluster
        nodeName - Name of node containing server to associate topic with in String format.
        serverName - Name of server to associate topic with in String format.
        SIBQName - Name of topic to create in String format
        SIBusName - Name of bus to associate topic with in String format.
    Returns:
        No return value
    """
    m = "createSIBTopic: "
    #--------------------------------------------------------------------
    # Create SIB topic
    #--------------------------------------------------------------------
    for topic in _splitlines(AdminConfig.list("SIBTopicSpace")):
        identifier = AdminConfig.showAttribute(topic, "identifier")
        if (identifier == SIBTName):
            sop(m, "The %s SIB topic already exists." % SIBTName)
            return
        #endIf
    #endFor

    #--------------------------------------------------------------------
    # Create SIB topic
    #--------------------------------------------------------------------
    if(clusterName == "none"):
        params = ["-bus", SIBusName, "-name", SIBTName, "-type", "TopicSpace", "-node", nodeName, "-server", serverName]
    else:
        params = ["-bus", SIBusName, "-name", SIBTName, "-type", "TopicSpace", "-cluster", clusterName]
    #endElse
    AdminTask.createSIBDestination(params)
#endDef

def createSIBJMSActivationSpec(activationSpecName, activationSpecJNDI, jmsQJNDI, destinationType, messageSelector, authAlias, SIBusName, scope):
    """ This method encapsulates the actions needed to create a JMS Activation Specification.

    Parameters:
        activationSpecName - Name of activation spec in String format
        actiovationSpecJNDI - JNDI Identifier of activation spec in String format
        jmsQJNDI - JNDI Identifier of the JMS queue to associate spec with in String format
        destinationType - Type of destination end point in String format
        messageSelector - Identifier of the message selector in String format
        authAlias - Authentication alias for activation spec in String format
        SIBusName - Name of bus to connect activation spec to in String format
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "createSIBJMSActivationSpec: "

    for spec in _splitlines(AdminTask.listSIBJMSActivationSpecs(scope)):
        name = AdminConfig.showAttribute(spec, "name")
        if (name == activationSpecName):
            sop(m, "The %s SIB JMS activation spec already exists." % activationSpecName)
            return
        #endIf
    #endFor

    #--------------------------------------------------------------------
    # Create SIB JMS activation spec
    #--------------------------------------------------------------------
    params = ["-name", activationSpecName, "-jndiName", activationSpecJNDI, "-busName", SIBusName, "-destinationJndiName", jmsQJNDI, "-destinationType", destinationType]
    if(not(authAlias == "")):
        params.append("-authenticationAlias")
        params.append(authAlias)
    #endIf
    if(not(messageSelector == "")):
        params.append("-messageSelector")
        params.append(messageSelector)
    #endIf
    AdminTask.createSIBJMSActivationSpec(scope, params)
#endDef

def deleteSIBJMSConnectionFactory(jmsCFName, clusterName, serverName):
    """ This method encapsulates the actions needed to delete a SIB JMS Connection Factory.

    Parameters:
        jmsCFName - Name of connection factory in String format.
        clusterName - Name of the cluster to associate queue with in String format. If value is "none", server will be used instead of cluster.
        serverName - Name of server to associate queue with in String format.
    Returns:
        No return value
    """
    m = "deleteSIBJMSConnectionFactory: "
    #--------------------------------------------------------------------
    # Retrieve specific Object ID and remove Connection Factory using ID
    #--------------------------------------------------------------------
    if(clusterName == "none"):
        jmsCF = AdminConfig.getid('/Server:%s/J2CResourceAdapter:SIB JMS Resource Adapter/J2CConnectionFactory:%s' % (serverName,jmsCFName))
    else:
        jmsCF = AdminConfig.getid('/Cluster:%s/J2CResourceAdapter:SIB JMS Resource Adapter/J2CConnectionFactory:%s' % (clusterName,jmsCFName))
    #endElse
    if(not(jmsCF == "")):
        AdminConfig.remove(jmsCF)
        sop(m, "Deleted connection factory %s" % jmsCFName)
    else:
        sop(m, "ConnectionFactory %s not found" % jmsCFName)
    #endElse
#endDef

def deleteSIBJMSActivationSpec(jmsASName, clusterName, serverName):
    """ This method encapsulates the actions needed to delete a SIB JMS Activation Specification.

    Parameters:
        jmsASName - Name of activation spec in String format.
        clusterName - Name of the cluster to associate queue with in String format. If value is "none", server will be used instead of cluster.
        serverName - Name of server to associate queue with in String format.
    Returns:
        No return value
    """
    m = "deleteSIBJMSActivationSpec: "
    #--------------------------------------------------------------------
    # Retrieve specific Resource Adapter Type ID for SIB JMS Resource Adapter
    #--------------------------------------------------------------------
    if(clusterName == "none"):
        ra = AdminConfig.getid('/Server:%s/J2CResourceAdapter:SIB JMS Resource Adapter' % serverName)
    else:
        ra = AdminConfig.getid('/Cluster:%s/J2CResourceAdapter:SIB JMS Resource Adapter' % clusterName)
    #endElse

    #--------------------------------------------------------------------
    # Remove the Activation Spec found in the SIB JMS Resource Adapter
    #--------------------------------------------------------------------
    for spec in _splitlines(AdminTask.listJ2CActivationSpecs(ra, ["-messageListenerType", "javax.jms.MessageListener"])):
        name = AdminConfig.showAttribute(spec, "name")
        if (name == jmsASName):
            AdminConfig.remove(spec)
            sop(m, "Deleted ActivationSpec %s" % jmsASName)
            return
        #endIf
    #endFor

    sop(m, "ActivationSpec %s not found" % jmsASName)
#endDef

def deleteSIBJMSQueue(qName, scope):
    """ This method encapsulates the actions needed to delete a SIB JMS Queue.

    Parameters:
        qName - Name of JMS queue in String format.
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "deleteSIBJMSQueue: "
    #--------------------------------------------------------------------
    # Search for queue based on scope and delete
    #--------------------------------------------------------------------
    for queue in _splitlines(AdminTask.listSIBJMSQueues(scope)):
        name = AdminConfig.showAttribute(queue, "name")
        if (name == qName):
            AdminTask.deleteSIBJMSQueue(queue)
            sop(m, "Deleted jms queue %s" % qName)
            return
        #endIf
    #endFor
#endDef

def deleteSIBJMSTopic(tName, scope):
    """ This method encapsulates the actions needed to delete a SIB JMS Topic.

    Parameters:
        tName - Name of JMS queue in String format.
        scope - Identification of object (such as server or node) in String format.
    Returns:
        No return value
    """
    m = "deleteSIBJMSTopic: "
    #--------------------------------------------------------------------
    # Search for topic based on scope and delete
    #--------------------------------------------------------------------
    for topic in _splitlines(AdminTask.listSIBJMSTopics(scope)):
        name = AdminConfig.showAttribute(topic, "name")
        if (name == tName):
            AdminTask.deleteSIBJMSTopic(topic)
            sop(m, "Deleted jms topic %s" % tName)
            return
        #endIf
    #endFor
#endDef

def deleteSIBQueue(qName, SIBusName):
    """ This method encapsulates the actions needed to delete a SIB Queue.

    Parameters:
        qName - Name of SIB queue in String format.
        SIBusName - Name of the bus the queue is associated with in String format.
    Returns:
        No return value
    """
    m = "deleteSIBQueue: "
    #--------------------------------------------------------------------
    # Search for queue based on scope and delete
    #--------------------------------------------------------------------
    params = ["-bus", SIBusName, "-name", qName]
    scope = ["-bus", SIBusName, "-type", "Queue"]

    if(not(re.compile(SIBusName, 0).search(AdminTask.listSIBuses())==None)):
        for q in _splitlines(AdminTask.listSIBDestinations(scope)):
            name = AdminConfig.showAttribute(q, "identifier")
            if (name == qName):
                AdminTask.deleteSIBDestination(params)
                sop(m, "Deleted destination %s" % qName)
                return
            #endIf
        #endFor
    #endIf
#endDef

def deleteBus(SIBusName):
    """ This method encapsulates the actions needed to delete a Service Integration Bus.

    Parameters:
        SIBusName - Name of the bus to delete in String format
    Returns:
        No return value
    """
    m = "deleteBus: "
    #--------------------------------------------------------------------
    # Search for bus using the provided bus name
    #--------------------------------------------------------------------
    for bus in _splitlines(AdminTask.listSIBuses()):
        name = AdminConfig.showAttribute(bus, "name")
        if (name == SIBusName):
            params = ["-bus", SIBusName]
            AdminTask.deleteSIBus(params)
            sop(m, "deleted SIBus %s" % SIBusName)
            return
        #endIf
    #endFor
#endDef

###############################################################################
# JCA Resource Management

def createCF(nodeName, raName, cfName, jndiName, cfiIndex):
    """ This method encapsulates the actions needed to create a J2C connection factory.

    Parameters:
        nodeName - Name of the node in String format
        raName - Name of resource adapter to associate connection factory with in String format
        cfName - Name of the connection factory to be created in String format
        jndiName - JNDI Identifier of the connection factory in String format
        cfiIndex - Index of the connection definition to use for connection factory creation in Integer format
    Returns:
        No return value
    """
    #--------------------------------------------------------------
    # set up locals
    #--------------------------------------------------------------
    cell = AdminControl.getCell()
    ra = AdminConfig.getid('/Cell:%s/Node:%s/J2CResourceAdapter:%s/' % (cell,nodeName,raName))
    cfi = (_splitlines(AdminConfig.list("ConnectionDefinition", ra)))[int(cfiIndex)]

    #---------------------------------------------------------
    # Create a J2CConnectionFactory
    #---------------------------------------------------------
    name_attr = ["name", cfName]
    jndi_attr = ["jndiName", jndiName]
    conndef_attr = ["connectionDefinition", cfi]
    attrs = [name_attr, jndi_attr, conndef_attr]
    AdminConfig.create("J2CConnectionFactory", ra, attrs)
#endDef

def deleteCF(nodeName, raName, cfName):
    """ This method encapsulates the actions needed to remove a J2C connection factory.

    Parameters:
        nodeName - Name of the node in String format
        raName - Name of resource adapter to associate connection factory with in String format
        cfName - Name of the connection factory to be created in String format
    Returns:
        No return value
    """
    #--------------------------------------------------------------
    # set up locals
    #--------------------------------------------------------------
    cell = AdminControl.getCell()
    ra = AdminConfig.getid('/Cell:%s/Node:%s/J2CResourceAdapter:%s/' % (cell,nodeName,raName))

    #---------------------------------------------------------
    # Get the ID of J2CConnectionFactory using the provided name
    #---------------------------------------------------------
    for cfItem in _splitlines(AdminConfig.list("J2CConnectionFactory", ra)):
        if (cfName == AdminConfig.showAttribute(cfItem, "name")):
            AdminConfig.remove(cfItem)
            break
        #endIf
    #endFor
#endDef

def customizeCF(nodeName, raName, cfName, propName, propValue):
    """ This method encapsulates the actions needed to modify a J2C connection factory.

    Parameters:
        nodeName - Name of the node in String format
        raName - Name of resource adapter to associate connection factory with in String format
        cfName - Name of the connection factory to be created in String format
        propName - Name of the connection factory property to be set in String format
        propValue - Value of the connection factory property to be set in String format
    Returns:
        No return value
    """
    #--------------------------------------------------------------
    # set up locals
    #--------------------------------------------------------------
    cell = AdminControl.getCell()
    ra = AdminConfig.getid('/Cell:%s/Node:%s/J2CResourceAdapter:%s/' % (cell,nodeName,raName))

    #---------------------------------------------------------
    # Get the ID of J2CConnectionFactory using the provided name
    #---------------------------------------------------------
    for cfItem in _splitlines(AdminConfig.list("J2CConnectionFactory", ra)):
        if (cfName == AdminConfig.showAttribute(cfItem, "name")):
            cf = cfItem
            break
        #endIf
    #endFor

    #---------------------------------------------------------
    # Customize the J2CConnectionFactory
    #---------------------------------------------------------
    propset = AdminConfig.list("J2EEResourcePropertySet", cf )
    for psItem in _splitlines(AdminConfig.list("J2EEResourceProperty", propset)):
        if (propName == AdminConfig.showAttribute(psItem, "name")):
            AdminConfig.modify(psItem, [["value", propValue]])
            break
        #endIf
    #endFor
#endDef

def installRA(nodeName, rarPath, raName):
    """ This method encapsulates the actions needed to install a resource adapter.

    Parameters:
        nodeName - Name of the node in String format
        rarPath - File system path of the configuration file of the resource adapter in String format (Using '/' for file separator)
        raName - Name of resource adapter to install in String format
    Returns:
        No return value
    """
    #---------------------------------------------------------
    # Install a J2CResourceAdapter using the provided rar file
    #---------------------------------------------------------
    option = ["-rar.name", raName]
    AdminConfig.installResourceAdapter(rarPath, nodeName, option)
#endDef

def uninstallRA(nodeName, raName):
    """ This method encapsulates the actions needed to remove an installed resource adapter.

    Parameters:
        nodeName - Name of the node in String format
        raName - Name of resource adapter to remove in String format
    Returns:
        No return value
    """

    #---------------------------------------------------------
    # Uninstall a J2CResourceAdapter using the provided name
    #---------------------------------------------------------
    cell = AdminControl.getCell()
    ra = AdminConfig.getid('/Cell:%s/Node:%s/J2CResourceAdapter:%s/' % (cell,nodeName,raName))
    AdminConfig.remove(ra)
#endDef

def createJAAS(auth_alias, auth_username, auth_password):
    """ This method encapsulates the actions needed to create a J2C authentication data entry.

    Parameters:
        auth_alias - Alias to identify authentication entry in String format
        auth_username - Name of user in authentication entry in String format
        auth_password - User password in authentication entry in String format
    Returns:
        JAAS object
    """
    #---------------------------------------------------------
    # Check if the alias already exists
    #---------------------------------------------------------
    auth = _splitlines(AdminConfig.list("JAASAuthData"))

    for autItem in auth:
        if (auth_alias == AdminConfig.showAttribute(autItem, "alias")):
            sop("createJAAS", "The %s Resource Environment Provider already exists." % auth_alias)
            return autItem  # return the object
        #endIf
    #endFor
    #---------------------------------------------------------
    # Get the config id for the Cell's Security object
    #---------------------------------------------------------
    cell = AdminControl.getCell()
    sec = AdminConfig.getid('/Cell:%s/Security:/' % cell)

    #---------------------------------------------------------
    # Create a JAASAuthData object for authentication
    #---------------------------------------------------------
    alias_attr = ["alias", auth_alias]
    desc_attr = ["description", "authentication information"]
    userid_attr = ["userId", auth_username]
    password_attr = ["password", auth_password]
    attrs = [alias_attr, desc_attr, userid_attr, password_attr]
    appauthdata = AdminConfig.create("JAASAuthData", sec, attrs)
    return appauthdata # return the object

    #--------------------------------------------------------------
    # Save all the changes
    #--------------------------------------------------------------
    # AdminConfig.save()   # Joey commented out
#endDef

def getJAAS(auth_alias):
    """ This method encapsulates the actions needed to retrieve a J2C authentication data entry.

    Parameters:
        auth_alias - Alias to identify authentication entry in String format
    Returns:
        j2c object
    """
    #---------------------------------------------------------
    # Get JAASAuthDat object
    #---------------------------------------------------------
    auth = _splitlines(AdminConfig.list("JAASAuthData"))

    for autItem in auth:
        if (auth_alias == AdminConfig.showAttribute(autItem, "alias")):
            return autItem
            break
        #endIf
    #endFor
#endDef

def deleteJAAS(auth_alias):
    """ This method encapsulates the actions needed to remove a J2C authentication data entry.

    Parameters:
        auth_alias - Alias to identify authentication entry in String format
    Returns:
        No return value
    """
    #---------------------------------------------------------
    # Get JAASAuthDat object
    #---------------------------------------------------------
    auth = _splitlines(AdminConfig.list("JAASAuthData"))

    for autItem in auth:
        if (auth_alias == AdminConfig.showAttribute(autItem, "alias")):
            AdminConfig.remove(autItem)
            break
        #endIf
    #endFor
#endDef

###############################################################################
# Security

def configureAppSecurity(enable):
    """ This method enables or disables application security for the server

    Parameters:
        enable - "true" or "false" to activate or deactivate application security, respectively
    Returns:
        No return value
    """
    security = AdminConfig.list('Security')
    AdminConfig.modify(security,[['appEnabled', enable]])
    AdminConfig.save()
#endDef

def configureAdminSecurity(enable):
    """ This method enables or disables administrative security for the server

    Parameters:
        enable - "true" or "false" to activate or deactivate administrative security, respectively
    Returns:
        No return value
    """
    security = AdminConfig.list('Security')
    AdminConfig.modify(security,[['enabled', enable]])
    AdminConfig.save()
#endDef

def runSecurityWizard(enableAppSecurity, enableJavaSecurity, userRegistryType, userName, password):
    """ This method applies the specified Security Wizard settings to the workspace

    Parameters:
        enableAppSecurity - "true" or "false" to activate or deactivate application security, respectively
        enableJavaSecurity - "true" of "false" to activate or deactivate Java 2 security, respectively
        userRegistryType - Type of user registry in String format (Restriced to: "LDAPUserRegistry", "CustomUserRegistry", "WIMUserRegistry", "LocalOSUserRegistry")
        userName - Administrative user name in String format
        password - Administrative password in String format
    Returns:
        No return value
    """
    param = '[-secureApps %s -secureLocalResources %s -userRegistryType %s -adminName %s -adminPassword %s]' % (enableAppSecurity,enableJavaSecurity,userRegistryType,userName,password)
    AdminTask.applyWizardSettings(param)
    AdminConfig.save()
#endDef

def extractCert(keyStoreName, cellName, nodeName, certPath, certAlias):
    """ This method extracts security certificate to specified path

    Parameters:
        keyStoreName - Name of the key store in String format
        cellName - Cell associated with specific key store in String format
        nodeName - Node in specified Cell associated with specific key store in String format
        certPath - File system path of certificate file in String format
        certAlias - Identification name of the certificate in String format
    Returns:
        0 if extraction completed successfully, 1 if extraction failed
    """
    try:
        AdminTask.extractCertificate('[-keyStoreName %s -keyStoreScope (cell):%s:(node):%s -certificateFilePath %s -certificateAlias %s]' % (keyStoreName,cellName,nodeName,certPath,certAlias))
        return 0
    except:
        print 'Error exporting cert ', keyStoreName, '. Exception information ', sys.exc_type, sys.exc_value
        return 1
#endDef

def importCert(keyStoreName, cellName, nodeName, certPath, certAlias):
    """ This method imports certificate information from specified path

    Parameters:
        keyStoreName - Name of the key store in String format
        cellName - Cell associated with specific key store in String format
        nodeName - Node in specified Cell associated with specific key store in String format
        certPath - File system path of certificate file in String format
        certAlias - Identification name of the certificate in String format
    Returns:
        0 if extraction completed successfully, 1 if extraction failed
    """
    try:
        AdminTask.addSignerCertificate('[-keyStoreName %s -keyStoreScope (cell):%s:(node):%s -certificateFilePath %s -certificateAlias %s]' % (keyStoreName,cellName,nodeName,certPath,certAlias))
        AdminConfig.save()
        return 0
    except:
        print 'Error importing cert ', keyStoreName, '. Exception information ', sys.exc_type, sys.exc_value
        return 1
#endDef

###############################################################################
# Custom Security

def setCustomSecurityWebContainer():
    security = AdminConfig.list("Security" )
    reg = AdminConfig.showAttribute(security, "userRegistries" )
    registries = reg[1:len(reg)-1].split(" ")
    serverId = ["serverId", "wcfvtAdmin"]
    serverPassword = ["serverPassword", "cvxa"]
    userReg = ["useRegistryServerId", "true"]
    classname = ["customRegistryClassName", "WCFvtRegistry"]
    attrs = [serverId, serverPassword, classname, userReg]
    for registry in registries:
            if (( registry.find("CustomUserRegistry") != -1 ) ):
                    AdminConfig.modify(registry, attrs )
            #endIf
    #endFor
    for registry in registries:
        if (( registry.find("CustomUserRegistry") != -1 ) ):
                    customRegistry = registry
            #endIf
    #endFor
    AdminConfig.create("Property", customRegistry, [["name", "usersFile"], ["value", "${WAS_INSTALL_ROOT}/users.props"]] )
    AdminConfig.create("Property", customRegistry, [["name", "groupsFile"], ["value", "${WAS_INSTALL_ROOT}/groups.props"]] )
    regStr = ["activeUserRegistry",customRegistry]
    attrs = [["enabled", "true"], ["enforceJava2Security", "true"], ["appEnabled", "true"], regStr]
    AdminConfig.modify(security, attrs )

def setCustomSecurityRoles(app_name,mapuser):
    i = 0
    while (i<len(app_name)):
        AdminApp.edit(app_name[i], '[ -MapRolesToUsers [[ '+mapuser+' AppDeploymentOption.No AppDeploymentOption.No '+mapuser+' "" AppDeploymentOption.No user:customRealm/123 "" ]]]' )
        i = i + 1

def setCustomSecurityRolesandMap(app_name,mapuser1,mapuser2,appdeploy1="No",appdeploy2="No",appdeploy3="No"):
    # appdeploy1, appdeploy2 and appdeploy3 has a value of No or Yes
    # this maps Map Special Subjects: All authenticated in application realm, Everyone and None (see infocenter for more details)
    # example setCustomSecurityRoles("SampleApp","User1","No","No","No")
    AdminApp.edit(app_name, '[ -MapRolesToUsers [[ '+mapuser1+' AppDeploymentOption.'+appdeploy1+' AppDeploymentOption.'+appdeploy2+' '+mapuser2+' "" AppDeploymentOption.'+appdeploy3+' user:customRealm/123 "" ]]]' )

def setSecurityProperty ( propName, propValue ):
        global AdminConfig
        print "Setting Security property"

        print "Locating security configuration IDs ..."
        security = AdminConfig.list("Security" )
        print "Locating the configuration IDs of all the properties in that security system ..."

        props = AdminConfig.showAttribute(security, "properties" )
        properties = props[1:len(props)-1].split(" ")

        print "Locating the property named "+propName+" ..."
        for property in properties:
                name = AdminConfig.showAttribute(property, "name" )
                if (propName == name):
                        prop = property
                        print "Found it!"
                #endIf
        #endFor

        if (prop == ""):
                print "No property named "+`propName`+" could be found!  Nothing to do!"
        else:
                print "Setting the value of the "+`propName`+" property to "+`propValue`+" ..."
                value = ["value", propValue]
                attrs = [value]
                AdminConfig.modify(prop, attrs )
                print "Done!"
        #endElse
#endDef

##########################################
# Web Services Feature Pack

def createPolicySetAttachment(applicationName, psName, resource, attachmentType):
    """ This method creates the attachments to a particular policy set

    Parameters:
        applicationName - Name of the application the policy set applies to in String format
        psName - Name of the policy set to create in String format
        resource - Name of application resources or trust resources in String format.
        attachmentType - Type of attachment in String format (Restricted to: "application", "client", "system/trust")
    Returns:
        No return value
    """
    arg = '[-applicationName %s -attachmentType %s -policySet %s -resources %s]' % (applicationName,attachmentType,psName,resource)
    AdminTask.createPolicySetAttachment(arg)
    refreshPolicyConfiguration()
#endDef

def deletePolicySetAttachment(applicationName, attachmentType):
    """ This method deletes the attachments to a particular policy set

    Parameters:
        applicationName - Name of the application the policy set applies to in String format
        attachmentType - Type of attachment in String format (Restricted to: "application", "client", "system/trust")
    Returns:
        No return value
    """
    psAttachmentString = getPolicySetAttachmentsNoResource(applicationName, attachmentType)
    psDict = propsToDictionary(psAttachmentString)
    attachmentID = psDict['id']

    arg = '[-applicationName %s -attachmentId  %s]' % (applicationName,attachmentID)
    AdminTask.deletePolicySetAttachment(arg)
#endDef

def getPolicySetAttachments(applicationName, attachmentType, expandResources):
    """ This method retrieves the attachments to a particular policy set

    Parameters:
        applicationName - Name of the application the policy set applies to in String format
        attachmentType - Type of attachment in String format (Restricted to: "application", "client", "system/trust")
        expandResources - Parameter for further details on attachment properties in String format. If '' is used, then the expandResources parameter will be ignored.
    Returns:
        Policy set attachments in a single String. splitlines() method can be used to separate into array.
    """
    if(expandResources == ''):
        arg =  '[-applicationName %s -attachmentType %s]' % (applicationName,attachmentType)
    else:
        arg = '[-applicationName %s -attachmentType %s -expandResources %s]' % (applicationName,attachmentType,expandResources)
    #endElse
    results = _splitlines(AdminTask.getPolicySetAttachments(arg))
    return results
#endDef

def configureHTTPTransport(user, password):
    """ This method configures the HTTP Transport attributes

    Parameters:
        user - User ID for the HTTP request in String format
        password - Password for the HTTP request in String format
    Returns:
        No return value
    """
    arg = '[-policyType HTTPTransport -bindingLocation -attributes "[ [outRequestBasicAuth:userid %s] ' + '[outRequestBasicAuth:password %s] ]" -attachmentType application]' % (user,password)
    AdminTask.setBinding(arg)
    AdminConfig.save()
    refreshPolicyConfiguration()
#endDef

def configureCallerBinding(applicationName):
    """ This method configures the binding for using Web Services security

    Parameters:
        applicationName - Name of application to attach binding to
    Returns:
        No return value
    """
    psAttachmentString = getPolicySetAttachments(applicationName, 'application', '')
    psDict = propsToDictionary(psAttachmentString)
    attachmentID = psDict['id']
    arg = '[-policyType WSSecurity -bindingLocation "[ [application %s] [attachmentId %s] ]" -attributes "[ [application.securityinboundbindingconfig.caller_0.calleridentity.uri http://www.ibm.com/websphere/appserver/tokentype/5.0.2] [application.securityinboundbindingconfig.caller_0.calleridentity.localname LTPA] [application.name application] [application.securityinboundbindingconfig.caller_0.jaasconfig.configname system.wss.caller] [application.securityinboundbindingconfig.caller_0.name LTPA] ]" -attachmentType application]' % (applicationName,attachmentID)
    AdminTask.setBinding(arg)
    AdminConfig.save()
    refreshPolicyConfiguration()
#endDef

def refreshPolicyConfiguration():
    """ Refreshes configuration for policy managers.

    Parameters:
        No parameters required
    Returns:
        No return value
    """
    policyMgrs = AdminControl.queryNames("type=PolicySetManager,*")
    policyMgrList = _splitlines(policyMgrs)
    for policyMgr in policyMgrList:
        AdminControl.invoke(policyMgr, "refresh")
    #endFor
#endDef

###############################################################################
# Custom Advisor methods

def createCustomAdvisorPolicy(proxysettings,options):
    """ create custom advisor policy"""
    return AdminConfig.create("CustomAdvisorPolicy", proxysettings, options)

def createCustomAdvisor(policyname,blaname,blaedition,cuname,cuedition,iotimeout):
    return AdminConfig.create('CustomAdvisor', policyname, [["blaID","WebSphere:blaname="+blaname+",blaedition="+blaedition],["cuID","WebSphere:cuname="+cuname+",cuedition="+cuedition],["ioTimeout",iotimeout]])

def createGenericServerClusterMapping(customadvisorname, clustername):
    return AdminConfig.create("GenericServerClusterMapping",customadvisorname, [["clusterName", clustername]])

def createGenericServerClusterMember(gscmapping,hostname,port):
    return AdminConfig.create("GSCMember", gscmapping, [["hostName",hostname], ["port", port]])

def createCustomAdvisorProperty(customadvisorname, propertyname,propertyvalue):
    return AdminConfig.create("Property",customadvisorname,[["name",propertyname],["value", propertyvalue]])

def createApplicationServerClusterMapping(customadvisorname, clustername,cellname, applicationname):
    return AdminConfig.create("ApplicationServerClusterMapping",customadvisorname, [["clusterName", clustername],["cellName", cellname],["applicationName", applicationname]])

def createApplicationServerClusterMember(appsrvclustermapping, nodename,servername):
    return AdminConfig.create("ApplicationServerClusterMember",appsrvclustermapping, [["nodeName",nodename],["serverName", servername]])

def createStandaloneApplicationServerMapping(customadvisorname,nodename,servername,cellname,applicationname):
    return AdminConfig.create("StandAloneApplicationServerMapping",customadvisorname, [["nodeName",nodename],["serverName", servername],["cellName", cellname],["applicationName", applicationname]])

###############################################################################
# Namespace bindings

def createStringNameSpaceBinding(scope, bindingName, nameInNameSpace, stringToBind):
    """Configuring String namespace binding

    scope           - objectId is the configuration ID of a cell, a node, or a server object(because we are using AdminConfig.list)
    bindingName     - Specifies a name that uniquely identifies this configured binding.
    nameInNameSpace - Specifies a name for this binding in the name space. It is a simple or compound name relative to the portion of the name space where this binding is configured.
    stringToBind    - Specifies the string to be bound into the name space.
    """

    for ns in AdminConfig.list('StringNameSpaceBinding', scope).splitlines() :
        if nameInNameSpace == AdminConfig.showAttribute( ns, 'nameInNameSpace' ) and bindingName != AdminConfig.showAttribute( ns, 'name' ) :
            raise "ERROR: A binding with nameInNamespace %s is already configured in this scope for bindingName %s." % (AdminConfig.showAttribute( ns, 'nameInNameSpace' ), AdminConfig.showAttribute( ns, 'name' ))

    for ns in AdminConfig.list('StringNameSpaceBinding', scope).splitlines() :
        if bindingName == AdminConfig.showAttribute( ns, 'name' ):
            print "The namespace binding %s already exist with nameInNameSpace %s and stringToBind %s." % (AdminConfig.showAttribute( ns, 'name' ), AdminConfig.showAttribute( ns, 'nameInNameSpace' ), AdminConfig.showAttribute( ns, 'stringToBind' ))
            print "Overwriting with new values: nameInNameSpace %s, stringToBind %s." % (nameInNameSpace, stringToBind)
            AdminConfig.modify(ns, [['nameInNameSpace', nameInNameSpace], ['stringToBind', stringToBind]])
            return

    print "Creating new namespace binding."
    return AdminConfig.create("StringNameSpaceBinding", scope, [["name", bindingName], ["nameInNameSpace", nameInNameSpace], ["stringToBind", stringToBind]])

###############################################################################
# Wrapper definitions for AdminConfig (logs activity automatically)

def modify( object, attrs ):
    """Modifies an object; Similar to setObjectAttributes(objectid, **settings)"""
    sop("modify:", 'AdminConfig.modify(%s, %s)' % ( repr(object), repr(attrs) ) )
    AdminConfig.modify(object, attrs)

def remove( object ):
    """Removes the specified object"""
    sop("remove:", 'AdminConfig.remove(%s)' % ( repr(object) ) )
    AdminConfig.remove(object)

def create( type, parent, attrs, parentAttrName=None ):
    """Creates an object of a specific type under the parent with the given attributes."""
    return _create(type, parent, attrs, None, parentAttrName, None)
def removeAndCreate( type, parent, attrs, objectKeyNames, parentAttrName=None ):
    """Creates an object of a specific type under the parent with the given attributes.  If such an object already exists, and if that object has existing attributes matching attributes named by objectKeyNames, the existing object is first removed."""
    return _create(type, parent, attrs, objectKeyNames, parentAttrName, None)
def createUsingTemplate( type, parent, attrs, template ):
    """Creates an object of a specific type under the parent with the given attributes using the supplied template."""
    return _create(type, parent, attrs, None, None, template)
def removeAndCreateUsingTemplate( type, parent, attrs, template, objectKeyNames ):
    """Creates an object of a specific type under the parent with the given attributes using the supplied template.  If such an object already exists, and if that object has existing attributes matching attributes named by objectKeyNames, the existing object is first removed."""
    return _create(type, parent, attrs, objectKeyNames, None, template)
def _create( type, parent, attrs, objectKeyNames, parentAttrName, template ):
    """Creates an object of a specific type under a specific parent with the given attrs and template.  parentAttrName defines the attribute name in the parent you want to create a type under. If an existing object has existing attributes matching attributes named by objectKeyNames, it will first be removed."""
    m = "_create:"
    if objectKeyNames:
        matchingAttrs = []
        for objectKeyName in objectKeyNames:
            for attr in attrs:
                if attr[0] == objectKeyName:
                    # assume that objectKeyNames are not specified more than once
                    matchingAttrs.append( [objectKeyName, attr[1]]  )
        if len(matchingAttrs)>0:
            findAndRemove(type, matchingAttrs, parent)
    if parentAttrName:
        sop(m, 'AdminConfig.create(%s, %s, %s, %s)' % (repr(type), repr(parent), repr(attrs), repr(parentAttrName)))
        object=AdminConfig.create(type, parent, attrs, parentAttrName)
    else:
        if template:
            sop(m, 'AdminConfig.create(%s, %s, %s, %s)' % (repr(type), repr(parent), repr(attrs), repr(template)))
            object=AdminConfig.createUsingTemplate(type, parent, attrs, template)
        else:
            sop(m, 'AdminConfig.create(%s, %s, %s)' % (repr(type), repr(parent), repr(attrs)))
            object=AdminConfig.create(type, parent, attrs)
    return object

def findAndRemove( type, attrs, parent=None ):
    """Removes all the objects of a specific type under a specific parent that have attributes matching attrs"""
    children = getFilteredTypeList(type, attrs, parent)
    for child in children:
        remove(child)

def getFilteredTypeList( type, attrs, parent=None ):
    """Produces a list of all the objects of a specific type under a specific parent that have attributes matching attrs"""
    myList = getObjectsOfType(type, parent)
    return filterTypeList(myList, attrs)

def filterTypeList( list, attrs ):
    """Filters the input list for items with an attribute names matching the input attribute list"""
    newlist = []
    for item in list:
        # assume that the item is wanted
        addItem = 1
        for attr in attrs:
            value = getObjectAttribute( item, attr[0] )
            if value != attr[1]:
                # if any attribute of an item is not wanted, then the item is not wanted
                addItem = 0
        if addItem:
            newlist.append( item )
    return newlist

###############################################################################
# JDBC

def createJdbcProvider ( parent, name, classpath, nativepath, implementationClassName, description, providerType=None ):
    """Creates a JDBCProvider in the specified parent scope; removes existing objects with the same name"""
    attrs = []
    attrs.append( [ 'name', name ] )
    attrs.append( [ 'classpath', classpath ] )
    attrs.append( [ 'nativepath', nativepath ] )
    attrs.append( [ 'implementationClassName', implementationClassName ] )
    attrs.append( [ 'description', description ] )
    if providerType:
        attrs.append( [ 'providerType', providerType ] )
    return removeAndCreate('JDBCProvider', parent, attrs, ['name'])

def removeJdbcProvidersByName ( providerName ):
    """Removes all the JDBCProvider objects with the specified name.  Implicitly deletes underlying DataSource objects."""
    findAndRemove('JDBCProvider', [['name', providerName]])

def createDataSource ( jdbcProvider, datasourceName, datasourceDescription, datasourceJNDIName, statementCacheSize, authAliasName, datasourceHelperClassname ):
    """Creates a DataSource under the given JDBCProvider; removes existing objects with the same jndiName"""
    mapping = []
    mapping.append( [ 'authDataAlias', authAliasName ] )
    mapping.append( [ 'mappingConfigAlias', 'DefaultPrincipalMapping' ] )
    attrs = []
    attrs.append( [ 'name', datasourceName ] )
    attrs.append( [ 'description', datasourceDescription ] )
    attrs.append( [ 'jndiName', datasourceJNDIName ] )
    attrs.append( [ 'statementCacheSize', statementCacheSize ] )
    attrs.append( [ 'authDataAlias', authAliasName ] )
    attrs.append( [ 'datasourceHelperClassname', datasourceHelperClassname ] )
    attrs.append( [ 'mapping', mapping ] )
    datasourceID = removeAndCreate( 'DataSource', jdbcProvider, attrs, ['jndiName'])
    create('J2EEResourcePropertySet', datasourceID, [], 'propertySet')
    return datasourceID

def createDataSource_ext ( scope, clusterName, nodeName, serverName_scope, jdbcProvider, datasourceName, datasourceDescription, datasourceJNDIName, statementCacheSize, authAliasName, datasourceHelperClassname, dbType, nonTransDS='', cmpDatasource='true', xaRecoveryAuthAlias=None, databaseName=None, serverName=None, portNumber=None, driverType=None, URL=None, informixLockModeWait=None, ifxIFXHOST=None ):
    """Creates a DataSource under the given JDBCProvider; removes existing objects with the same jndiName"""

    m = "createDataSource_ext"
    sop (m, "Entering createDataSource_ext...")
    mapping = []
    mapping.append( [ 'authDataAlias', authAliasName ] )
    mapping.append( [ 'mappingConfigAlias', 'DefaultPrincipalMapping' ] )
    attrs = []
    attrs.append( [ 'name', datasourceName ] )
    attrs.append( [ 'description', datasourceDescription ] )
    attrs.append( [ 'jndiName', datasourceJNDIName ] )
    attrs.append( [ 'statementCacheSize', statementCacheSize ] )
    attrs.append( [ 'authDataAlias', authAliasName ] )
    attrs.append( [ 'datasourceHelperClassname', datasourceHelperClassname ] )
    attrs.append( [ 'mapping', mapping ] )

    jdbcProviderType = getObjectAttribute (jdbcProvider, 'providerType')
    sop (m, "jdbcProviderType = %s" % jdbcProviderType)
    if jdbcProviderType:
        sop (m, "looking for 'XA' in providerType")
        if jdbcProviderType.find("XA") >= 0 and xaRecoveryAuthAlias:
            sop (m, "found 'XA' in providerType")
            attrs.append(['xaRecoveryAuthAlias', xaRecoveryAuthAlias])
        #endIf
    #endIf
    sop (m, "calling removeAndCreate to create datasource")
    datasourceID = removeAndCreate( 'DataSource', jdbcProvider, attrs, ['jndiName'])
    create('J2EEResourcePropertySet', datasourceID, [], 'propertySet')

    # Create properties for the datasource based on the specified database type

    sop (m, "Create properties for the datasource based on the specified database type")
    dsProps = []

    retcode = 0
    if dbType == 'DB2':
        if (databaseName == None or serverName == None or portNumber == None or driverType == None):
            sop (m, "All required properties for a DB2 datasource (databaseName, serverName, portNumber, driverType) were not specified.")
            retcode = 2
        else:
            dsProps.append( [ 'databaseName', 'java.lang.String',  databaseName ] )
            dsProps.append( [ 'serverName',   'java.lang.String',  serverName   ] )
            dsProps.append( [ 'portNumber',   'java.lang.Integer', portNumber   ] )
            dsProps.append( [ 'driverType',   'java.lang.Integer', driverType   ] )
    elif dbType == 'SQLServer-DD':
        if serverName == None:
            sop (m, "All required properties for a SQL Server (Data Direct) datasource (serverName) were not specified.")
            retcode = 3
        else:
            dsProps.append( [ 'serverName',   'java.lang.String',  serverName   ] )
            if databaseName:
                dsProps.append( [ 'databaseName', 'java.lang.String',  databaseName ] )
            if portNumber:
                dsProps.append( [ 'portNumber',   'java.lang.Integer', portNumber   ] )
    elif dbType == 'SQLServer-MS':
        if databaseName:
            dsProps.append( [ 'databaseName', 'java.lang.String',  databaseName ] )
        if serverName:
            dsProps.append( [ 'serverName',   'java.lang.String',  serverName   ] )
        if portNumber:
            dsProps.append( [ 'portNumber',   'java.lang.Integer', portNumber   ] )
    elif dbType == 'Oracle':
        if URL == None:
            sop (m, "All required properties for an Oracle datasource (URL) were not specified.")
            retcode = 4
        else:
            dsProps.append( [ 'URL', 'java.lang.String', URL ] )
    elif dbType == 'Sybase2':
        if (databaseName == None or serverName == None or portNumber == None):
            sop (m, "All required properties for a Sybase JDBC-2 datasource (databaseName, serverName, portNumber, driverType) were not specified.")
            retcode = 5
        else:
            dsProps.append( [ 'databaseName', 'java.lang.String',  databaseName ] )
            dsProps.append( [ 'serverName',   'java.lang.String',  serverName   ] )
            dsProps.append( [ 'portNumber',   'java.lang.Integer', portNumber   ] )
    elif dbType == 'Sybase3':
        if (databaseName == None or serverName == None or portNumber == None):
            sop (m, "All required properties for a Sybase JDBC-3 datasource (databaseName, serverName, portNumber, driverType) were not specified.")
            retcode = 6
        else:
            dsProps.append( [ 'databaseName', 'java.lang.String',  databaseName ] )
            dsProps.append( [ 'serverName',   'java.lang.String',  serverName   ] )
            dsProps.append( [ 'portNumber',   'java.lang.Integer', portNumber   ] )
    elif dbType == 'Informix':
        if (databaseName == None or serverName == None or informixLockModeWait == None):
            sop (m, "All required properties for an Informix datasource (databaseName, serverName, informixLockModeWait) were not specified.")
            retcode = 7
        else:
            dsProps.append( [ 'databaseName',         'java.lang.String',   databaseName         ] )
            dsProps.append( [ 'serverName',           'java.lang.String',   serverName           ] )
            dsProps.append( [ 'informixLockModeWait', 'java.lang.Integer',  informixLockModeWait ] )
            if portNumber:
                dsProps.append( [ 'portNumber', 'java.lang.Integer', portNumber ] )
            if ifxIFXHOST:
                dsProps.append( [ 'ifxIFXHOST', 'java.lang.String',  ifxIFXHOST ] )
    else:  # Invalid dbType specified
        sop (m, "Invalid dbType '%s' specified" % dbType)
        retcode = 8
    # end else

    if retcode == 0:
        if (nonTransDS != ""):
            dsProps.append( [ 'nonTransactionalDataSource', 'java.lang.Boolean', nonTransDS ] )
        #endif

        for prop in dsProps:
            propName  = prop[0]
            propType  = prop[1]
            propValue = prop[2]
            propDesc  = ""
            sop (m, "calling setJ2eeResourceProperty")
            setJ2eeResourceProperty (  \
                                        datasourceID,
                                        propName,
                                        propType,
                                        propValue,
                                        propDesc,
                                    )
            sop (m, "returned from calling setJ2eeResourceProperty")
        # endfor

        # Create CMP Connection Factory if this datasource will support Container Managed Persistence

        sop (m, "checking if cmpDatasource == 'true'")
        if cmpDatasource == 'true':
            sop(m, "calling createCMPConnectorFactory")
            createCMPConnectorFactory ( scope, clusterName, nodeName, serverName_scope, datasourceName, authAliasName, datasourceID )
            sop(m, "returned from calling createCMPConnectorFactory")
        #endIf

        return datasourceID
    else:
        return None
    #endif


def configureDSConnectionPool (scope, clustername, nodename, servername, jdbcProviderName, datasourceName, connectionTimeout, minConnections, maxConnections, additionalParmsList=[]):
    """ This function configures the Connection Pool for the specified datasource for
        the specified JDBC Provider.

        Input parameters:

        scope - the scope of the datasource.  Valid values are 'cell', 'node', 'cluster', and 'server'.
        clustername - name of the cluster for the datasource.  Required if scope = 'cluster'.
        nodename - the name of the node for the datasource.  Required if scope = 'node' or 'server'.
        servername - the name of the server for the datasource.  Required if scope = 'server'.
        jdbcProviderName - the name of the JDBC Provider for the datasource
        datasourceName - the name of the datasource whose connection pool is to be configured.
        connectionTimeout - Specifies the interval, in seconds, after which a connection request times out.
                            Valid range is 0 to the maximum allowed integer.
        minConnections - Specifies the minimum number of physical connections to maintain.  Valid
                         range is 0 to the maximum allowed integer.
        maxConnections - Specifies the maximum number of physical connections that you can create in this
                         pool.  Valid range is 0 to the maximum allowed integer.
        additionalParmsList - A list of name-value pairs for other Connection Pool parameters.  Each
                              name-value pair should be specified as a list, so this parameter is
                              actually a list of lists.  The following additional parameters can be
                              specified:
                              - 'reapTime' - Specifies the interval, in seconds, between runs of the
                                             pool maintenance thread.  Valid range is 0 to the maximum
                                             allowed integer.
                              - 'unusedTimeout' - Specifies the interval in seconds after which an unused
                                                  or idle connection is discarded.  Valid range is 0 to
                                                  the maximum allowed integer.
                              - 'agedTimeout' - Specifies the interval in seconds before a physical
                                                connection is discarded.  Valid range is 0 to the maximum
                                                allowed integer.
                              - 'purgePolicy' - Specifies how to purge connections when a stale
                                                connection or fatal connection error is detected.
                                                Valid values are EntirePool and FailingConnectionOnly.
                              - 'numberOfSharedPoolPartitions' - Specifies the number of partitions that
                                                                 are created in each of the shared pools.
                                                                 Valid range is 0 to the maximum allowed
                                                                 integer.
                              - 'numberOfFreePoolPartitions' - Specifies the number of partitions that
                                                               are created in each of the free pools.
                                                               Valid range is 0 to the maximum allowed
                                                               integer.
                              - 'freePoolDistributionTableSize' - Determines the distribution of Subject
                                                                  and CRI hash values in the table that
                                                                  indexes connection usage data.
                                                                  Valid range is 0 to the maximum allowed
                                                                  integer.
                              - 'surgeThreshold' - Specifies the number of connections created before
                                                   surge protection is activated.  Valid range is -1 to
                                                   the maximum allowed integer.
                              - 'surgeCreationInterval' - Specifies the amount of time between connection
                                                          creates when you are in surge protection mode.
                                                          Valid range is 0 to the maximum allowed integer.
                              - 'stuckTimerTime' - This is how often, in seconds, the connection pool
                                                   checks for stuck connections.  Valid range is 0 to the
                                                   maximum allowed integer.
                              - 'stuckTime' - The stuck time property is the interval, in seconds, allowed
                                              for a single active connection to be in use to the backend
                                              resource before it is considered to be stuck.  Valid range
                                              is 0 to the maximum allowed integer.
                              - 'stuckThreshold' - The stuck threshold is the number of connections that
                                                   need to be considered stuck for the pool to be in stuck
                                                   mode.  Valid range is 0 to the maximum allowed integer.

        Here is an example of how the 'additionalParmsList" argument could be built by the caller:

        additionalParmsList = []
        additionalParmsList.append( [ 'unusedTimeout', '600' ] )
        additionalParmsList.append( [ 'agedTimeout', '600' ] )

        Outputs - No return values.  If an error is detected, an exception will be thrown.
    """

    m = "configureDSConnectionPool:"
    sop (m, "Entering function...")

    if scope == 'cell':
        sop (m, "Calling getCellName()")
        cellname = getCellName()
        sop (m, "Returned from getCellName(); cellname = %s." % cellname)
        dsStringRep = '/Cell:%s/JDBCProvider:%s/DataSource:%s/' % (cellname, jdbcProviderName, datasourceName)
    elif scope == 'cluster':
        dsStringRep = '/ServerCluster:%s/JDBCProvider:%s/DataSource:%s/' % (clustername, jdbcProviderName, datasourceName)
    elif scope == 'node':
        dsStringRep = '/Node:%s/JDBCProvider:%s/DataSource:%s/' % (nodename, jdbcProviderName, datasourceName)
    elif scope == 'server':
        dsStringRep = '/Node:%s/Server:%s/JDBCProvider:%s/DataSource:%s/' % (nodename, servername, jdbcProviderName, datasourceName)
    else:
        raise 'Invalid scope specified: %s' % scope
    #endif

    sop (m, "Calling AdminConfig.getid with the following name: %s." % dsStringRep)
    dsID = AdminConfig.getid(dsStringRep)
    sop (m, "Returned from AdminConfig.getid; returned dsID = %s" % dsID)

    if dsID == '':
        raise 'Could not get config ID for name = %s' % dsStringRep
    else:
        sop (m, "Calling AdminConfig.showAttribute to get the connectionPool config ID")
        cpID = AdminConfig.showAttribute(dsID,'connectionPool')
        sop (m, "Returned from AdminConfig.showAttribute; returned cpID = %s" % cpID)
        if cpID == '':
            raise 'Could not get connectionPool config ID'
        else:
            attrs = []
            attrs.append( [ 'connectionTimeout', connectionTimeout ] )
            attrs.append( [ 'minConnections', minConnections ] )
            attrs.append( [ 'maxConnections', maxConnections ] )

            if additionalParmsList != []:
                attrs = attrs + additionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (cpID, attrs)
            sop (m, "Returned from AdminConfig.modify")
        #endif
    #endif

    sop (m, "Exiting function...")
#endDef


def setJ2eeResourceProperty ( parent, propName, propType, propValue, propDescription ):
    """Sets a J2EEResourceProperty on the object specified by parent; parent must have a PropertySet child attribute named 'propertySet'"""
    attrs = []
    attrs.append( [ 'name', propName ] )
    attrs.append( [ 'type', propType ] )
    attrs.append( [ 'value', propValue ] )
    attrs.append( [ 'description', propDescription ] )
    propSet = getObjectAttribute(parent, 'propertySet')
    removeAndCreate('J2EEResourceProperty', propSet, attrs, ['name'])

def testDataSourcesByJndiName ( jndiName ):
    """Tests DataSource connectivity for all DataSource objects with a matching JNDI name.  If any AdminControl.testConnection fails, an exception is raised."""
    m = "testDataSourcesByJndiName:"
    dataSources = getFilteredTypeList('DataSource', [['jndiName', jndiName]])
    for dataSource in dataSources:
        sop(m, 'AdminControl.testConnection(%s)' % ( repr(dataSource) ) )
        try:
            sop(m, "  "+AdminControl.testConnection(dataSource) )
        except:
            # sometimes the error message is cryptic, so it's good to explicitly state the basic cause of the problem
            sop(m, "  Unable to establish a connection with DataSource %s" % (dataSource)  )
            raise Exception("Unable to establish a connection with DataSource %s" % (dataSource))

def createCMPConnectorFactory ( scope, clusterName, nodeName, serverName, dataSourceName, authAliasName, datasourceId ):
    """Creates a CMP Connection Factory at the scope identified by the scopeId.  This CMP Connection Factory corresponds to the specified datasource."""

    m = "createCMPConnectorFactory"

    sop (m, "entering createCMPConnectorFactory")
    cmpCFName = dataSourceName+"_CF"
    cmpCFAuthMech = "BASIC_PASSWORD"
    rraName = "WebSphere Relational Resource Adapter"

    # Check if the connection factory already exists
    objType = "J2CResourceAdapter:"+rraName+"/CMPConnectorFactory"
    sop (m, "calling getCfgItemId with scope=%s, clusterName=%s, nodeName=%s, serverName=%s, objType=%s, cmpCFName=%s" % (scope, clusterName, nodeName, serverName, objType, cmpCFName))
    cfgId = getCfgItemId(scope, clusterName, nodeName, serverName, objType, cmpCFName)
    sop (m, "returned from calling getCfgItemId: returned value = '%s'" % cfgId)
    if (cfgId != ""):
        sop(m,""+cmpCFName+" already exists on "+scope+" "+scope)
        return
    else:
        sop(m, "calling getCfgItemId to get ID for %s" % rraName)
        rraId = getCfgItemId(scope, clusterName, nodeName, serverName, "J2CResourceAdapter", rraName)
        sop(m, "returned from calling getCfgItemId")
    #endIf

    # Create connection factory using default RRA

    attrs = []
    attrs.append ( ["name", cmpCFName] )
    attrs.append ( ["authMechanismPreference", cmpCFAuthMech] )
    attrs.append ( ["cmpDatasource", datasourceId] )

    sop(m, "calling AdminConfig.create to create CMPConnectorFactory")
    cf = AdminConfig.create("CMPConnectorFactory", rraId,  attrs)

    # Mapping Module

    attrs1 = []
    attrs1.append ( ["authDataAlias", authAliasName] )
    attrs1.append ( ["mappingConfigAlias", "DefaultPrincipalMapping"] )

    sop(m, "calling AdminConfig.create to create MappingModule")
    mappingModule = AdminConfig.create("MappingModule", cf, attrs1)

    return cf

###############################################################################
# Java Mail procedures

def createMailProvider ( scope, nodeName, serverName, clusterName, providerName, providerDesc):
    """Creates a mail provider resource in set scope
    Should return error 99 if creation fails, otherwise if provider already exists, no error
    Returns the provider object it just created or already found
    """

    global AdminConfig

    m = "createMailProvider:"
    sop(m, "Create Mail Provider "+providerName+", if it does not exist")

    # Check if mail provider already exists
    provider = getCfgItemId (scope, clusterName, nodeName, serverName, 'MailProvider', providerName)

    if (provider != ''):
        sop (m, "Provider "+providerName+" already exists!!")
        return provider
    #endif

    parentId = getScopeId (scope, serverName, nodeName, clusterName)

    attrs = []
    attrs.append(["name", providerName])
    attrs.append(["description", providerDesc])

    provider = AdminConfig.create("MailProvider", parentId, attrs )

    if provider == '':
        sop (m, "Caught Exception creating Mail Provider "+provider)
        return 99

    sop (m, "Created "+providerName+" successfully.") 
    return provider

#endDef


def createProtocolProvider( scope, nodeName, serverName, clusterName, provName, protocol, classPath, type, className ):
    """Creates a protocol provider resource for set scope
    Should return error 99 if creation fails, or if mail provider to create protocol is not found
    Returns protocol provider it created if successful
    """

    m = "createProtocolProvider:"
    sop (m, "Create Mail Protocol Provider "+protocol+", if it does not exist")

    mProvider = getCfgItemId (scope, clusterName, nodeName, serverName, 'MailProvider', provName)

    # Check if mail provider exists
    if (mProvider == ''):
        sop (m, "Provider "+mProvider+" doesn't exist!!")
        return 99
    #endif

    attrs = []
    attrs.append(["classpath", classPath])
    attrs.append(["type", type])
    attrs.append(["classname", className])
    attrs.append(["protocol", protocol])

    pProvider = AdminConfig.create('ProtocolProvider', mProvider, attrs)

    if pProvider == '':
        sop (m, "Caught Exception creating protocol provider "+provName)
        return 99

    sop (m, "Creation of protocol provider "+protocol+" was successful.")
    return pProvider

def createMailSession( scope, nodeName, serverName, clusterName, provName, name, jndiName, desc, category, mailTransHost, mailTransProto, mailTransUserId, mailTransPasswd, enableParse, mailFrom, mailStoreHost, mailStoreProto, mailStoreUserId, mailStorePasswd, enableDebug ):
    """ 
    
    This function creates a JavaMail MailSession under the specified Provider Name. 

        Input parameters:

        scope               - The scope of the MailProvider. Valid values are (in order of preecendence): 'cell', 'node', 'cluster' and 'server'.
                              Note: The scope of 'cell' is not valid for the createMailSession function. 
        nodeName            - The name of the node of the MailProvider. Required if scope = 'node' or 'server'.
        serverName          - The name of the server of the MailProvider. Required if scope = 'server'.
        clusterName         - The name of the cluster of the MailProvider. Required if scope = 'cluster'.
        provName            - The name of the MailProvider. Typical value: "Built-in Mail Provider".
        name                - The required display name of the MailSession to be created.
        jndiName            - The required JNDI of the MailSession to be created. 
        desc                - An optional description of this MailSession.
        category            - An optional category string to use when classifying or grouping the MailSession to be created. 
        mailTransHost       - Specifies the server to connect to when sending mail.
        mailTransProto      - Specifies the transport protocol to use when sending mail. Actual protocol values are defined in the protocol
                              providers that you configured for the current mail provider.
                              Typical Value: "builtin_smtp".
        mailTransUserId     - Specifies the user id to use when the mail transport host requires authentication.
        mailTransPasswd     - Specifies the password to use when the mail transport host requires authentication.
        enableParse         - Enable strict internet address parsing. Set to "true" to enforce the RFC 822 syntax rules for parsing Internet addresses when sending mail.
                              Valid values: "true" or "false". 
        mailFrom            - Specifies the Internet e-mail address that is displayed in messages as the mail originator. Typical value: "".
        mailStoreHost       - Specifies the mail account host, or domain name. Typical value: "".
        mailStoreProto      - Specifies the protocol to use when receiving mail. Actual protocol values are defined in the protocol providers
                              that you configured for the current mail provider.
                              Typical Value: "builtin_pop3" or "builtin_imap".
        mailStoreUserId     - Specifies the user ID of the mail account. Typical value: "".
        mailStorePasswd     - Specifies the password of the mail account. Typical value: "".
        enableDebug         - Enable debug information which shows interaction between the mail application and the mail servers,
                              as well as the properties of this mail session. to be sent to the SystemOut.log file. 
                              Valid values: "true" or "false". 

        Return Value:
            The newly created MailSession.  If an error occurs, an exception will be thrown.
    """

    m = "createMailSession:"
    sop(m, "Create Mail Session " + name + ", if it does not exist.")

    haveTransportProtocol = False
    haveStoreProtocol = False

    mailProvider = getCfgItemId(scope, clusterName, nodeName, serverName, 'MailProvider', provName)
    sop(m, "MailProvider = " + repr(mailProvider))

    # Check if mail provider exists
    if (mailProvider == ''):
        sop (m, "Provider doesn't exist!!")
        return None
    #endif

    protocolProviders = getObjectsOfType('ProtocolProvider', mailProvider)
    sop(m, "ProtocolProviders = " + repr(protocolProviders))

    # Iterate through the list (horrible way to do it, but...) looking for matching mail transport and store protocols.
    for protocolProvider in protocolProviders:
        sop(m, "Iterating through protocolProviders. protocolProvider = " + repr(protocolProvider))
        if -1 != protocolProvider.find(mailTransProto):
            sop(m, "Matched Transport Provider: " + repr(mailTransProto) + " with " + repr(protocolProvider))
            haveTransportProtocol = True
            mailTransportProtocol = protocolProvider
        if -1 != protocolProvider.find(mailStoreProto):
            sop(m, "Matched Store Provider: " + repr(mailStoreProto) + " with " + repr(protocolProvider))
            haveStoreProtocol = True
            mailStoreProtocol = protocolProvider

    # Now build the attribute list.
    attrs = []
    attrs.append(["name", name])
    attrs.append(["jndiName", jndiName])
    attrs.append(["description", desc])
    attrs.append(["category", category])
    attrs.append(["mailTransportHost", mailTransHost])
    if haveTransportProtocol:
        attrs.append(["mailTransportProtocol", mailTransportProtocol])
    attrs.append(["mailTransportUser", mailTransUserId])
    attrs.append(["mailTransportPassword", mailTransPasswd])
    attrs.append(["strict", enableParse])
    attrs.append(["mailFrom", mailFrom])
    attrs.append(["mailStoreHost", mailStoreHost])
    if haveStoreProtocol:
        attrs.append(["mailStoreProtocol", mailStoreProtocol])
    attrs.append(["mailStoreUser", mailStoreUserId])
    attrs.append(["mailStorePassword", mailStorePasswd])
    attrs.append(["debug", enableDebug])

    sop (m, "Creating mail session: " + repr(name) + " as a child of: " + repr(mailProvider) + " using attributes: " + repr(attrs)) 
    mSession = AdminConfig.create("MailSession", mailProvider, attrs)

    sop (m, "Creation of mail session " + name + " was successful! session = " + repr(mSession) )
    return mSession

#endDef
###############################################################################
# WebSphere Environment Variable Management

def getVariableMap ( nodeName=None, serverName=None, clusterName=None ):
    """Returns the VariableMap for the specified cell, node, server, or cluster"""
    target='(cells/'+getCellName()
    if clusterName:
        target=target+'/clusters/'+clusterName
    else:
        if nodeName:
            target=target+'/nodes/'+nodeName
        if serverName:
            target=target+'/servers/'+serverName
    target=target+'|variables.xml#VariableMap'
    maps=getObjectsOfType('VariableMap')
    for map in maps:
        if map.startswith(target):
            return map
    return None

def getWebSphereVariable ( name, nodeName=None, serverName=None, clusterName=None ):
    """Return the value of a variable for the specified scope -- or None if no such variable or not set"""
    map = getVariableMap(nodeName, serverName, clusterName)
    if map != None:  # Tolerate nodes with no such maps, for example, IHS nodes.
        entries = AdminConfig.showAttribute(map, 'entries')
        # this is a string '[(entry) (entry)]'
        entries = entries[1:-1].split(' ')
        for e in entries:
            symbolicName = AdminConfig.showAttribute(e,'symbolicName')
            value = AdminConfig.showAttribute(e,'value')
            if name == symbolicName:
                return value
    return None

def setWebSphereVariable ( name, value, nodeName=None, serverName=None, clusterName=None ):
    """Creates a VariableSubstitutionEntry at the specified scope, removing any previously existing entry in the process"""
    map = getVariableMap(nodeName, serverName, clusterName)
    attrs = []
    attrs.append( [ 'symbolicName', name ] )
    attrs.append( [ 'value', value ] )
    return removeAndCreate('VariableSubstitutionEntry', map, attrs, ['symbolicName'])

def removeWebSphereVariable ( name, nodeName=None, serverName=None, clusterName=None ):
    """Removes a VariableSubstitutionEntry at the specified scope"""
    map = getVariableMap(nodeName, serverName, clusterName)
    findAndRemove('VariableSubstitutionEntry', [['symbolicName', name]], map)

def expandWebSphereVariables ( variableString, nodeName=None, serverName=None, clusterName=None ):
    """ This function expands all WAS variable references 
        such as ${WAS_INSTALL_ROOT} in variableString with their
        values at the specified scope."""
    while variableString.find("${") != -1:
        startIndex = variableString.find("${")
        endIndex = variableString.find("}", startIndex)

        if endIndex == -1:
            raise 'end of variable not found'

        variableName = variableString[startIndex+2:endIndex]

        if variableName == '':
            raise 'variable name is empty'

        variableValue = getWebSphereVariable(variableName, nodeName, serverName, clusterName)

        if variableValue == None:
            raise 'variable ' + variableName + ' is not defined at the specified scope.'

        variableString = variableString.replace("${" + variableName + "}", variableValue)

    return variableString

###############################################################################
# Core Group Management (changes introduced by CGB FAT - to be added to wsadminlib.py/wsat project)

def removeCoreGroup ( name ):
    """Remove and existing Coregroup."""
    # create a new coregroup if the existing one is not found
    AdminTask.deleteCoreGroup('[-coreGroupName %s]' % name)

def createServerInCoreGroup( nodename, servername, coregroupname ):
    """Create new app server in a specified Core Group"""
    AdminTask.createApplicationServer( nodename, '[-name %s]' % servername)
    moveServerToCoreGroup( nodename, servername, coregroupname )


def createBridgeInterface(cgap_id, nodeName, serverName, chainName):
    """Creates a bridge interface in the specified coregroup, if no such bridge interface already exists"""
    result = None
    attrs = []
    attrs.append( [ 'node', nodeName ] )
    attrs.append( [ 'server', serverName ] )
    attrs.append( [ 'chain', chainName ] )
    matches = getFilteredTypeList('BridgeInterface', attrs, cgap_id)
    if len(matches)>0:
        result = matches[0]
    else:
        result = create('BridgeInterface', cgap_id, attrs, 'propertySet')
    return result


def setNumOfCoordinators( coregroupname, number ):
    """Sets the number of Coordinators in a Core Group"""
    m = "setNumOfCoordinators:"
    #sop(m,"setNumOfCoordinators: Entry. coregroupname =" + coregroupname + " node=" + node + " server=" + server + " chain=" + chain)

    cgid = getCoreGroupIdByName( coregroupname )
    AdminConfig.modify(cgid, [['numCoordinators',number]])


def moveAppServerToCoreGroup( nodename, servername, coregroupname ):
    """Adds the specified applicationserver to the specified coregroup."""
    m = "moveAppServerToCoreGroup:"
    #sop(m,"Entry. nodename=%s servername=%s coregroupname=%s" % ( nodename, servername, coregroupname ))

    # Find the coregroup which presently contains the server.
    oldcoregroup_id = findCoreGroupIdForServer(servername)
    #sop(m,"moveServerToCoreGroup: oldcoregroup_id=%s" % ( oldcoregroup_id ))

    # Extract the name of this coregroup from the id.
    oldcoregroup_name = getNameFromId(oldcoregroup_id)
    #sop(m,"oldcoregroup_name=%s" % ( oldcoregroup_name ))

    # Move the server.
    commandstring = '[-source %s -target %s -nodeName %s -serverName %s]' % ( oldcoregroup_name, coregroupname, nodename, servername )
    #sop(m,"commandstring=%s" % ( commandstring ))
    AdminTask.moveServerToCoreGroup( commandstring )

    sop(m,"Moved server %s to coregroup %s." % ( servername, coregroupname ))


def setCoreGroupCustomProperty( coregroupname, propertyName, propertyValue ):
    """Enables seamless failover on the specified coregroup (in this cell)."""
    coregroup_id = getCoreGroupIdByName(coregroupname)
    attrs = []
    attrs.append( [ 'name', propertyName ] )
    attrs.append( [ 'value', propertyValue ] )
    removeAndCreate('Property', coregroup_id, attrs, ['name'], 'customProperties')


def removeCoreGroupCustomProperty( coregroupname, propertyName ):
    """Disables seamless failover on the specified coregroup (in this cell)."""
    coregroup_id = getCoreGroupIdByName(coregroupname)
    findAndRemove('Property', [['name', propertyName]], coregroup_id)


def createCoreGroupNoAccessPoint( name ):
    """Create a new CoreGroup without creating an access point.
    Uses AdminConfig instead of AdminTask: behaves differently than ISC.
    If a CoreGroup with the same name already exists, it is removed before a new one is created."""
    attrs = []
    attrs.append( [ 'name', name ] )
    cellId = getCellId()
    removeAndCreate('CoreGroup', cellId, attrs, ['name'])

###############################################################################
# Remote Request Dispatcher methods

#--------------------------------------------------------------------
# PROCEDURE: configureRRD
#
#   Arguments:
#       appName:                        The name of the installed application you want to configure
#       allowDispatchRemoteInclude:     This option indicates that the web modules included
#                                         in the application are enabled to be RemoteRequestDispatcher
#                                         clients that can dispatch remote includes.
#       allowServiceRemoteInclude:      This option indicates that the web modules included
#                                         in the application are enabled to be RemoteRequestDispatcher
#                                         servers that can be resolved to service a remote include.
#
#   This procedure modifies the remote request dispatcher (RRD) attributes
#     of a specific application.  That application must be installed prior
#     to executing this procedure.
#
#   Possible values for the arguments are:
#     allowDispatchRemoteInclude        <boolean>
#     allowServiceRemoteInclude         <boolean>
#
#   Returns:
#       (nothing)
#
#--------------------------------------------------------------------
def configureRRD (appName, allowDispatchRemoteInclude, allowServiceRemoteInclude):
    deployments = AdminConfig.getid("/Deployment:%s/" % (appName) )
    deployedObject = AdminConfig.showAttribute(deployments, 'deployedObject')
    AdminConfig.modify(deployedObject, [['allowDispatchRemoteInclude', allowDispatchRemoteInclude], ['allowServiceRemoteInclude', allowServiceRemoteInclude]])

def createJ2EEResourceProperty(propSetID, propName, propType, propValue ):
    return create('J2EEResourceProperty', propSetID, [['name', propName],['type', propType],['value', propValue],['description','']])

def createDerbyDataSource( jdbcProvider, datasourceJNDIName, authAliasName, databaseName, datasourceName=None ):
    datasourceHelperClassname = "com.ibm.websphere.rsadapter.DerbyDataStoreHelper"
    datasourceDescription = "Simple Derby DataSource"
    if( datasourceName == None): # check the optional param
       datasourceName = "EventDS"
    statementCacheSize = "60"
    dataSourceId = createDataSource( jdbcProvider, datasourceName, datasourceDescription, datasourceJNDIName, statementCacheSize, authAliasName, datasourceHelperClassname )
    #We need the propSet from the above datasource
    propSet = AdminConfig.list('J2EEResourcePropertySet', dataSourceId)
    createJ2EEResourceProperty(propSet, 'databaseName', 'java.lang.String', databaseName)
    createJ2EEResourceProperty(propSet, 'createDatabase', 'java.lang.String', 'create')
    return dataSourceId # return the object

def configureARDTests(cellName, nodeName, serverName, jdbcProviderName, derbyDriverPath, datasourceJNDIName, dsAuthAliasName, dbuser, dbpassword, ardExecTO):
    cell_id = getCellId()
    #jdbcProviderID = createJdbcProvider(cellName, nodeName, jdbcProviderName, derbyDriverPath, "", "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource", "Derby embedded non-XA JDBC Provider.")
    jdbcProviderID = createJdbcProvider(cell_id, jdbcProviderName, derbyDriverPath, "", "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource", "Derby embedded non-XA JDBC Provider.")
    createJAAS(dsAuthAliasName, dbuser, dbpassword)
    createDerbyDataSource(jdbcProviderID, datasourceJNDIName, dsAuthAliasName)
    configureServerARD(cellName, nodeName, serverName, "true", ardExecTO)

###############################################################################
# Resource Environment Management

def createREProviders ( proName, description, scope ):
    """Creates a Resource Environment Provider in the specified parent scope"""
    m = "createREProviders:"

    for ob in _splitlines(AdminConfig.list('ResourceEnvironmentProvider',scope)):
        name = AdminConfig.showAttribute(ob, "name")
        if (name == proName):
            sop(m, "The %s Resource Environment Provider already exists." % proName)
            return

    attrs = []
    attrs.append( [ 'name', proName ] )
    attrs.append( [ 'description', description ] )
    return create('ResourceEnvironmentProvider', scope, attrs)

def createREProviderReferenceable ( factoryClassname, classname, proid ):
    """Creates a Resource Environment Provider Referenceable """
    m = "createREProviderReferenceable:"

    for ob in _splitlines(AdminConfig.list('Referenceable',proid)):
        name = AdminConfig.showAttribute(ob, "factoryClassname")
        if (name == factoryClassname):
            sop(m, "The %s Resource Environment Provider Referenceable already exists." % factoryClassname)
            return

    attrs = []
    attrs.append( [ 'factoryClassname', factoryClassname ] )
    attrs.append( [ 'classname', classname ] )
    return create('Referenceable', proid, attrs)

def createREProviderResourceEnvEntry ( entryName, jndiName, refid, proid ):
    """Creates a Resource Environment Provider ResourceEnvEntry """
    m = "createREProviderResourceEnvEntry:"

    for ob in _splitlines(AdminConfig.list('ResourceEnvEntry',proid)):
        name = AdminConfig.showAttribute(ob, "name")
        if (name == entryName):
            sop(m, "The %s Resource Environment Provider ResourceEnvEntry already exists." % entryName)
            return

    attrs = []
    attrs.append( [ 'name', entryName ] )
    attrs.append( [ 'jndiName', jndiName ] )
    attrs.append( [ 'referenceable', refid ] )
    return create('ResourceEnvEntry', proid, attrs)

def createREProviderProperties ( propName, propValue, proid ):
    """Creates a Resource Environment Provider Custom Property """
    m = "createREProviderProperties:"

    propSet = AdminConfig.showAttribute(proid, 'propertySet')
    if(propSet == None):
        propSet = create('J2EEResourcePropertySet',proid,[])

    for ob in _splitlines(AdminConfig.list('J2EEResourceProperty',proid)):
        name = AdminConfig.showAttribute(ob, "name")
        if (name == propName):
            sop(m, "The %s Resource Environment Provider Custom Property already exists." % propName)
            return

    attrs = []
    attrs.append( [ 'name', propName ] )
    attrs.append( [ 'value', propValue ] )
    return create('J2EEResourceProperty', propSet, attrs)

def rmminusrf(origpath):
    """Recursively deletes all files and directories under path"""
    #print "Entry. origpath=%s" % (origpath)
    if os.path.isfile(origpath):
        #print "Removing file. origpath=%s" % (origpath)
        os.remove(origpath)
    else:
        filelist = os.listdir(origpath)
        #print "filelist=%s" % (filelist)
        for filex in filelist:
            filepath = os.path.join(origpath,filex)
            #print "Handling filepath=%s" % (filepath)
            if os.path.isdir(filepath):
                #print "Going deeper %s" % (filepath)
                rmminusrf(filepath)
            else:
                #print "Removing file. filepath=%s" % (filepath)
                os.remove(filepath)
    #print "Exit"


def isMeStarted (busname, scope, nodename, servername, clustername, clusternum):
    """ This function returns a boolean to determine if a given Messaging Engine is started or not
        (1 == started, 0 == not started).  This method is useful for checking status of ME after starting your
        server.  It will return 0 (false) if the MBean is not found.

        Function parameters:

        busname - name of bus on which the ME resides.
        scope - scope of the ME, either 'server' or 'cluster'
        nodename - if 'server' scope, the name of the node on which the ME server member resides.
        servername - if 'server' scope, the name of the server member for the ME.
        clustername - if 'cluster' scope, the cluster name of the ME cluster member
        clusternum - if 'cluster' scope, the 3 digit number identifying which cluster ME member (ie 000, 001, 002 etc)

    """


    m = "isMeStarted"
    sop (m, "Entering %s function..." % m)

    sop (m, "Calling AdminConfig.getid to check if the bus %s exists" % busname)
    bus = AdminConfig.getid('/SIBus:%s' % busname)
    sop (m, "Returning from AdminConfig.getid, returned id is %s" % bus)

    if len(bus) == 0 :
        raise "Bus %s does not exist" % busname
    #endif

    if scope == 'server' :
        sop (m, "Calling getServerId to check if the server %s on node %s exists" % (nodename, servername))
        serverid = getServerId(nodename, servername)
        sop (m, "Returning from getServerId, returned id is %s" % serverid)

        if serverid == None :
            raise "Server %s does not exist on node %s" % (servername, nodename)
        #endif

        meName = nodename + '.' + servername + '-'+busname

        sop(m, "Beginning logic to determine if the ME %s exists" % meName)
        engineExists = 0
        for member in _splitlines(AdminTask.listSIBEngines(['-bus', busname])) :
            memberName = AdminConfig.showAttribute(member, 'name')
            if memberName == meName :
                sop(m, "The ME does exist")
                engineExists = 1
            #endfor
        #endfor
        sop(m, "Ending logic to determine if the ME %s exists" % meName)

        if engineExists == 0 :
            raise "The messaging engine %s does not exist" % meName
        #endif


        meLookupName = AdminControl.makeObjectName('WebSphere:type=SIBMessagingEngine,name='+ meName +',*')
    elif scope == 'cluster' :
        sop (m, "Calling getClusterId to check if the cluster %s exists" % clustername)
        clusterid = getClusterId(clustername)
        sop (m, "Returning from getClusterId, returned id is %s" % clusterid)

        if clusterid == None :
            raise "Cluster %s does not exist" % clustername
        #endif

        meName = clustername + '.' + clusternum + '-'+busname

        sop(m, "Beginning logic to determine if the ME %s exists" % meName)
        engineExists = 0
        for member in _splitlines(AdminTask.listSIBEngines(['-bus', busname])) :
            memberName = AdminConfig.showAttribute(member, 'name')
            if memberName == meName :
                sop(m, "The ME does exist")
                engineExists = 1
            #endfor
        #endfor
        sop(m, "Ending logic to determine if the ME %s exists" % meName)

        if engineExists == 0 :
            raise "The messaging engine %s does not exist" % meName
        #endif

        meLookupName = AdminControl.makeObjectName('WebSphere:type=SIBMessagingEngine,name=' + meName +',*')
    else:
        raise "Incorrect scope used, only options are server or cluster"
    #endif

    sop(m, "Querying the MBean names to get the MBean object %s" % meLookupName)
    meBeans = AdminControl.queryNames_jmx(meLookupName, None)
    sop(m, "Returning from MBean query")

    if (meBeans == None) or (meBeans.size() == 0) :
        sop (m, "The server is not starting/started and/or the MBean is not intialized yet")
        return 0
    #endif

    meBean=meBeans[0]
    return AdminControl.invoke_jmx(meBean, "isStarted", [],[])


#endDef

def isClusterStarted (clustername):
    """ This function returns a boolean to determine if a given cluster is started or not
        (1 == started, 0 == not started).  This method is useful for checking status of Cluster after starting your
        servers.

        Function parameters:

        clustername - the cluster name to check

    """


    m = "isClusterStarted"
    sop (m, "Entering %s function..." % m)

    sop(m, "Calling AdminControl.completeObjectName to get cluster %s's ObjectName" % clustername)
    cluster = AdminControl.completeObjectName('cell='+getCellName()+',type=Cluster,name='+clustername+',*')
    sop(m, "Returning from AdminControl.completeObjectName, ObjectName for %s is %s" % (clustername,cluster))

    if cluster == '' :
        raise "Exception getting ObjectName for cluster %s, it must not exist" % clustername
    #endif

    try:
        sop(m, "Calling AdminControl.getAttribute to get cluster %s's state" % clustername)
        output = AdminControl.getAttribute(cluster, 'state')
        sop(m, "Returning from AdminControl.getAttribute, the state of %s is %s" % (clustername, output))
    except:
        raise "Exception getting attribute for cluster %s's state" % clustername
    #endtry

    if output.find('running') != -1 :
        return 1
    else :
        return 0
    #endif

#endDef

def findNodesOnHostname(hostname):
    """Return the list of nodes name of a (non-dmgr) node on the given hostname, or None

       Function parameters:

        hostname - the hostname to check, with or without the domain suffix


    """


    m = "findNodesOnHostname:"
    nodes = []
    for nodename in listNodes():
        if hostname.lower() == getNodeHostname(nodename).lower():
            sop(m, "Found node %s which is on %s" % (nodename, hostname))
            nodes.append(nodename)
        #endif
    #endfor

    # Not found - try matching without domain - z/OS systems might not have domain configured
    shorthostname = hostname.split(".")[0].lower()
    for nodename in listNodes():
        shortnodehostname = getNodeHostname(nodename).split(".")[0].lower()
        if shortnodehostname == shorthostname:
            if nodename in nodes :
                sop(m, "Node name %s was already found with the domain attached" % nodename)
            else :
                nodes.append(nodename)
                sop(m, "Found node %s which is on %s" % (nodename, hostname))
            #endif
        #endif
    #endfor

    if len(nodes) == 0 :
        sop(m,"WARNING: Unable to find any node with the hostname %s (not case-sensitive)" % hostname)
        sop(m,"HERE are the hostnames that your nodes think they're on:")
        for nodename in listNodes():
            sop(m,"\tNode %s: hostname %s" % (nodename, getNodeHostname(nodename)))
        #endfor
        return None
    else :
         return nodes
     #endif
#enddef



###############################################################################
# HPEL Functions - WAS V8 only
###############################################################################

def isHPELEnabled (nodename, servername):
    """ This function determines whether or not the High Performance Extensible Logging feature is enabled.

        Function parameters:

          nodename - the name of the node on which the specified server resides.
          servername - the name of the server that should be checked to determine if the HPEL feature
                       is enabled.

        Return Value:

          If HPEL is enabled, "1" is returned.
          If HPEL is not enabled, "0" is returned.
    """

    m = "isHPELEnabled:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.showAttribute to get the 'enable' attribute.")
            enable = AdminConfig.showAttribute (HPELID, 'enable')
            sop (m, "Returned from AdminConfig.showAttribute; enable = %s" % enable)

            if enable == 'true':
                retvalue = 1
            else:
                retvalue = 0
            #endif
            sop (m, "Exiting function...")

            return retvalue
        #endif
    #endif
#endDef

def configureHPEL (nodename, servername, enable, additionalParmsList=[]):
    """ This function configures High Performance Extensible Logging for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose Application Profiling Service is to be configured.
        enable - specifies whether High Performance Extensible Logging is to be enabled or disabled.
                 Valid values are 'true' and 'false'.
        additionalParmsList - A list of name-value pairs for other HPEL Logging parameters.
                              Each name-value pair should be specified as a list, so this parameter
                              is actually a list of lists.  The following additional parameters can
                              be specified:

                              - 'correlationIdEnabled' - Valid values are 'true' and 'false'.
                              - 'stackTraceSuppressionEnabled' - Valid values are 'true' and 'false'.
                              - 'startupTraceSpec' - startup trace string.

        Here is an example of how the 'additionalParmsList" argument could be built by the caller:

        additionalParmsList = []
        additionalParmsList.append( [ 'correlationIdEnabled', 'true' ] )
        additionalParmsList.append( [ 'startupTraceSpec', '*=info' ] )

    """

    m = "configureHPEL:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            attrs = []
            attrs.append( [ 'enable', enable ] )

            if additionalParmsList != []:
                attrs = attrs + additionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (HPELID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef

def configureHPELBinaryLog (nodename, servername, additionalParmsList):
    """ This function configures the HPEL Binary Log for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Binary Log is to be configured.
        additionalParmsList - A list of name-value pairs for other HPEL Binary Log parameters.
                              Each name-value pair should be specified as a list, so this parameter
                              is actually a list of lists.  The following additional parameters can
                              be specified:

                              - 'dataDirectory' - Specifies the name of the directory where the HPEL logs
                                                  will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the logs
                                                 (in hours).

        Here is an example of how the 'additionalParmsList" argument could be built by the caller:

        additionalParmsList = []
        additionalParmsList.append( [ 'purgeBySizeEnabled', 'false' ] )
        additionalParmsList.append( [ 'purgeByTimeEnabled', 'true' ] )
        additionalParmsList.append( [ 'purgeMinTime', '24' ] )
    """

    m = "configureHPELBinaryLog:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Binary Log object.")
            HPELLogID = AdminConfig.list("HPELLog", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELLogID = %s" % HPELLogID)

            attrs = additionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (HPELLogID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef

def getHPELBinaryLogAttribute(nodename, servername, attributename):
    """ This function returns an attribute of the HPEL Binary Log for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Binary Log is to be configured.
        attributename - the following attribute names can be specified:
                              - 'dataDirectory' - Specifies the name of the directory where the HPEL logs
                                                  will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the logs
                                                 (in hours).
    """

    m = "getHPELBinaryLogAttribute:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Binary Log object.")
            HPELLogID = AdminConfig.list("HPELLog", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELLogID = %s" % HPELLogID)

            sop(m, "Calling AdminConfig.showAttribute to get the value of attribute = %s" % ( attributename ))
            attributevalue = AdminConfig.showAttribute(HPELLogID, attributename)
            sop (m, "Returned from AdminConfig.showAttribute; attributevalue = %s" % ( attributevalue ))

            sop (m, "Exiting function...")
            return attributevalue
        #endif
    #endif
#endDef

def configureHPELTextLog (nodename, servername, additionalParmsList):
    """ This function configures HPEL Text Logging for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Text Logging is to be configured.
        additionalParmsList - A list of name-value pairs for other HPEL Text Logging parameters.
                              Each name-value pair should be specified as a list, so this parameter
                              is actually a list of lists.  The following additional parameters can
                              be specified:

                              - 'enabled' - Specifies whether or not writing to the text log has been
                                            enabled.  Valid values are 'true' and 'false'.
                              - 'dataDirectory' - Specifies the name of the directory where the HPEL
                                                  text logs will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new text log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'minimumLevel' - Specifies the minimum level of messages that should be
                                                 included in the text log.  Valid values are 'INFO',
                                                 'FINEST', 'FINER', 'FINE', 'DETAIL', 'CONFIG', 'AUDIT',
                                                 'WARNING', 'SEVERE', and 'FATAL'.
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'outputFormat' - Specifies the output format to use in the text log.
                                                 Valid values are 'BASIC' and 'ADVANCED'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the text logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the text logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the text logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the text logs
                                                 (in hours).
                              - 'systemErrIncluded' - Specifies whether or not to include SystemErr information
                                                      in the text log.  Valid values are 'true' and 'false'.
                              - 'systemOutIncluded' - Specifies whether or not to include SystemOut information
                                                      in the text log.  Valid values are 'true' and 'false'.

        Here is an example of how the 'additionalParmsList" argument could be built by the caller:

        additionalParmsList = []
        additionalParmsList.append( [ 'purgeBySizeEnabled', 'false' ] )
        additionalParmsList.append( [ 'purgeByTimeEnabled', 'true' ] )
        additionalParmsList.append( [ 'purgeMinTime', '24' ] )
    """

    m = "configureHPELTextLog:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Text Log object.")
            HPELTextLogID = AdminConfig.list("HPELTextLog", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELTextLogID = %s" % HPELTextLogID)

            attrs = additionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (HPELTextLogID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef

def getHPELTextLogAttribute(nodename, servername, attributename):
    """ This function returns an attribute of the HPEL Text Log for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Text Logging is to be configured.
        attributename - the following attribute names can be specified:
                              - 'enabled' - Specifies whether or not writing to the text log has been
                                            enabled.  Valid values are 'true' and 'false'.
                              - 'dataDirectory' - Specifies the name of the directory where the HPEL
                                                  text logs will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new text log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'minimumLevel' - Specifies the minimum level of messages that should be
                                                 included in the text log.  Valid values are 'INFO',
                                                 'FINEST', 'FINER', 'FINE', 'DETAIL', 'CONFIG', 'AUDIT',
                                                 'WARNING', 'SEVERE', and 'FATAL'.
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'outputFormat' - Specifies the output format to use in the text log.
                                                 Valid values are 'BASIC' and 'ADVANCED'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the text logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the text logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the text logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the text logs
                                                 (in hours).
                              - 'systemErrIncluded' - Specifies whether or not to include SystemErr information
                                                      in the text log.  Valid values are 'true' and 'false'.
                              - 'systemOutIncluded' - Specifies whether or not to include SystemOut information
                                                      in the text log.  Valid values are 'true' and 'false'.
    """

    m = "getHPELTextLogAttribute:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Text Log object.")
            HPELTextLogID = AdminConfig.list("HPELTextLog", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELTextLogID = %s" % HPELTextLogID)

            sop(m, "Calling AdminConfig.showAttribute to get the value of attribute = %s" % ( attributename ))
            attributevalue = AdminConfig.showAttribute(HPELTextLogID, attributename)
            sop (m, "Returned from AdminConfig.showAttribute; attributevalue = %s" % ( attributevalue ))

            sop (m, "Exiting function...")
            return attributevalue
        #endif
    #endif
#endDef

def configureHPELTrace (nodename, servername, additionalParmsList):
    """ This function configures HPEL Trace for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Trace is to be configured.
        additionalParmsList - A list of name-value pairs for other HPEL Trace parameters.
                              Each name-value pair should be specified as a list, so this parameter
                              is actually a list of lists.  The following additional parameters can
                              be specified:

                              - 'dataDirectory' - Specifies the name of the directory where the HPEL logs
                                                  will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'memoryBufferSize' - Specifies the size (in MB) of the memory trace buffer.
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the logs
                                                 (in hours).
                              - 'storageType' - Specifies whether the trace log should be written to a
                                                directory or to memory.  Valid values are 'DIRECTORY'
                                                and 'MEMORYBUFFER'.

        Here is an example of how the 'additionalParmsList" argument could be built by the caller:

        additionalParmsList = []
        additionalParmsList.append( [ 'purgeBySizeEnabled', 'false' ] )
        additionalParmsList.append( [ 'purgeByTimeEnabled', 'true' ] )
        additionalParmsList.append( [ 'purgeMinTime', '24' ] )
    """

    m = "configureHPELTrace:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Trace object.")
            HPELTraceID = AdminConfig.list("HPELTrace", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELTraceID = %s" % HPELTraceID)

            attrs = additionalParmsList

            sop (m, "Calling AdminConfig.modify with the following parameters: %s" % attrs)
            AdminConfig.modify (HPELTraceID, attrs)
            sop (m, "Returned from AdminConfig.modify")
            sop (m, "Exiting function...")
        #endif
    #endif
#endDef

def getHPELTraceLogAttribute(nodename, servername, attributename):
    """ This function returns an attribute of the HPEL Trace Log for the specified server.

        Function parameters:

        nodename - the name of the node on which the server to be configured resides.
        servername - the name of the server whose HPEL Trace is to be configured.
        attributename - the following attribute names can be specified:
                              - 'dataDirectory' - Specifies the name of the directory where the HPEL logs
                                                  will be stored.
                              - 'bufferingEnabled' - Specifies whether or not log record buffering should
                                                     be enabled.  Valid values are 'true' and 'false'.
                              - 'fileSwitchEnabled' - Specifies whether or not a new log file should be
                                                      started each day.  Valid values are 'true' and 'false'.
                              - 'fileSwitchTime' - If 'fileSwitchEnabled' is set to 'true', this field
                                                   specifies the time that new log file should be started.
                                                   A value from 0 - 23 should be specified. A value of 0
                                                   means 12 AM 1 means 1 AM, 2 means 2 AM, ..., 23 means
                                                   11 PM.  If a value greater than 23 is entered, this
                                                   field will be set to 0 (12 AM).
                              - 'memoryBufferSize' - Specifies the size (in MB) of the memory trace buffer.
                              - 'outOfSpaceAction' - Specifies which action to take if the hard disk runs
                                                     out of space.  Valid values are 'StopLogging',
                                                     'StopServer', and 'PurgeOld'.
                              - 'purgeBySizeEnabled' - Specifies whether or not to purge the logs based
                                                       on size.  Valid values are 'true' and 'false'.
                              - 'purgeByTimeEnabled' - Specifies whether or not to purge the logs based
                                                       on time.  Valid values are 'true' and 'false'.
                              - 'purgeMaxSize' - Specifies the maximum total size of the logs (in MB).
                              - 'purgeMinTime' - Specifies the minimum amount of time to keep the logs
                                                 (in hours).
                              - 'storageType' - Specifies whether the trace log should be written to a
                                                directory or to memory.  Valid values are 'DIRECTORY'
                                                and 'MEMORYBUFFER'.
    """

    m = "getHPELTraceLogAttribute:"
    sop (m, "Entering function...")

    sop (m, "Calling getNodeId() with nodename = %s." % (nodename))
    nodeID = getNodeId(nodename)
    sop (m, "Returned from getNodeID; returned nodeID = %s" % nodeID)

    if nodeID == "":
        raise "Could not find node name '%s'" % (nodename)
    else:
        sop (m, "Calling getServerId() with nodename = %s and servername = %s." % (nodename, servername))
        serverID = getServerId(nodename, servername)
        sop (m, "Returned from getServerID; returned serverID = %s" % serverID)

        if serverID == None:
            raise "Could not find server '%s' on node '%s'" % (servername, nodename)
        else:
            serviceName = "HighPerformanceExtensibleLogging"

            sop (m, "Calling AdminConfig.list with serviceName = %s and serverID = %s." % (serviceName, serverID))
            HPELID = AdminConfig.list(serviceName, serverID)
            sop (m, "Returned from AdminConfig.list; HPELID = %s" % HPELID)

            sop (m, "Calling AdminConfig.list to get the config ID of the HPEL Trace object.")
            HPELTraceID = AdminConfig.list("HPELTrace", HPELID)
            sop (m, "Returned from AdminConfig.list; HPELTraceID = %s" % HPELTraceID)

            sop(m, "Calling AdminConfig.showAttribute to get the value of attribute = %s" % ( attributename ))
            attributevalue = AdminConfig.showAttribute(HPELTraceID, attributename)
            sop (m, "Returned from AdminConfig.showAttribute; attributevalue = %s" % ( attributevalue ))

            sop (m, "Exiting function...")
            return attributevalue
        #endif
    #endif
#endDef

def getServerNamedEndPoint(serverName, endPointName) :
    """
    Returns a list that contains hostname and port for a server specified

    Given the name of the server (String) and the end point (for example, PROXY_HTTP_ADDRESS),

    The returned list is like [hostname, port]
    """

    m = "getServerNamedEndPoint:"
    sop (m, "Attempt to get value of %s for server %s" %(endPointName, serverName))

    all_nodes = _splitlines(AdminConfig.list( "Node" ))

    for nodeEntry in all_nodes :
        nodeEntryName = AdminConfig.showAttribute(nodeEntry, "name")
        nodeHostName = AdminConfig.showAttribute(nodeEntry, "hostName")


        serversOnNode = _splitlines(AdminConfig.list( "ServerEntry", nodeEntry))
        for serverEntry in serversOnNode :
            serverEntryName = AdminConfig.showAttribute(serverEntry, "serverName")


            if serverEntryName == serverName :
                namedEndPoints = _splitlines(AdminConfig.list("NamedEndPoint", serverEntry))


                for eachPoint in namedEndPoints :
                    thisPtrName = AdminConfig.showAttribute(eachPoint, "endPointName")
                    thisPtr = AdminConfig.showAttribute(eachPoint, "endPoint")


                    if thisPtrName == endPointName :
                        thisPort = AdminConfig.showAttribute(thisPtr, "port")

                        return [nodeHostName, thisPort]

                    #end_if_thisPtrName
                #end_for_eachPoint
            #end_if_serverEntryName
        #end_for_serverEntry
    #end_for_nodeEntry

#end_def

###############################################################################
# Cookie Configuration
# sets session manager cookie options, application scoped only at this time

def setCookieConfig(scope, serverName, nodeName, appName, maximumAge, name, domain, path, secure, httpOnly) :

    m = "setCookieConfig:"
    if (scope == 'server'):
        sop(m, "please use the modifyCookies() function instead of setCookieConfig, example: modifyCookies(nodeName,serverName,'true',maximumAge)")
        # example: modifyCookies(nodeName,serverName,'true',maximumAge)
        return 99
    elif (scope == 'application'):
        setCookieConfigApplication(appName, maximumAge, name, domain, path, secure, httpOnly)
    else:
        sop(m, "no scope set " + scope)
        return 99

#end_def

def setCookieConfigApplication(appName, maximumAge, name, domain, path, secure, httpOnly) :
    """
    sets properties of cookie in a cookie and enables cookies in a session manager given an application name
    """
    m = "setCookieConfigApplication:"

    tuningParmsDetailList = [['invalidationTimeout', 45]]
    tuningParamsList = ['tuningParams', tuningParmsDetailList]
    cookie = [['maximumAge', maximumAge], ['name', name], ['domain', domain], ['path', path], ['secure', secure], ['httpOnly', httpOnly]]
    cookieSettings = ['defaultCookieSettings', cookie]
    sessionManagerDetailList = [['enable', 'true'], ['enableSecurityIntegration', 'true'], ['maxWaitTime', 30], ['sessionPersistenceMode', 'NONE'], ['enableCookies', 'true'], cookieSettings, tuningParamsList]
    sessionMgr = ['sessionManagement', sessionManagerDetailList]

    sop (m, "Attempt to get value of application %s" %(appName))

    deployments = AdminConfig.getid("/Deployment:"+appName+"/")

    if (deployments == ''):
        sop (m, "could not find application "+appName)
        return 99

    sop (m, deployments)
    appDeploy = AdminConfig.showAttribute(deployments, 'deployedObject')

    app_id = AdminConfig.create('ApplicationConfig', appDeploy, [sessionMgr], 'configs')
    if (app_id == ''):
        sop (m, "could not find application "+appName)
        return 99
    else:
       sop (m, "found "+app_id)

    targetMappings = AdminConfig.showAttribute(appDeploy, 'targetMappings')
    targetMappings = targetMappings[1:len(targetMappings)-1].split(" ")
    sop (m, targetMappings)

    for target in targetMappings:
      if target.find('DeploymentTargetMapping') != -1:
        attrs = ['config', app_id]
        sop (m, "Modifying the application's cookie settings")
        AdminConfig.modify(target,[attrs])
      #endif
    #endfor

#end_def


def removeAllDisabledSessionCookies() :
    """ new functionality in v8, there can be secure session cookies that will deny programmatical
    session cookies in your application
    Removes all of these session cookies, return list of disabled cookies, which should be empty at the end of this function
    """
    m = "removeAllDisabledSessionCookies:"

    cellname = getCellName()

    # this gets the list of secure session cookies
    cell_id = AdminConfig.getid( '/Cell:%s/' % cellname )
    sessionManager = AdminConfig.list('SecureSessionCookie', cell_id).split('\n')

    if (sessionManager == ['']):
        sop (m, "You have no secure session cookies for your cell "+cellname)
        return AdminTask.listDisabledSessionCookie() # nothing to delete
    else:

        for sessionCookie in sessionManager:
            size = len(sessionCookie)
            sop (m, sessionCookie)

            # a little parsing
            if sessionCookie[size-1] == ')':
                sessionCookie = sessionCookie[1:size-1]
            else:
                sessionCookie = sessionCookie[1:size-2]

            attr = "-cookieId " + sessionCookie
            AdminTask.removeDisabledSessionCookie(attr)

        #endfor

    return AdminTask.listDisabledSessionCookie()

#end_def

