Artifacts with 2, 3 or 4 colons only. 

| Artifacts                          | Result | failure_message | Force Staging | 
| -----------------------------------| ------ | --------------- | ------------- |
| abc:versions-maven-plugin:jar:2.7  | Unstable | Not Empty      | True       | 
| abc:versions-maven-plugin:jar:2.7  | Fail | Not Empty      | False       | 
| org.eclipse.sisu:sisu-inject:pom:0.3.4, com.google.inject:guice:jar:no_aop:4.0 | Pass |  Empty      | Don't Care       | 
| io.swagger.core.v3:swagger-annotations:jar:2.1.2, org.springframework.boot:spring-boot-maven-plugin:jar:2.3.0.RELEASE, org.springdoc:springdoc-openapi-maven-plugin:jar:1.0, org.springdoc:springdoc-openapi-ui:jar:1.3.6| Pass | Empty      | Don't Care       | 
| abc:xyz:123  | Fail | Not Empty      | False       | 
| abc:xyz:123  | Unstable | Not Empty      | True       | 
| org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7, junit:junit:4.12  | Pass | Empty      | Don't Care       | 
| abc:xyz:123, junit:junit:4.12  | Unstable | Not Empty      | True       | 
| abc:xyz:123, junit:junit:4.12  | Fail | Not Empty      | False       | 
|o rg.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7, org.codehaus.mojo:xyz:eclipse-plugin:2.7, org.codehaus.mojo:xyz:javadoc:2.7 | Pass | Empty      | Don't Care      | 
| org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7, org.codehaus.mojo:xyz:jar:2.7, org.codehaus.mojo:xyz:zip:4.6 | Fail | Not Empty      | False       | 
| org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7, org.codehaus.mojo:xyz:jar:2.7, org.codehaus.mojo:xyz:zip:4.6 | Unstable | Not Empty      | True       | 