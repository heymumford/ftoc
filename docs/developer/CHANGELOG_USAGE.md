# Automated Changelog Usage

This project uses an automated changelog generation system based on [Conventional Commits](https://www.conventionalcommits.org/) format.

## Generating Changelog Entries

The changelog is automatically generated during the release process. It extracts information from commit messages that follow the Conventional Commits format.

### Local Testing

You can test the changelog generation locally by running:

```bash
.github/scripts/generate-changelog.sh
```

This will generate a changelog entry for commits since the last tag.

## Making a Release

When you create and push a version tag (e.g., `v1.2.3`), the GitHub Actions workflow will:

1. Build the project
2. Generate a changelog entry from commits since the last tag
3. Create a GitHub Release with the changelog as the description
4. Update the main CHANGELOG.md file with the new entry

## Types of Changes

Commits are categorized in the changelog based on their type:

- **Added**: Features (prefix: `feat:`)
- **Fixed**: Bug fixes (prefix: `fix:`)
- **Changed**: Code refactoring, performance improvements (prefix: `refactor:`, `perf:`)
- **Documentation**: Documentation changes (prefix: `docs:`)
- **Security**: Security improvements (prefix: `security:`)

For example, a commit with the message `feat(parser): add support for new tag format` will appear in the changelog under "Added" as "parser: add support for new tag format".

## Best Practices

- Follow the [Conventional Commits](./CONVENTIONAL_COMMITS.md) format for all commit messages
- Use a clear and specific scope in parentheses (e.g., `feat(parser):`, `fix(cli):`)
- Write meaningful commit messages that explain the change
- For breaking changes, add a `!` after the type/scope and include a `BREAKING CHANGE:` footer

## Viewing Changelog

The full project changelog is maintained in the [CHANGELOG.md](../../CHANGELOG.md) file at the root of the repository.