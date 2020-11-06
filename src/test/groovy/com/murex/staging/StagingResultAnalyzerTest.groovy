package groovy.com.murex.staging

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StagingResultAnalyzerTest  {
	private static final String LOG_TEMPLATE = """#MAVEN_DOWNLOAD_MESSAGES

Sucessful Retrievals:

#SUCCESSFUL_RETRIEVALS

Failed Retrievals:

#FAILED_RETRIEVALS
Sucessful Deployments:

#SUCCESSFUL_DEPLOYMENTS
Failed Deployments:

#FAILED_DEPLOYMENTS
Skipped Deployments (POM already in target):

#SKIPPED_DEPLOYMENTS
Potential Deployments :

#POTENTIAL_DEPLOYMENTS

"""
	StagingResultAnalyzer analyzer
	List<String> artifacts
	List<String> logs

	@BeforeEach
	void setUp() {
		artifacts = new ArrayList<>()
		analyzer = new StagingResultAnalyzer()
		logs = new ArrayList<>()
	} 

	@Test // exit code = 1
	void doNotIgnorePatternIfSubstringOfValidGAV() {		
		def logs = generateLogs(["abc:versions-maven-plugin:jar:2.7", "abc:abc:pom:123"],"")
	    analyzer.validateLogs(logs)
		String expectedFailureMessage = """Could not find artifact abc:versions-maven-plugin:jar:2.7 in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)
Could not find artifact abc:abc:pom:123 in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)
"""
		assertEquals(expectedFailureMessage, analyzer.getCurrentFailureMessage())
	}

	@Test // exit code = 1
	void artifactsNotFoundMessagesRepeatedMultipleTimes() {
		def logs = generateLogs(["abc:abc:pom:123", "abc:abc:jar:123"],"")
		analyzer.validateLogs(logs)
		String expectedFailureMessage =
				"Could not find artifact abc:abc:pom:123 in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)\n" +
				"Could not find artifact abc:abc:jar:123 in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)\n"
		
		assertEquals(expectedFailureMessage, analyzer.getCurrentFailureMessage())
	}

	@Test // exit code = 0
	void allArtifactsDownloadedSuccessfully(){
		def logs = generateLogs([],
			 "org.eclipse.sisu:sisu-inject:pom:0.3.4\njunit:junit:4.12")
		analyzer.validateLogs(logs)		
		assertEquals("", analyzer.getCurrentFailureMessage())
	}

	@Test // exit code = 1
	void allArtifactsDownloadedSuccessfully_givenSomeIgnoredArtifacts() {
		def logs = generateLogs(["org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7",
								 "group:artifact:bundle:1.0",
								 "group1:artifact1:jar:vc15:1.0",
								 "group:artifact2:jar:javadoc:1.0"],"")
		analyzer.validateLogs(logs)
		assertEquals("", analyzer.getCurrentFailureMessage())
	}

	@Test
	void givenIgnoredArtifactsOnlyCouldNotBeDownloaded_thenTreatAsSuccess() {
		def logs = generateLogs(["org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7" + "org.codehaus.mojo:abc:eclipse-plugin:2.7"+ "org.codehaus.mojo:xyz:javadoc:2.7"], "")
		analyzer.validateLogs(logs)
		assertEquals("", analyzer.getCurrentFailureMessage())
	}

	@Test
	void givenIgnoredAndRealArtifactCouldNotBeDownloaded_thenReportFailureForRealArtifactOnly() {
		def logs = generateLogs([ "org.codehaus.mojo:versions-maven-plugin:maven-plugin:2.7","org.codehaus.mojo:abc:jar:2.7"], "")
		analyzer.validateLogs(logs)
		String expectedFailureMessage = "Could not find artifact org.codehaus.mojo:abc:jar:2.7 in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)\n"
		assertEquals(expectedFailureMessage, analyzer.getCurrentFailureMessage())
	}

	private static List<String> toList(String logExtract) {
		return logExtract.split("\r?\n|,")
	}

	private static List<String> generateLogs(List<String> failedRetrievals, String successfulRetrievals) {
		String finalFailedRetrievalMessages = ""
		if(!failedRetrievals.isEmpty()) {
			failedRetrievals.each {
				finalFailedRetrievalMessages += "Could not find artifact $it in central (http://repo-dev:8082/nexus/content/repositories/maven.central/)\n"
			}
		}
		def log = LOG_TEMPLATE.replaceAll("#SUCCESSFUL_RETRIEVALS\r?\n","$successfulRetrievals")
							  .replaceAll("#SUCCESSFUL_DEPLOYMENTS", "")
							  .replaceAll("#MAVEN_DOWNLOAD_MESSAGES", "$finalFailedRetrievalMessages")
							  .replaceAll("#FAILED_RETRIEVALS", "$finalFailedRetrievalMessages")
							  .replaceAll("#FAILED_DEPLOYMENTS", "")
							  .replaceAll("#SKIPPED_DEPLOYMENTS", "")
							  .replaceAll("#POTENTIAL_DEPLOYMENTS", "")
		return toList(log)
	}

}