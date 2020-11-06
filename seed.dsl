pipelineJob('stage-artifacts') {
    properties {
        disableConcurrentBuilds()
    }

    parameters {
        textParam('ARTIFACTS', '', "<h4>A string of the form <br><br><i>groupId:artifactId[:packaging[:classifier]]:version.</i><br><br>NOTE: recommended to avoid the 'jar' packaging type in the coordinate wherepossible- e.g. g:a:v not g:a:jar:v<br><br>Multiple artifacts can be passed in, separated by '|' or end-of-line<br><br>Example : <br><br><i>org.jacoco:org.jacoco.core:pom:0.7.9</i><br><br>OR<br><br><i>a:b:c<br>a1:b1:c1</i></h4>")
        booleanParam('I_KNOW_WHAT_I_WANT', false, "Do you know exactly what you want to stage?")
        stringParam('SOURCE_REPOSITORY', 'http://repo-dev:8082/nexus/content/repositories/maven.central/', '<h4>Source Repo that contains the artifacts you want to stage.</h4>')
        choiceParam('TARGET_REPOSITORY', ['Callisto: Murex Central (Production TPS - Java)', 'Callisto: Mx Toolchain (build, test & internal-use TPS)', 'Callisto: Mx 3p (Production TPS - Cpp)', 'Callisto: BPM (all BPM-specific TPS)'], "Staging current supported for 'Murex Central' and 'Mx Toolchain' on callisto.<br>C++ staging is coming soon...")
        booleanParam('TEST_MODE', false, 'Check the artifacts exist in the SOURCE_REPOSITORY, without actually staging them')
        booleanParam('INCLUDE_JAVADOC', true, 'Search for, and deploy, JavaDoc')
        booleanParam('INCLUDE_SOURCES', true, 'Search for, and deploy, Sources')
        stringParam('STAGING_COMMENT', 'auto-staging via Jenkins', 'Optional comment for the staged repository - e.g. "Testing JUnit 4.2.1"<br>This can help adminstrators to monitor what items are being staged and why.')
        booleanParam('FORCE_STAGING', false, 'Force staging even if some artifacts were not retrieved')
    }

    environmentVariables {
        env('MAVEN_ROOT', "${MAVEN_ROOT}")
    }

    authorization { //allow logged in users to build the job
        permission('hudson.model.Item.Build:authenticated')
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        name('origin')
                        url('ssh://git@stash.murex.com:7999/TMT/mx-staging.git')
                    }
                    branch("master")
                    extensions {
                        cleanBeforeCheckout()
                    }
                }
                scriptPath('Jenkinsfile')
            }
        }
    }
}
