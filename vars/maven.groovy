def mavenGoals() {
    sh 'mvn clean compile test package'
}


