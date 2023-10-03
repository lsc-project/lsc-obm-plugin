# OBM LSC plugin

[![Build Status](https://travis-ci.org/lsc-project/lsc-obm-plugin.svg?branch=master)](https://travis-ci.org/lsc-project/lsc-obm-plugin)

> **_NOTE:_** Minimal plugin version: 1.2, Minimal required LSC version: 2.1

Presentation
============


[OBM](https://www.obm.org) is a free messaging and collaboration platform useful for just a couple to many thousands of users with strong support for mobile device.

Since version 2.6, OBM provides a REST API to manage users and groups: [https://obm.org/wiki/obm-provisioning-api](https://obm.org/wiki/obm-provisioning-api)

This LSC plugin uses this API in destination, so you can synchronize users and groups from a LDAP directory, a database or any other LSC source.

Installation
============

Get the OBM plugin. Then copy the plugin (.jar file) inside LSC lib directory. (for example `/usr/lib/lsc`)

Configuration
=============

XML namespace
-------------

You need to adapt the namespace of the main markup to import obm namespace:

```
<?xml version="1.0" ?>
<lsc xmlns="http://lsc-project.org/XSD/lsc-core-2.1.xsd" xmlns:obm="http://lsc-project.org/XSD/lsc-obm-plugin-1.0" revision="0">
...
</lsc>
```

Connection
----------

Define the connection like this:

```
    <pluginConnection implementationClass="org.lsc.plugins.connectors.obm.generated.obmConnectionType">
            <name>obm</name>
            <url>http://OBM_SYNC_IP:OBM_SYNC_PORT/obm-sync/</url>
            <username>ADMIN_LOGIN@DOMAIN</username>
            <password>ADMIN_PASSWORD</password>
    </pluginConnection>
```

Parameters are:

* `OBM_SYNC_IP`: IP or name ot the server hosting OBM-Sync
* `OBM_SYNC_PORT`: Port of the OBM-Sync process
* `ADMIN_LOGIN`: Login of the administrator
* `DOMAIN`: OBM Domain 
* `ADMIN_PASSWORD`: Password of the administrator

Destination service
-------------------

You can create a service to manage users or groups. For users:

```
      <pluginDestinationService implementationClass="org.lsc.plugins.connectors.obm.ObmUserDstService">
              <name>obm-user-dst</name>
              <connection reference="obm" />
              <obm:obmUserService>
              <name>obm-user-dst2</name>
              <connection reference="obm" />
                <obm:domainUUID>e0cf03c1-2a41-149a-bcb9-ed18a50815e7</obm:domainUUID>
                <obm:writableAttributes>
                        <string>addresses</string>
                        <string>commonname</string>
                        <string>company</string>
                        <string>description</string>
                        <string>faxes</string>
                        <string>firstname</string>
                        <string>hidden</string>
                        <string>id</string>
                        <string>kind</string>
                        <string>lastname</string>
                        <string>login</string>
                        <string>mails</string>
                        <string>mobile</string>
                        <string>phones</string>
                        <string>profile</string>
                        <string>service</string>
                        <string>title</string>
                        <string>town</string>
                </obm:writableAttributes>
              </obm:obmUserService>
      </pluginDestinationService>
```

For groups:

```
      <pluginDestinationService implementationClass="org.lsc.plugins.connectors.obm.ObmGroupDstService">
              <name>obm-group-dst</name>
              <connection reference="obm" />
              <obm:obmGroupService>
              <name>obm-group-dst2</name>
              <connection reference="obm" />
                <obm:domainUUID>e0cf03c1-2a41-149a-bcb9-ed18a50815e7</obm:domainUUID>
                <obm:writableAttributes>
                        <string>description</string>
                        <string>email</string>
                        <string>name</string>
                        <string>users</string>
                        <string>subgroups</string>
                </obm:writableAttributes>
              </obm:obmGroupService>
      </pluginDestinationService>
```

This service needs the following parameters:

* domainUUID: Identifier of the domain. You can list the available domains on `http://OBM_SYNC_IP:OBM_SYNC_PORT/obm-sync/provisioning/v1/domains/`
* writableAttributes: list of attributes to manage trough this connector. Refer to OBM provisioning documentation to get the complete list ([user API](https://obm.org/wiki/provisioning-user-api) | [group API](https://obm.org/wiki/provisioning-group-api) ).

Main Identifier
------------------

The main identifier should be the same as the chosen pivot attribute. For example, for a LDAP source connection, the `srcBean.getMainIdentifier()` method can *NOT* be used, because it contains the entry DN.

If the pivot attribute is the entryUUID attribute, then this value can be used :

```
<mainIdentifier>
  srcBean.getDatasetFirstValueById("entryUUID");
</mainIdentifier>
```


The rest of the configuration is like any other LSC connector: define conditions and datasets to configure the mapping between your source and your destination.

For example, to manage users and subgroups in a group:

```
          <dataset>
                <name>users</name>
                <forceValues>
                        <string>
                                <![CDATA[
                                var membersSrcDn = srcBean.getDatasetValuesById("member");
                                var memberIdValues = [];
                                for  (var i=0; i<membersSrcDn.size(); i++) {
                                        var memberSrcDn = membersSrcDn.get(i);
                                        var id = "";
                                        if ( memberSrcDn.indexOf("ou=users") != -1 ) {
                                                try {
                                                        id = srcLdap.attribute(memberSrcDn, "entryUUID").get(0);
                                                } catch(e) {
                                                        continue;
                                                }
                                                memberIdValues.push (id);
                                        }
                                }
                                memberIdValues
                                ]]>
                        </string>
                </forceValues>
        </dataset>
        <dataset>
                <name>subgroups</name>
                <forceValues>
                        <string>
                                <![CDATA[
                                var membersSrcDn = srcBean.getDatasetValuesById("member");
                                var memberIdValues = [];
                                for  (var i=0; i<membersSrcDn.size(); i++) {
                                        var memberSrcDn = membersSrcDn.get(i);
                                        var id = "";
                                        if ( memberSrcDn.indexOf("ou=groups") != -1 ) {
                                                try {
                                                        id = srcLdap.attribute(memberSrcDn, "entryUUID").get(0);
                                                } catch(e) {
                                                        continue;
                                                }
                                                memberIdValues.push (id);
                                        }
                                }
                                memberIdValues
                                ]]>
                        </string>
                </forceValues>
        </dataset>
```

Plugin loading
==================

To load the plugin into LSC, you need to modify `JAVA_OPTS`:

```
JAVA_OPTS="-DLSC.PLUGINS.PACKAGEPATH=org.lsc.plugins.connectors.obm.generated"
```

For example, to run a user synchronization:

```
JAVA_OPTS="-DLSC.PLUGINS.PACKAGEPATH=org.lsc.plugins.connectors.obm.generated" /usr/bin/lsc -f /etc/lsc/obm/ -s user -t 1
```

> **_NOTE:_** The use of -t 1 limits LSC to one thread, which is better when using the OBM REST API.

Commit transaction
==================

OBM really applies the synchronization done by LSC when the transaction is committed.

To do this, you need to insert a pair of hook tags :

```
<task>
  <name>taskName1</name>
  <bean>org.lsc.beans.SimpleBean</bean>
  <cleanHook>org.lsc.plugins.connectors.obm.ObmDao.close</cleanHook>
  <syncHook>org.lsc.plugins.connectors.obm.ObmDao.close</syncHook> 
  <.../>
</task>
```

