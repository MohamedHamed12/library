# Continuous Integration and Supply-Chain Policy

GitHub Actions is the authoritative CI system for this repository. The legacy CircleCI configuration has been removed.

## Local verification

Run the same full Maven verification command used by CI:

```bash
./mvnw clean verify
```

Docker must be available because the integration-test suite starts PostgreSQL with Testcontainers.

## Pull request quality gates

The CI workflow runs on pull requests targeting `master` and on pushes to `master`.

| Check | Purpose |
|---|---|
| Build | Compiles and packages the application on Java 25. |
| Unit tests | Runs the unit-test suite. |
| Integration tests | Runs `./mvnw clean verify`, including PostgreSQL/Testcontainers integration tests, Flyway migration verification, and JaCoCo coverage generation, then generates the CycloneDX SBOM. |
| Architecture tests | Runs the ArchUnit architecture rules as a dedicated check. |
| Dependency review | Fails pull requests that introduce dependencies with high-or-higher known vulnerabilities. |
| Container scan | Builds `Dockerfile.build` and scans the resulting application image for high-or-higher vulnerabilities. |
| CodeQL | Performs Java static security analysis on pull requests, `master`, and a weekly schedule. |

Successful full verification uploads test reports, JaCoCo coverage output, and CycloneDX `bom.json` / `bom.xml` artifacts.

GitHub native secret scanning is the repository-level secret detection control. CI configuration must not contain plaintext credentials, and workflow steps must not print secrets or sensitive environment values.

## Supply-chain policy

- GitHub Actions are pinned to full commit SHAs. The release version is recorded in an inline comment for reviewability.
- GitHub-owned actions are preferred. A third-party action is allowed only when it provides a required capability that GitHub Actions does not provide directly; it must be pinned to a reviewed commit SHA.
- Dependabot checks Maven and GitHub Actions dependencies weekly. Dependabot updates to pinned Actions must be reviewed like application dependency changes.
- Maven dependencies and plugins use repository-managed versions or explicitly pinned versions. The CycloneDX plugin is pinned because it is part of the build supply chain.
- Trusted CI must not install tools through `curl | bash`, process substitution from remote scripts, or equivalent unaudited execution patterns.
- Workflow permissions follow least privilege. Jobs receive only the `GITHUB_TOKEN` permissions they require.
- CI must not depend on long-lived credentials for build, test, coverage, SBOM generation, or container scanning.

## Branch protection

For `master`, configure branch protection or a repository ruleset to require the build, unit test, integration test, architecture test, dependency review, container scan, and CodeQL checks before merge.
