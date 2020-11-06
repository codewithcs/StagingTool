@Library('3p-utils@master') _

import groovy.transform.Field

def exit_code
REQUIRED = ["jar", "pom", "sources", "zip"]

pipeline {
    agent any

    environment {
        MAVEN_OPTS = "-Xms256m -Xmx1200m"
        TARGET_REPO = " http://dummy"
        TPS_DIRECTORY_PATH = "$WORKSPACE/local-cache/"
        OUTPUT_FILENAME = "missing-artifacts.log"
    }

    stages {

        stage('Fetch Components') {
            steps {

                script {

                    if(checkEmptyArtifacts()){
                        addErrorBadge(text:"No artifacts found in input. Use the format: group:artifact:type:classifier:version")
                        setBuildAndStageResult('No artifacts found in input, nothing to stage, Terminating.', 'FAILURE')
                    }

                    wrap([$class: 'BuildUser']) {
                        addShortTextInternal("${BUILD_USER_ID}", 'grey') ;
                    }

                    addInfoBadge("${ARTIFACTS}")

                    cleaned_list = clean_input(ARTIFACTS)
                    catchError{
                        exit_code = sh returnStatus: true, script: """#!/bin/bash
                        $MAVEN_ROOT/bin/mvn org.apache.maven.plugins:maven-dependency-plugin:2.9:get -B -DuseDevMavenRepo -Dartifact=com.simpligility.maven:maven-repository-provisioner:1.2.1mx1:jar:jar-with-dependencies -Ddest=. -s ${WORKSPACE}@script/settings.xml
                        java -jar maven-repository-provisioner-1.2.1mx1-jar-with-dependencies.jar -a "${cleaned_list}" -t dummy/ -u dummy -p dummy -s "$SOURCE_REPOSITORY" -vo true -ij $INCLUDE_JAVADOC -is $INCLUDE_SOURCES"""

                        sh """echo \"Cleaning up remote.repositories files...\"
                        find "$TPS_DIRECTORY_PATH" -name "_remote.repositories" -exec rm '{}' \\;
                        """
                        println "Exit code: " + exit_code
                    }

                    failIfInvalidMavenCoordinatesFound()

                    List<String> stageLogs = currentBuild.rawBuild.getLog(300)
                    def analyzer = load "${env.WORKSPACE}/src/main/groovy/com/murex/staging/StagingResultAnalyzer.groovy"
                    analyzer.validateLogs(stageLogs)
                    def failure_message = analyzer.getCurrentFailureMessage()
                    if(!failure_message.isEmpty()) {
                        createBadgeAndSummaryAndSetBuildAndStageResult(failure_message, FORCE_STAGING=="true"?'UNSTABLE':'FAILURE')
                    }
                }
                //  deleteDir()
            }
        }

        stage('Staging') {
            when {
                expression {
                    return (TEST_MODE != "true" )
                }
            }
            steps {
                wrap([$class: 'BuildUser']) {
                    withCredentials([usernamePassword(credentialsId: 'staging-user', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh "$MAVEN_ROOT/bin/mvn -B org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:deploy-staged-repository -s ${WORKSPACE}@script/settings.xml -DrepositoryDirectory=$TPS_DIRECTORY_PATH -Dserver.user=$USER -Dserver.password=$PASS -DuseStaging -DserverId=callisto -DnexusUrl=http://callisto:8080/nexus/ -DstagingProfileId=${get_staging_repo_id("$TARGET_REPOSITORY")} -DstagingDescription=\"[${get_build_user()} #${BUILD_NUMBER}] ${STAGING_COMMENT}\""
                    }
                }

                script{
                    def logs = currentBuild.rawBuild.getLog(300)
                    def repoIdFound = false
                    logs.findAll {
                        def m = it =~ /Created \*\*\*\* repository with ID "([a-z0-9-]+)"/
                        if(m.find()) {
                            repoIdFound = true
                            addShortText( text: ""+ m[0][1], border: "0", link:"http://callisto:8080/nexus/content/repositories/" + m[0][1] )
                        }
                    }

                    if(!repoIdFound){
                        addWarningBadge(text:"No staging repository found")
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                            error("No staging repository found")
                        }
                    }
                }
            }
        }}
    post {
        always {
            archiveArtifacts artifacts: OUTPUT_FILENAME, allowEmptyArchive: true
        }
    }
}

// for artifacts with < 2 and > 4 colons
def failIfInvalidMavenCoordinatesFound(){
    artifacts = "${ARTIFACTS}".split("\n")
    for(String artifact: artifacts){
        artifact = artifact.replaceAll("\\s","")
        int numOfColons = artifact.split(":").size() - 1
        if(!"".equals(artifact) && (numOfColons < 2 || numOfColons > 4) ) {
            createBadgeAndSummaryAndSetBuildAndStageResult("Bad artifact coordinates, expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>", 'FAILURE')
        }
    }
}

def checkEmptyArtifacts(){
    artifacts = "${ARTIFACTS}".split("\n")
    for(String artifact: artifacts){
        artifact = artifact.replaceAll("\\s","")
        if(!artifact.isEmpty()){
            return false
        }
    }
    return true
}

def get_staging_repo_id(String target) {
    if (target ==~ /Callisto: Murex Central.*/) {
        return "306cc3cf10228e"
    } else if (target ==~ /Callisto: Mx Toolchain.*/) {
        return "32ad09aeecf257"
    } else if (target ==~ /Callisto: Mx 3p.*/) {
        return "3b2df35dabf9d5"
    } else if (target ==~ /Callisto: BPM.*/) {
        return "286f81161830bf"
    } else {
        return "ERROR: unknown TARGET_REPOSITORY " + target
    }
}

def clean_input(String input) {
    res = ""

    if (I_KNOW_WHAT_I_WANT == "true") {
        comps = input.split("\n")
        for (String comp : comps) {
            if(!comp.isEmpty()){
                res += comp + "|"
            }
        }
    } else {
        for (String artifact : artifacts_to_stage(input).gavsNotInReferenceRepository) {
            res += artifact + "|"
        }
    }

    println "Artifacts: " + res
    return res.replaceAll(" ", "")
}

@NonCPS
def get_build_user() {
    return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
}

// workaround because the native addShortText(...) is badly formatted
def addShortTextInternal(String message, String color='black') {
    addHtmlBadge("<font color=\"$color\">$message </font>")
}

def createBadgeAndSummary(String message, String severity){
    if( severity == 'FAILURE'){
        addErrorBadge(text: message)
        message = message.replaceAll("\n", "<br>")
        createSummary(icon: "error.gif", text: message)
    }

    else if (severity == 'UNSTABLE') {
        addWarningBadge(text: message)
        message = message.replaceAll("\n", "<br>")
        createSummary(icon: "warning.gif", text: message)
    }
}

def setBuildAndStageResult(String message, String severity){
    catchError(buildResult: "$severity", stageResult: "$severity") {
        if(severity == 'UNSTABLE')
            error("$message")
        else
            sh 'exit 1'
    }

    if(severity == 'FAILURE'){
        error("$message")
    }
}

def createBadgeAndSummaryAndSetBuildAndStageResult(String message, String severity) {
    createBadgeAndSummary(message, severity)
    setBuildAndStageResult(message, severity)
}

def noRetrievalsOrDeploymentsDetected(def analyzer, List<String> stageLogs) {
    return analyzer.checkRetrievalsAndDeploymentsFailure(stageLogs)
}