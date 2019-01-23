# kotlin-module-metadata-transformer

Resource Transformer for the maven-shade-plugin processing *.kotlin_module files

## Usage

Add the following to the `<plugins>` section of your pom.xml:
```xml
<plugin>
    <artifactId>maven-shade-plugin</artifactId>
    <dependencies>
        <dependency>
            <groupId>io.invenium.maven</groupId>
            <artifactId>kotlin-module-metadata-transformer</artifactId>
            <version>1.3.11-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="io.invenium.maven.shade.KotlinModuleMetadataResourceTransformer">
                        <moduleName>${artifactId}</moduleName>
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```
