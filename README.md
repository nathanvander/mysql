# mysql
This is a non-JDBC Java driver for MariaDB

It is a wrapper around libmariadb.dll, which is part of MariaDB Connector C, available at:
https://downloads.mariadb.org/connector-c/ . This only works with the 32-bit version for some reason.
The ResultSet and Field classes have not been tested.

This has a dependency on: 
jna.jar
win32-x86.jar
jna-platform.jar
which are available at: https://github.com/java-native-access/jna/tree/master/dist.

I dislike the JDBC driver because I find it bloated and overly complicated.  This is much simpler.
