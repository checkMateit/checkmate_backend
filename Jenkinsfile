pipeline {
  agent {
    kubernetes {
      label 'docker'
      defaultContainer 'docker'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:27-cli
    command: ["cat"]
    tty: true
    env:
    - name: DOCKER_HOST
      value: tcp://localhost:2375
    - name: DOCKER_TLS_CERTDIR
      value: ""

  - name: dind
    image: docker:27-dind
    securityContext:
      privileged: true
    env:
    - name: DOCKER_TLS_CERTDIR
      value: ""
    args:
    - --host=tcp://0.0.0.0:2375
    - --host=unix:///var/run/docker.sock
"""
    }
  }

  environment {
    DISCORD_WEBHOOK = credentials('discord-webhook')
    ORG = "checkmateit"
    REGISTRY = "ghcr.io/${ORG}"
    IMAGE_TAG = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Docker test') {
      steps {
        sh '''
          echo "Waiting for dind..."
          for i in $(seq 1 60); do
            if docker info >/dev/null 2>&1; then
              echo "Docker daemon is ready"
              break
            fi
            sleep 2
          done
          docker version
          docker info
        '''
      }
    }

    stage('Detect changed services') {
      steps {
        script {
          def allServices = ["gateway-service","user-service","community-service","store-service","study-service","eureka-service"]

          def hasPrevCommit = (sh(script: 'git rev-parse --verify HEAD~1 >/dev/null 2>&1', returnStatus: true) == 0)

          if (!hasPrevCommit) {
            echo "No previous commit detected (first build). Building ALL services."
            env.CHANGED_SERVICES = allServices.join(" ")
          } else {
            def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
            echo "Changed files:\n${changedFiles}"

            def lines = changedFiles ? changedFiles.readLines() : []
            def changed = []

            for (svc in allServices) {
              if (lines.any { it.startsWith("${svc}/") }) changed << svc
            }

            env.CHANGED_SERVICES = (changed.isEmpty() ? allServices : changed).join(" ")
          }
        }
      }
    }

    stage('Login to GHCR') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
          sh 'echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USER --password-stdin'
        }
      }
    }

    stage('Build & Push Images') {
      steps {
        script {
          def services = env.CHANGED_SERVICES.split("\\s+")
          for (svc in services) {
            def imageName = "${REGISTRY}/checkmate-${svc.replace('-service','')}:${IMAGE_TAG}"
            sh """
              echo "=== Building ${svc} -> ${imageName} ==="
              docker build --build-arg SERVICE=${svc} -t ${imageName} .
              docker push ${imageName}
            """
          }
        }
      }
    }
  }

  post {
    success {
    withCredentials([string(credentialsId: 'discord-webhook', variable: 'DISCORD_WEBHOOK')])
       script{ try { discordSend(
                             title: "${env.JOB_NAME} #${env.BUILD_NUMBER} 성공",
                             description: """\
                       브랜치: ${env.BRANCH_NAME ?: 'N/A'}
                       결과: ${currentBuild.currentResult}
                       실행 시간: ${(currentBuild.duration ?: 0) / 1000}s
                       """,
                             link: env.BUILD_URL,
                             result: currentBuild.currentResult,
                             webhookURL: env.DISCORD_WEBHOOK
                           )} catch (e) { echo "discordSend failed: ${e}" } }
    }

    failure { script{ try {
      discordSend(
        title: "${env.JOB_NAME} #${env.BUILD_NUMBER} 실패",
        description: """\
  브랜치: ${env.BRANCH_NAME ?: 'N/A'}
  결과: ${currentBuild.currentResult}
  실행 시간: ${(currentBuild.duration ?: 0) / 1000}s
  """,
        link: env.BUILD_URL,
        result: currentBuild.currentResult,
        webhookURL: env.DISCORD_WEBHOOK
      )} catch (e)  { echo "discordSend failed: ${e}" } }
    }

    always {
      sh 'docker image prune -f || true'
    }
  }
}