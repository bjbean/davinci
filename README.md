edp
======

how to make a dist package
--------------------------
mvn clean install package -Pdist

how to generate THIRD-PARTY.txt?
------
mvn clean install -DskipTests

mvn license:aggregate-add-third-party


how to check scala style?
------
mvn scalastyle:check



how to unit test spark?
------
-Xms512M -Xmx2048M -XX:+CMSClassUnloadingEnabled





