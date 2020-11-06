package groovy.com.murex.staging

import groovy.transform.Field

@Field
List<String> stageLogs
@Field
String failureMessage = ""
@Field
IGNORE_TYPES = ["bundle", "maven-plugin", "eclipse-plugin", "javadoc", "vc15"]

void validateLogs(List<String> logs) {
	stageLogs = logs
	setFailureMessage()
}

void setFailureMessage() {
	for(String message: getFailureMessagesFromLogs()){
		failureMessage += message
	}
}

String getCurrentFailureMessage(){
	return failureMessage
}

def getFailureMessagesFromLogs(){
	Set<String> failureMessages = new HashSet<>();
	stageLogs.findAll {
		def m1 = it =~ /Could not find artifact (.+)/
		if(m1.find()) {
			String message = "Could not find artifact " +  m1[0][1]  + "\n"
			List<String> list = m1[0][1].split(":")

			boolean found = false
			for(String part: list) {
				for(String ignore_type: IGNORE_TYPES){
					if(part.equals(ignore_type)){
						found = true
						break
					}
				}
			}
			if(!found){
				failureMessages.add(message)
			}
		}
	}
	return failureMessages
}

boolean checkArtifactsNotFoundError(){
	return failureMessage.isEmpty()
}

boolean noArtifactsRetrievedDeployed(List<String> logs){
	if(!checkForSuccessfulRetrievalsAndDeployments(logs, "Retrievals") || ( ("${TEST_MODE}" == 'false') && !checkForSuccessfulRetrievalsAndDeployments(logs, "Deployments") ) ){
		return true
	}
	return false
}

boolean foundSuccessfulRetrievalsOrDeployments(List<String> logs, String string){
	def artifacts = ""
	for(int i=0 ; i<logs.size() ; i++ ){
		if(logs.get(i) == "Sucessful ${string}:" ){
			while(logs.get(i+1) != "Failed ${string}:" ) {
				artifacts = artifacts + logs.get(i+1) + "\n"
				i++
			}
			break
		}
	}
	return artifacts.size() > 2

}

return this 