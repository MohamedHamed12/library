# DDD Library GitHub Issue Importer

This package imports the 20 learning issues from `ddd-library-20-learning-issues.md` into a GitHub repository.

## Requirements

- Node.js 18 or newer.
- A GitHub repository with Issues enabled.
- For execution, a fine-grained personal access token that can access the repository and has **Issues: Read and write** permission.

No npm packages are required.

## Files

- `create-github-issues.mjs`: the importer.
- `ddd-library-20-learning-issues.md`: the source backlog.

## 1. Preview without changing GitHub

PowerShell:

```powershell
node .\create-github-issues.mjs `
  --repo YOUR_GITHUB_USER/library `
  --file ".\ddd-library-20-learning-issues.md"
```

The script is dry-run by default. It prints the parsed titles, labels, and dependency numbers.

## 2. Create the issues

PowerShell:

```powershell
$env:GITHUB_TOKEN = "github_pat_REPLACE_ME"

node .\create-github-issues.mjs `
  --repo YOUR_GITHUB_USER/library `
  --file ".\ddd-library-20-learning-issues.md" `
  --assignee YOUR_GITHUB_USER `
  --execute

Remove-Item Env:GITHUB_TOKEN
```

Bash:

```bash
export GITHUB_TOKEN="github_pat_REPLACE_ME"

node ./create-github-issues.mjs \
  --repo YOUR_GITHUB_USER/library \
  --file ./ddd-library-20-learning-issues.md \
  --assignee YOUR_GITHUB_USER \
  --execute

unset GITHUB_TOKEN
```

## Behavior

The script:

- parses headings in the form `# Issue N - Title`;
- creates missing suggested labels;
- adds the `learning-backlog` tracking label;
- creates issues serially in backlog order;
- converts backlog dependencies into actual GitHub issue references;
- creates native GitHub `blocked by` dependencies when available;
- leaves Markdown dependency links in issue bodies as a fallback;
- adds an invisible import marker to each issue;
- detects previously imported issues and skips them on later runs;
- waits 1.1 seconds between mutating API requests by default;
- retries rate-limit responses using GitHub response headers.

## Useful options

```text
--start 5 --end 10          Import only backlog issues 5 through 10
--update-existing           Rewrite already imported issue bodies and labels
--no-native-dependencies    Keep only Markdown dependency references
--no-create-labels          Do not create missing repository labels
--milestone 2               Assign GitHub milestone number 2
--assignee user1,user2      Assign multiple users
--delay-ms 1500             Change the delay between write requests
```

Run this for the complete option list:

```powershell
node .\create-github-issues.mjs --help
```

## Rerun behavior

Rerunning the same command is safe while imported issues keep the `learning-backlog` label and the invisible import marker. Existing imported issues are skipped unless `--update-existing` is supplied.
