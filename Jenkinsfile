pipeline {
  agent any
  stages {
    stage('Build and test') {
      steps{
		wrap([$class: 'Xvfb']) {
	      withMaven {
            sh 'mvn clean install -f invesdwin-context-client-parent/pom.xml -T4'
          }  
		}
      }
    }
  }
}