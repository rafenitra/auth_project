pipeline {
    agent none

    environment {
        SONAR_TOKEN = credentials('sonarcloud_token')
        DOCKER_CREDS = credentials('dockerhub_creds')
        BACK_DIR = "authback"
        FRONT_DIR = "authfront"
    }

    stages {
        stage('1. Checkout & Init') {
            agent {label 'PC_LOCAL'}
            steps {
                script {
                    if (!fileExists(".git")) {
                        bat "git clone -b develop https://github.com/rafenitra/auth_project.git ."
                    } else {
                        bat "git fetch origin develop"
                        bat "git reset --hard origin/develop"
                    }
                }
            }
        }

        stage('2. Frontend (Installation & Tests)') {
            agent {label 'PC_LOCAL'}
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
            agent {label 'PC_LOCAL'}
            steps {
                dir("${env.BACK_DIR}") {
                    echo "Tests JUnit..."
                    bat 'mvn test "-Dspring.profiles.active=test" '
                }
            }
        }

        stage('4. Analyse Sonar - Backend') {
            agent {label 'PC_LOCAL'}
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
            agent {label 'PC_LOCAL'}
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

        stage('6. Build (Docker Compose)') {
            agent {label 'PC_LOCAL'}
            steps {
                echo "Debut du docker compose build"
                bat 'docker-compose build'
                echo "Application buildé avec succès !"
            }
        }

        stage('7. Push vers le docker registry'){
            agent {label 'PC_LOCAL'}
            steps {
                bat 'docker login -u %DOCKER_CREDS_USR% -p %DOCKER_CREDS_PSW%'
                bat 'docker-compose push'
                echo "Application publié dans Docker Hub"
            }
        }

        stage('8. Clean les containeurs avant') {
                agent { label 'built-in' }
                steps {
                    sh 'docker-compose down --remove-orphans || true'
            
                    // 2. Suppression forcée des conteneurs par leur nom exact 
                    // (Cela règle le conflit même si le conteneur vient d'un autre projet)
                    sh 'docker rm -f auth-db authapp-back authapp-front || true'
                //sh 'docker-compose down --remove-orphans || true'
		        //sh 'docker-compose down'
            }
        }

        stage('9. CD & Deploy (SUR AZURE)') {
            agent { label 'built-in' } // Le Maître (Azure) reprend la main
            steps {
                withCredentials([
                    string(credentialsId: 'db_password_prod', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'jwt_secret_prod', variable: 'JWT_SECRET'),
                    string(credentialsId: 'cors_url_prod', variable: 'CORS_URL')
                ]){
                    script {
                        env.DB_USER = "postgres"
                        env.DB_NAME = "authdb"
                        
                        if (!fileExists(".git")) {
                            sh "git clone -b develop https://github.com/rafenitra/auth_project.git ."
                        } else {
                            sh "git fetch origin develop && git reset --hard origin/develop"
                        }
                        sh 'docker-compose pull'
		                sh 'docker-compose up -d'
                    }
                }
                echo "Déploiement terminé"
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
