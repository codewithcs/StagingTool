# OSS Staging tool

This tool is used to stage artifacts from some dev repository to the production repositories on Callisto.

See: [How to use staging](https://mxwiki.murex.com/confluence/display/TMT/Validate+your+TPS+via+Staging)

In the latest version, we are attempting to shield end-users from some of the less-important failures, while also making real failures more clear and understandable.

## Types of failure:

### a) Some 'unimportant' artifact type is missing.

i.e. an artifact of type _javadoc_ or _bundle_ was not found.

    In this case, we should force the staging, but log a warning for the developer to let them know something was missing. (this list should exclude 'bundle's because they aren't a real type)

### b) An essential artifact type is missing.

i.e. an artifact of type _POM_, _sources_, or _jar_ was not found.

    In this case, we should fail, listing the essential components and some optional ones


We have decided to allow a failure of type A, while continuing to block on those of type B. Type A now passes silently. 

---

## Release Notes 
1.2
* Add BPM staging support
1.1
* MAVEN_HOME is configurable 
* Pipeline Usability Add badges to job history and summary