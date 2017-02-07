edp
======
how to package wormhole/davinci?
------
cd edp

-- for linux terminal --
mvn --non-recursive clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; \
cd common; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..; \
cd endurance/endurance-db; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ../..; \
cd endurance/endurance-hadoop; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ../..; \
cd endurance/endurance-hbase; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ../..; \
cd endurance/endurance-kafka; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ../..; \
cd endurance/endurance-spark; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ../..; \
cd wormhole-ums; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..; \
cd litup; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..; \
cd moonbox; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..; \
cd swifts; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..; \
cd swifts-yrd; mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true; cd ..

-- for windows idea2016 terminal --
mvn --non-recursive clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true 
cd common
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../
cd endurance/endurance-db
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd endurance/endurance-hadoop
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd endurance/endurance-hbase
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd endurance/endurance-kafka
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd endurance/endurance-spark
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd wormhole-ums
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../
cd litup
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../
cd moonbox
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../
cd swifts/swifts-common
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd swifts/swifts-framework
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../../
cd swifts-yrd
mvn clean deploy -Dassembly.skipAssembly=true -U -Dmaven.test.skip=true
cd ../

cd wormhole
mvn clean package -U -Dmaven.test.skip=true

cd swifts/swifts-sql
mvn clean package -U -Dmaven.test.skip=true

cd swifts/swifts-strategy
mvn clean package -U -Dmaven.test.skip=true

cd davinci; mvn clean package -U -Dmaven.test.skip=true; cd ..

mvn clean install -Dassembly.skipAssembly=true -Dmaven.test.skip=true

how to package wormhole without test?
------
cd wormhole; mvn clean package -U -Dmaven.test.skip=true; cd ..

mvn clean package -Dmaven.test.skip=true



how to check scala style?
------
mvn scalastyle:check



how to unit test spark?
------
-Xms512M -Xmx2048M -XX:+CMSClassUnloadingEnabled



maven settings.xml
------
    <?xml version="1.0" encoding="UTF-8"?>
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <servers>
            <server>
                <id>ce-nexus</id>
                <username>borrowsale</username>
                <password>bs@123</password>
            </server>
        </servers>
    
        <mirrors>
            <mirror>
                <id>nexus-osc</id>
                <mirrorOf>*</mirrorOf>
                <name>Nexus osc</name>
                <url>http://10.100.31.71:8080/nexus/content/groups/public/</url>
            </mirror>
        </mirrors>
    </settings>




