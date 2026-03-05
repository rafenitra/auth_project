pipeline {
    agent any

    tools {
        maven 'MAVEN_3'
        nodejs 'NODE_JS'
    }

    //Commentaire pour savoir si ça marche avec l'automatisation du pipeline ou pas
    //rajout commentaire
    //un autre commentaire
    environment {
        SONAR_TOKEN = credentials('sonarcloud_token')
        // On utilise des chemins relatifs au workspace de Jenkins
        BACK_DIR = "authback"
        FRONT_DIR = "authfront"
    }

    stages {
        stage('1. Checkout & Init') {
            steps {
                // Jenkins récupère automatiquement le code ici si configuré en "Pipeline from SCM"
                echo "Code récupéré depuis GitHub dans : ${WORKSPACE}"
            }
        }

        stage('2. Frontend (Installation & Tests)') {
            steps {
                dir("${env.FRONT_DIR}") {
                    echo "Installation des modules..."
                    bat 'npm install'
                    echo "Exécution des tests Angular..."
                    // Ajout du flag --watch=false pour que Jenkins ne bloque pas
                    bat 'npx ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('3. Backend (Compilation & Tests)') {
            steps {
                dir("${env.BACK_DIR}") {
                    echo "Tests JUnit..."
                    bat 'mvn clean test'
                }
            }
        }

        stage('4. Analyse Sonar - Backend') {
            steps {
                dir("${env.BACK_DIR}") {
                    bat """
                    mvn sonar:sonar ^
                    -Dsonar.projectKey=rafenitra_auth_project ^
                    -Dsonar.organization=rafenitra ^
                    -Dsonar.host.url=https://sonarcloud.io ^
                    -Dsonar.token=%SONAR_TOKEN% ^
                    -Dsonar.qualitygate.wait=true ^
                    -Dsonar.sources=src/main/java ^
                    -Dsonar.java.binaries=target/classes
                    """
                }
            }
        }

        stage('5. Analyse Sonar - Frontend') {
            steps {
                script {
                    def scannerHome = tool name: 'SONAR_SCANNER_CLI', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                    dir("${env.FRONT_DIR}") {
                        bat """
                        "${scannerHome}/bin/sonar-scanner" ^
                        -Dsonar.projectKey=rafenitra_auth_project_frontend ^
                        -Dsonar.organization=rafenitra ^
                        -Dsonar.host.url=https://sonarcloud.io ^
                        -Dsonar.token=%SONAR_TOKEN% ^
                        -Dsonar.sources=src ^
                        -Dsonar.exclusions=**/node_modules/**,**/*.spec.ts
                        """
                    }
                }
            }
        }

        stage('6. Docker Build') {
            steps {
                // Docker-compose à la racine du projet
                echo "Construction des images Docker..."
                bat 'docker-compose build'
            }
        }
    }

    post {
        always {
            // Optionnel : Nettoyage pour ne pas saturer le disque de Jenkins
            echo "Fin du pipeline."
        }
    }
}