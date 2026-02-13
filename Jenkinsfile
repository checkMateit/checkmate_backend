pipeline {
  agent any

  environment {

    REGISTRY_NS = "ghcr.io/checkMateit"        // <- org 이름
    IMAGE_TAG   = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
  }

  stages {

    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Detect changed services') {
      steps {
        script {
          // 변경 기준 커밋 잡기 (PR이면 target, 아니면 직전 커밋)
          sh 'git fetch --all --prune'

          def baseRef = ""
          if (env.CHANGE_TARGET) {
            // 멀티브랜치 PR 빌드일 때
            baseRef = "origin/${env.CHANGE_TARGET}"
          } else {
            // 일반 브랜치 빌드일 때 (직전 커밋)
            baseRef = "HEAD~1"
          }

          // 변경 파일 목록
          def changedFiles = sh(
            script: "git diff --name-only ${baseRef} HEAD || true",
            returnStdout: true
          ).trim()

          echo "Changed files:\n${changedFiles}"

          // 서비스 폴더 리스트
          def serviceDirs = [
            "gateway-service",
            "user-service",
            "community-service",
            "store-service",
            "study-service",
            "eureka-service"
          ]

          def changedServices = [] as Set

          if (changedFiles) {
            changedFiles.split("\n").each { f ->
              serviceDirs.each { svc ->
                if (f.startsWith("${svc}/")) {
                  changedServices.add(svc)
                }
              }
            }
          }

          // 공통 모듈(common-service 등) 바뀌면 전부 빌드 (원하면 조정 가능)
          if (changedFiles.contains("common-service/") || changedFiles.contains("build.gradle") || changedFiles.contains("settings.gradle")) {
            changedServices = serviceDirs as Set
          }

          if (changedServices.isEmpty()) {
            echo "No service changes detected. Skip build/push."
            env.CHANGED_SERVICES = ""
          } else {
            env.CHANGED_SERVICES = changedServices.join(",")
            echo "Changed services: ${env.CHANGED_SERVICES}"
          }
        }
      }
    }

    stage('Login to GHCR') {
      when { expression { return env.CHANGED_SERVICES?.trim() } }
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'ghcr-credentials',
          usernameVariable: 'GITHUB_USER',
          passwordVariable: 'GITHUB_TOKEN'
        )]) {
          sh '''
            echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USER --password-stdin
          '''
        }
      }
    }

    stage('Build & Push changed images') {
      when { expression { return env.CHANGED_SERVICES?.trim() } }
      steps {
        script {
          def services = env.CHANGED_SERVICES.split(",")

          for (svc in services) {
            def imageName = "${REGISTRY_NS}/checkmate-${svc}:${IMAGE_TAG}"

            sh """
              docker build -t ${imageName} ./${svc}
              docker push ${imageName}
            """
          }
        }
      }
    }


  }

  post {
    success { echo "Build/Push done. ArgoCD Image Updater will update tags." }
    failure { echo "Build Failed" }
  }
}