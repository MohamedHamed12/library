#!/usr/bin/env node

import { readFile } from 'node:fs/promises';
import process from 'node:process';

const DEFAULT_API_VERSION = '2026-03-10';
const DEFAULT_TRACKING_LABEL = 'learning-backlog';
const DEFAULT_MUTATION_DELAY_MS = 1100;

const LABEL_DEFINITIONS = {
  'learning-backlog': ['5319e7', 'Imported DDD Library learning backlog issue'],
  'learning/java': ['b07219', 'Java learning objective'],
  'learning/spring': ['6f42c1', 'Spring learning objective'],
  'learning/ddd': ['8a2be2', 'Domain-driven design learning objective'],
  'domain-model': ['d4c5f9', 'Domain model work'],
  'application-layer': ['0e8a16', 'Application layer work'],
  infrastructure: ['1d76db', 'Infrastructure work'],
  cqrs: ['fbca04', 'CQRS and read-model work'],
  events: ['c5def5', 'Domain or integration events'],
  testing: ['bfdadc', 'Testing work'],
  architecture: ['0052cc', 'Architecture work'],
  beginner: ['c2e0c6', 'Beginner difficulty'],
  intermediate: ['fef2c0', 'Intermediate difficulty'],
  advanced: ['f9d0c4', 'Advanced difficulty'],
};

function printHelp() {
  console.log(`
Create GitHub issues from the DDD Library learning-backlog Markdown file.

Usage:
  node create-github-issues.mjs --repo MohamedHamed12/library --file /workspaces/library/ddd-library-20-learning-issues.md --execute

  node create-github-issues.mjs --repo OWNER/REPO --file BACKLOG.md --execute

Safety:
  The default mode is a dry run. Add --execute to modify GitHub.

Required for --execute:
  GITHUB_TOKEN   Fine-grained token with repository Issues: write permission.

Options:
  --repo OWNER/REPO             Target repository. Defaults to GITHUB_REPOSITORY.
  --file PATH                   Markdown backlog file.
  --execute                     Create/update data in GitHub.
  --start NUMBER                First backlog issue to process. Default: 1.
  --end NUMBER                  Last backlog issue to process. Default: all.
  --assignee USER[,USER]        Assign one or more GitHub users.
  --milestone NUMBER            GitHub milestone number.
  --tracking-label NAME         Idempotency label. Default: learning-backlog.
  --update-existing             Rewrite bodies/labels of previously imported issues.
  --no-create-labels            Do not create missing labels.
  --no-native-dependencies      Do not create GitHub native blocked-by relationships.
  --delay-ms NUMBER             Delay between mutating API calls. Default: 1100.
  --api-version YYYY-MM-DD      GitHub REST API version. Default: 2026-03-10.
  --api-url URL                  API base URL. Defaults to GITHUB_API_URL or GitHub.com.
  --help                        Show this help.

Examples:
  node create-github-issues.mjs \\
    --repo my-user/library \\
    --file "ddd-library-20-learning-issues.md"

  GITHUB_TOKEN=github_pat_xxx node create-github-issues.mjs \\
    --repo my-user/library \\
    --file "ddd-library-20-learning-issues.md" \\
    --assignee my-user \\
    --execute
`);
}

function parseArgs(argv) {
  const args = {
    repo: process.env.GITHUB_REPOSITORY ?? '',
    file: '',
    execute: false,
    start: 1,
    end: Number.POSITIVE_INFINITY,
    assignees: [],
    milestone: undefined,
    trackingLabel: DEFAULT_TRACKING_LABEL,
    updateExisting: false,
    createLabels: true,
    nativeDependencies: true,
    delayMs: DEFAULT_MUTATION_DELAY_MS,
    apiVersion: DEFAULT_API_VERSION,
    apiUrl: process.env.GITHUB_API_URL ?? 'https://api.github.com',
    help: false,
  };

  for (let index = 0; index < argv.length; index += 1) {
    const option = argv[index];
    const nextValue = () => {
      const value = argv[index + 1];
      if (!value || value.startsWith('--')) {
        throw new Error(`Missing value for ${option}`);
      }
      index += 1;
      return value;
    };

    switch (option) {
      case '--repo':
        args.repo = nextValue();
        break;
      case '--file':
        args.file = nextValue();
        break;
      case '--execute':
        args.execute = true;
        break;
      case '--dry-run':
        args.execute = false;
        break;
      case '--start':
        args.start = parsePositiveInteger(nextValue(), '--start');
        break;
      case '--end':
        args.end = parsePositiveInteger(nextValue(), '--end');
        break;
      case '--assignee':
        args.assignees.push(
          ...nextValue()
            .split(',')
            .map((value) => value.trim().replace(/^@/, ''))
            .filter(Boolean),
        );
        break;
      case '--milestone':
        args.milestone = parsePositiveInteger(nextValue(), '--milestone');
        break;
      case '--tracking-label':
        args.trackingLabel = nextValue().trim();
        break;
      case '--update-existing':
        args.updateExisting = true;
        break;
      case '--no-create-labels':
        args.createLabels = false;
        break;
      case '--no-native-dependencies':
        args.nativeDependencies = false;
        break;
      case '--delay-ms':
        args.delayMs = parseNonNegativeInteger(nextValue(), '--delay-ms');
        break;
      case '--api-version':
        args.apiVersion = nextValue();
        break;
      case '--api-url':
        args.apiUrl = nextValue().replace(/\/$/, '');
        break;
      case '--help':
      case '-h':
        args.help = true;
        break;
      default:
        throw new Error(`Unknown option: ${option}`);
    }
  }

  args.assignees = [...new Set(args.assignees)];
  return args;
}

function parsePositiveInteger(value, option) {
  const parsed = Number.parseInt(value, 10);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error(`${option} must be a positive integer.`);
  }
  return parsed;
}

function parseNonNegativeInteger(value, option) {
  const parsed = Number.parseInt(value, 10);
  if (!Number.isInteger(parsed) || parsed < 0) {
    throw new Error(`${option} must be a non-negative integer.`);
  }
  return parsed;
}

function parseRepository(value) {
  const match = value.trim().match(/^([^/\s]+)\/([^/\s]+)$/);
  if (!match) {
    throw new Error('--repo must use OWNER/REPO format.');
  }
  return { owner: match[1], repo: match[2].replace(/\.git$/i, '') };
}

function parseBacklog(markdown) {
  const headingPattern = /^# Issue\s+(\d+)\s+-\s+(.+)$/gm;
  const matches = [...markdown.matchAll(headingPattern)];

  if (matches.length === 0) {
    throw new Error('No headings matching "# Issue N - Title" were found.');
  }

  const issues = matches.map((match, index) => {
    const number = Number.parseInt(match[1], 10);
    const title = match[2].trim();
    const bodyStart = match.index + match[0].length;
    const nextStart = matches[index + 1]?.index ?? markdown.length;
    let body = markdown.slice(bodyStart, nextStart).trim();

    if (index === matches.length - 1) {
      body = body.split(/^# Recommended Pull Request Template for Every Issue$/m)[0].trim();
    }

    body = body.replace(/(?:\n|^)---\s*$/g, '').trim();

    const labels = parseSuggestedLabels(body);
    const dependencyText = findMetadataValue(body, 'Depends on') ?? 'None';
    const dependencies = parseDependencies(dependencyText, number);

    return {
      backlogNumber: number,
      title,
      body,
      labels,
      dependencyText,
      dependencies,
    };
  });

  const numbers = issues.map((issue) => issue.backlogNumber);
  if (new Set(numbers).size !== numbers.length) {
    throw new Error('Duplicate issue numbers were found in the Markdown file.');
  }

  return issues.sort((left, right) => left.backlogNumber - right.backlogNumber);
}

function parseSuggestedLabels(body) {
  const value = findMetadataValue(body, 'Suggested labels');
  if (!value) {
    return [];
  }

  const backtickLabels = [...value.matchAll(/`([^`]+)`/g)].map((match) => match[1].trim());
  if (backtickLabels.length > 0) {
    return [...new Set(backtickLabels.filter(Boolean))];
  }

  return [...new Set(value.split(',').map((label) => label.trim()).filter(Boolean))];
}

function findMetadataValue(body, name) {
  const escapedName = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = body.match(new RegExp(`^\\*\\*${escapedName}:\\*\\*\\s*(.+)$`, 'mi'));
  return match?.[1]?.trim();
}

function parseDependencies(text, currentIssueNumber) {
  if (/^none\.?$/i.test(text.trim())) {
    return [];
  }

  if (/\ball\b/i.test(text) || /earlier backlog/i.test(text)) {
    return range(1, currentIssueNumber - 1);
  }

  const dependencies = new Set();
  let remaining = text;

  for (const match of text.matchAll(/(\d+)\s*[-–—]\s*(\d+)/g)) {
    const start = Number.parseInt(match[1], 10);
    const end = Number.parseInt(match[2], 10);
    for (const number of range(Math.min(start, end), Math.max(start, end))) {
      dependencies.add(number);
    }
    remaining = remaining.replace(match[0], ' ');
  }

  for (const match of remaining.matchAll(/\d+/g)) {
    dependencies.add(Number.parseInt(match[0], 10));
  }

  return [...dependencies]
    .filter((number) => number > 0 && number < currentIssueNumber)
    .sort((left, right) => left - right);
}

function range(start, end) {
  if (end < start) {
    return [];
  }
  return Array.from({ length: end - start + 1 }, (_, index) => start + index);
}

function renderIssueBody(issue, issueMap) {
  const dependencyReferences = issue.dependencies.map((dependencyNumber) => {
    const dependency = issueMap.get(dependencyNumber);
    return dependency ? `#${dependency.number}` : `backlog issue ${dependencyNumber}`;
  });

  const replacement = dependencyReferences.length > 0 ? dependencyReferences.join(', ') : 'None';
  const dependsLine = /^\*\*Depends on:\*\*\s*.*$/mi;
  let body = issue.body;

  if (dependsLine.test(body)) {
    body = body.replace(dependsLine, `**Depends on:** ${replacement}`);
  } else {
    body = `**Depends on:** ${replacement}\n\n${body}`;
  }

  return `${body.trim()}\n\n---\n\n<!-- ddd-learning-backlog-issue:${issue.backlogNumber} -->\n`;
}

function makeMarkerRegex() {
  return /<!--\s*ddd-learning-backlog-issue:(\d+)\s*-->/i;
}

function deterministicLabelDefinition(name) {
  let hash = 2166136261;
  for (const character of name) {
    hash ^= character.codePointAt(0);
    hash = Math.imul(hash, 16777619);
  }
  const color = (hash >>> 0).toString(16).padStart(8, '0').slice(0, 6);
  return [color, `Learning backlog label: ${name}`.slice(0, 100)];
}

function sleep(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

class GitHubClient {
  constructor({ token, owner, repo, apiVersion, apiUrl, delayMs }) {
    this.token = token;
    this.owner = owner;
    this.repo = repo;
    this.apiVersion = apiVersion;
    this.apiUrl = apiUrl.replace(/\/$/, '');
    this.delayMs = delayMs;
    this.lastMutationAt = 0;
  }

  async request(method, path, body, options = {}) {
    const allowedStatuses = new Set(options.allowedStatuses ?? []);
    const isMutation = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method);

    if (isMutation) {
      await this.paceMutation();
    }

    const url = path.startsWith('http') ? path : `${this.apiUrl}${path}`;
    let lastResponse;
    let lastPayload;

    for (let attempt = 0; attempt < 5; attempt += 1) {
      const response = await fetch(url, {
        method,
        redirect: 'follow',
        headers: {
          Accept: 'application/vnd.github+json',
          Authorization: `Bearer ${this.token}`,
          'X-GitHub-Api-Version': this.apiVersion,
          'User-Agent': 'ddd-library-learning-backlog-importer',
          ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
      });

      const text = await response.text();
      let payload = null;
      if (text) {
        try {
          payload = JSON.parse(text);
        } catch {
          payload = text;
        }
      }

      lastResponse = response;
      lastPayload = payload;

      if (response.ok || allowedStatuses.has(response.status)) {
        if (isMutation) {
          this.lastMutationAt = Date.now();
        }
        return { response, data: payload };
      }

      if (![403, 429].includes(response.status) || attempt === 4) {
        break;
      }

      const waitMs = this.calculateRateLimitWait(response, attempt);
      console.warn(`GitHub rate limit response (${response.status}); retrying after ${Math.ceil(waitMs / 1000)}s.`);
      await sleep(waitMs);
    }

    const message =
      typeof lastPayload === 'object' && lastPayload?.message
        ? lastPayload.message
        : typeof lastPayload === 'string'
          ? lastPayload
          : 'Unknown GitHub API error';
    const acceptedPermissions = lastResponse?.headers.get('x-accepted-github-permissions');
    const permissionHint = acceptedPermissions ? ` Required permissions: ${acceptedPermissions}.` : '';

    throw new Error(`${method} ${path} failed with ${lastResponse?.status}: ${message}.${permissionHint}`);
  }

  async paceMutation() {
    const elapsed = Date.now() - this.lastMutationAt;
    const remaining = this.delayMs - elapsed;
    if (this.lastMutationAt > 0 && remaining > 0) {
      await sleep(remaining);
    }
  }

  calculateRateLimitWait(response, attempt) {
    const retryAfter = Number.parseInt(response.headers.get('retry-after') ?? '', 10);
    if (Number.isInteger(retryAfter) && retryAfter >= 0) {
      return (retryAfter + 1) * 1000;
    }

    const remaining = response.headers.get('x-ratelimit-remaining');
    const resetAt = Number.parseInt(response.headers.get('x-ratelimit-reset') ?? '', 10);
    if (remaining === '0' && Number.isInteger(resetAt)) {
      return Math.max(1000, resetAt * 1000 - Date.now() + 1000);
    }

    return Math.min(60_000 * 2 ** attempt, 15 * 60_000);
  }

  repositoryPath(suffix = '') {
    return `/repos/${encodeURIComponent(this.owner)}/${encodeURIComponent(this.repo)}${suffix}`;
  }
}

async function listAllPages(client, path, itemLimit = Number.POSITIVE_INFINITY) {
  const items = [];
  let page = 1;

  while (items.length < itemLimit) {
    const separator = path.includes('?') ? '&' : '?';
    const { data } = await client.request('GET', `${path}${separator}per_page=100&page=${page}`);
    if (!Array.isArray(data)) {
      throw new Error(`Expected an array from ${path}.`);
    }

    items.push(...data);
    if (data.length < 100) {
      break;
    }
    page += 1;
  }

  return items.slice(0, itemLimit);
}

async function ensureLabels(client, labelNames) {
  const existing = await listAllPages(client, client.repositoryPath('/labels'));
  const existingNames = new Set(existing.map((label) => label.name.toLowerCase()));

  for (const name of labelNames) {
    if (existingNames.has(name.toLowerCase())) {
      continue;
    }

    const [color, description] = LABEL_DEFINITIONS[name] ?? deterministicLabelDefinition(name);
    await client.request('POST', client.repositoryPath('/labels'), { name, color, description });
    existingNames.add(name.toLowerCase());
    console.log(`Created label: ${name}`);
  }
}

async function loadExistingImportedIssues(client, trackingLabel) {
  const encodedLabel = encodeURIComponent(trackingLabel);
  const candidates = await listAllPages(
    client,
    client.repositoryPath(`/issues?state=all&labels=${encodedLabel}`),
  );

  const imported = new Map();
  const marker = makeMarkerRegex();

  for (const candidate of candidates) {
    if (candidate.pull_request) {
      continue;
    }
    const match = candidate.body?.match(marker);
    if (!match) {
      continue;
    }
    imported.set(Number.parseInt(match[1], 10), candidate);
  }

  return imported;
}

async function createOrUpdateIssues({ client, issues, existingIssues, args }) {
  const issueMap = new Map(existingIssues);

  for (const issue of issues) {
    const existing = issueMap.get(issue.backlogNumber);
    const labels = [...new Set([args.trackingLabel, ...issue.labels])];
    const body = renderIssueBody(issue, issueMap);
    const requestBody = {
      title: issue.title,
      body,
      labels,
      ...(args.assignees.length > 0 ? { assignees: args.assignees } : {}),
      ...(args.milestone !== undefined ? { milestone: args.milestone } : {}),
    };

    if (existing) {
      console.log(`Exists: backlog ${issue.backlogNumber} -> #${existing.number} ${existing.title}`);
      if (args.updateExisting) {
        const { data } = await client.request(
          'PATCH',
          client.repositoryPath(`/issues/${existing.number}`),
          requestBody,
        );
        issueMap.set(issue.backlogNumber, data);
        console.log(`Updated: #${data.number} ${data.title}`);
      }
      continue;
    }

    const { data } = await client.request('POST', client.repositoryPath('/issues'), requestBody);
    issueMap.set(issue.backlogNumber, data);
    console.log(`Created: backlog ${issue.backlogNumber} -> #${data.number} ${data.title}`);
  }

  return issueMap;
}

async function createNativeDependencies({ client, issues, issueMap }) {
  let nativeDependenciesAvailable = true;

  for (const issue of issues) {
    if (!nativeDependenciesAvailable || issue.dependencies.length === 0) {
      continue;
    }

    const current = issueMap.get(issue.backlogNumber);
    if (!current) {
      console.warn(`Skipping native dependencies for backlog ${issue.backlogNumber}; issue was not imported.`);
      continue;
    }

    const path = client.repositoryPath(`/issues/${current.number}/dependencies/blocked_by`);
    const { response, data } = await client.request('GET', path, undefined, {
      allowedStatuses: [404, 410],
    });

    if ([404, 410].includes(response.status)) {
      nativeDependenciesAvailable = false;
      console.warn('Native issue dependencies are unavailable for this repository; Markdown dependency links remain in the bodies.');
      break;
    }

    const existingIds = new Set((Array.isArray(data) ? data : []).map((dependency) => dependency.id));

    for (const dependencyNumber of issue.dependencies) {
      const blocker = issueMap.get(dependencyNumber);
      if (!blocker) {
        console.warn(`Cannot add dependency for backlog ${issue.backlogNumber}: backlog ${dependencyNumber} was not imported.`);
        continue;
      }
      if (existingIds.has(blocker.id)) {
        continue;
      }

      const result = await client.request('POST', path, { issue_id: blocker.id }, {
        allowedStatuses: [404, 410, 422],
      });

      if ([404, 410].includes(result.response.status)) {
        nativeDependenciesAvailable = false;
        console.warn('Native issue dependencies became unavailable; Markdown dependency links remain in the bodies.');
        break;
      }

      if (result.response.status === 422) {
        const message = result.data?.message ?? 'validation failed';
        console.warn(`Could not add #${blocker.number} as a native blocker of #${current.number}: ${message}`);
        continue;
      }

      existingIds.add(blocker.id);
      console.log(`Dependency: #${current.number} is blocked by #${blocker.number}`);
    }
  }
}

function printDryRun({ args, allIssues, selectedIssues }) {
  console.log(`Mode: dry run (add --execute to create issues)`);
  console.log(`Repository: ${args.repo}`);
  console.log(`Backlog file: ${args.file}`);
  console.log(`Parsed issues: ${allIssues.length}`);
  console.log(`Selected issues: ${selectedIssues.length}\n`);

  for (const issue of selectedIssues) {
    const dependencyText = issue.dependencies.length > 0 ? issue.dependencies.join(', ') : 'none';
    const labels = [args.trackingLabel, ...issue.labels].join(', ');
    console.log(`${String(issue.backlogNumber).padStart(2, '0')}. ${issue.title}`);
    console.log(`    labels: ${labels}`);
    console.log(`    blocked by backlog: ${dependencyText}`);
  }
}

async function main() {
  const args = parseArgs(process.argv.slice(2));

  if (args.help) {
    printHelp();
    return;
  }
  if (!args.repo) {
    throw new Error('--repo is required unless GITHUB_REPOSITORY is set.');
  }
  if (!args.file) {
    throw new Error('--file is required.');
  }
  if (args.start > args.end) {
    throw new Error('--start cannot be greater than --end.');
  }

  const markdown = await readFile(args.file, 'utf8');
  const allIssues = parseBacklog(markdown);
  const selectedIssues = allIssues.filter(
    (issue) => issue.backlogNumber >= args.start && issue.backlogNumber <= args.end,
  );

  if (selectedIssues.length === 0) {
    throw new Error('No issues match the selected --start/--end range.');
  }

  if (!args.execute) {
    printDryRun({ args, allIssues, selectedIssues });
    return;
  }

  const token = process.env.GITHUB_TOKEN;
  if (!token) {
    throw new Error('GITHUB_TOKEN is required with --execute.');
  }

  const { owner, repo } = parseRepository(args.repo);
  const client = new GitHubClient({
    token,
    owner,
    repo,
    apiVersion: args.apiVersion,
    apiUrl: args.apiUrl,
    delayMs: args.delayMs,
  });

  const allLabels = new Set([args.trackingLabel]);
  for (const issue of selectedIssues) {
    for (const label of issue.labels) {
      allLabels.add(label);
    }
  }

  if (args.createLabels) {
    await ensureLabels(client, [...allLabels]);
  }

  const existingIssues = await loadExistingImportedIssues(client, args.trackingLabel);
  const issueMap = await createOrUpdateIssues({
    client,
    issues: selectedIssues,
    existingIssues,
    args,
  });

  if (args.nativeDependencies) {
    await createNativeDependencies({ client, issues: selectedIssues, issueMap });
  }

  console.log('\nImport complete.');
  for (const issue of selectedIssues) {
    const created = issueMap.get(issue.backlogNumber);
    if (created) {
      console.log(`Backlog ${issue.backlogNumber}: ${created.html_url}`);
    }
  }
}

main().catch((error) => {
  console.error(`Error: ${error.message}`);
  process.exitCode = 1;
});
