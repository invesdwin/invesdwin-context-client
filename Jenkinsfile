pipeline {
  agent any
  stages {
    stage('Build and test') {
      steps{
		wrap([$class: 'Xvfb', autoDisplayName: true, displayNameOffset: 1]) {
	      withMaven {
            sh 'mvn clean install -f invesdwin-context-client-parent/pom.xml -T4'
          }  
		}
      }
    }
  }
}