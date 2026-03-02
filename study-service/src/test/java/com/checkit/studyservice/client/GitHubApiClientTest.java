package com.checkit.studyservice.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GitHub API 클라이언트 단위 테스트.
 * - parseOwnerRepo: URL/문자열 파싱 검증
 */
@DisplayName("GitHubApiClient 단위 테스트")
class GitHubApiClientTest {

    @Nested
    @DisplayName("parseOwnerRepo")
    class ParseOwnerRepo {

        @Test
        void https_github_com_형식() {
            GitHubApiClient.OwnerRepo result = GitHubApiClient.parseOwnerRepo("https://github.com/owner/repo");
            assertThat(result).isNotNull();
            assertThat(result.owner()).isEqualTo("owner");
            assertThat(result.repo()).isEqualTo("repo");
        }

        @Test
        void https_github_com_끝에_슬래시() {
            GitHubApiClient.OwnerRepo result = GitHubApiClient.parseOwnerRepo("https://github.com/owner/repo/");
            assertThat(result).isNotNull();
            assertThat(result.owner()).isEqualTo("owner");
            assertThat(result.repo()).isEqualTo("repo");
        }

        @Test
        void http_github_com_형식() {
            GitHubApiClient.OwnerRepo result = GitHubApiClient.parseOwnerRepo("http://github.com/foo/bar");
            assertThat(result).isNotNull();
            assertThat(result.owner()).isEqualTo("foo");
            assertThat(result.repo()).isEqualTo("bar");
        }

        @Test
        void git_at_github_com_형식() {
            GitHubApiClient.OwnerRepo result = GitHubApiClient.parseOwnerRepo("git@github.com:user/project.git");
            assertThat(result).isNotNull();
            assertThat(result.owner()).isEqualTo("user");
            assertThat(result.repo()).isEqualTo("project");
        }

        @Test
        void owner_repo_단축_형식() {
            GitHubApiClient.OwnerRepo result = GitHubApiClient.parseOwnerRepo("owner/repo");
            assertThat(result).isNotNull();
            assertThat(result.owner()).isEqualTo("owner");
            assertThat(result.repo()).isEqualTo("repo");
        }

        @Test
        void null이면_null_반환() {
            assertThat(GitHubApiClient.parseOwnerRepo(null)).isNull();
        }

        @Test
        void 빈문자열이면_null_반환() {
            assertThat(GitHubApiClient.parseOwnerRepo("")).isNull();
            assertThat(GitHubApiClient.parseOwnerRepo("   ")).isNull();
        }

        @Test
        void 슬래시_하나만_있으면_null() {
            assertThat(GitHubApiClient.parseOwnerRepo("owner/")).isNull();
            assertThat(GitHubApiClient.parseOwnerRepo("/repo")).isNull();
        }
    }
}
