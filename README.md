edp
======

how to make a dist package
--------------------------
cd edp-davinci && mvn clean package -Pdist




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




