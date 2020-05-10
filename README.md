## Description

[Lightbend Akka for Scala - Professional](https://academy.lightbend.com/courses/course-v1:lightbend+LAS-P+v1/course/) in Java

## To Run

Add to `~/.m2/settings.xml`:

```xml
<settings>
 <profiles>
   <profile>
     <id>lightbend</id>
     <repositories>
      <repository>
        <id>lightbend-commercial</id>
        <name>Lightbend Commercial</name>
        <url>https://repo.lightbend.com/pass/${token}/commercial-releases</url>
      </repository>
     </repositories>
   </profile>
 </profiles>
</settings>
```
Run:

```bash
./mvnw -Plightbend package -DskipTests -pl exercises -am exec:exec
```
