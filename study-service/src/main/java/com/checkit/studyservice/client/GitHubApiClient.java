package com.checkit.studyservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GitHub REST API 호출 (커밋 목록 조회).
 * 공개 repo 기준; 토큰 미설정 시 비인증 호출(rate limit 낮음), 설정 시 Bearer로 호출.
 */
@Component
@Slf4j
public class GitHubApiClient {

    private static final String COMMITS_URL = "https://api.github.com/repos/{owner}/{repo}/commits?sha={sha}&since={since}&until={until}&per_page=100";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiToken;

    public GitHubApiClient(@Value("${app.github.token:}") String apiToken) {
        this.apiToken = apiToken != null && !apiToken.isBlank() ? apiToken : null;
    }

    /**
     * 지정 기간 내 해당 브랜치 커밋 목록에서 author의 GitHub 사용자 ID 목록을 반환.
     * (동일 사용자가 여러 커밋을 넣어도 ID는 한 번만 포함)
     */
    public List<String> listCommitAuthorIds(String owner, String repo, String branch, Instant since, Instant until) {
        String sinceStr = DateTimeFormatter.ISO_INSTANT.format(since);
        String untilStr = DateTimeFormatter.ISO_INSTANT.format(until);
        String url = COMMITS_URL
                .replace("{owner}", owner)
                .replace("{repo}", repo)
                .replace("{sha}", branch)
                .replace("{since}", sinceStr)
                .replace("{until}", untilStr);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.parseMediaType("application/vnd.github+json")));
        if (apiToken != null) {
            headers.setBearerAuth(apiToken);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            if (response.getBody() == null || !response.getBody().isArray()) {
                return List.of();
            }
            List<String> authorIds = new ArrayList<>();
            for (JsonNode node : response.getBody()) {
                Optional<String> id = authorIdFromCommit(node);
                id.ifPresent(authorIds::add);
            }
            return authorIds;
        } catch (Exception e) {
            log.warn("GitHub API list commits failed: owner={}, repo={}, branch={}, error={}", owner, repo, branch, e.getMessage());
            return List.of();
        }
    }

    /** commit 노드에서 author.id (GitHub 사용자 ID) 추출. author가 null이면 empty */
    private static Optional<String> authorIdFromCommit(JsonNode commit) {
        JsonNode author = commit.path("author");
        if (author.isMissingNode() || author.isNull()) return Optional.empty();
        if (author.has("id") && !author.get("id").isNull()) {
            return Optional.of(String.valueOf(author.get("id").asInt()));
        }
        return Optional.empty();
    }

    /**
     * repo_url에서 owner와 repo 추출.
     * 지원 형식: https://github.com/owner/repo, https://github.com/owner/repo/, owner/repo
     */
    public static OwnerRepo parseOwnerRepo(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) return null;
        String s = repoUrl.trim();
        if (s.startsWith("https://github.com/")) {
            s = s.substring("https://github.com/".length());
        } else if (s.startsWith("http://github.com/")) {
            s = s.substring("http://github.com/".length());
        } else if (s.startsWith("git@github.com:")) {
            s = s.substring("git@github.com:".length());
        }
        if (s.endsWith(".git")) s = s.substring(0, s.length() - 4);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        int slash = s.indexOf('/');
        if (slash <= 0 || slash == s.length() - 1) return null;
        return new OwnerRepo(s.substring(0, slash), s.substring(slash + 1));
    }

    public record OwnerRepo(String owner, String repo) {}
}
