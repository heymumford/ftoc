# Fixing a Bug in FTOC

Use this prompt template to help Copilot fix bugs in FTOC.

## Template

```
@workspace
I'm trying to fix a bug in the FTOC project:

Issue: [DESCRIBE_ISSUE]

Steps to reproduce:
1. [STEP_1]
2. [STEP_2]
3. [STEP_3]

Expected behavior:
[EXPECTED_BEHAVIOR]

Actual behavior:
[ACTUAL_BEHAVIOR]

Relevant files:
- [FILE_PATH_1]
- [FILE_PATH_2]

Can you:
1. Analyze the code to find the root cause
2. Propose a fix that follows the project's code patterns
3. Add or update tests to verify the fix
4. Make sure the fix doesn't break existing functionality
```

## Example

```
@workspace
I'm trying to fix a bug in the FTOC project:

Issue: Tag filtering doesn't work with tags that have hyphens

Steps to reproduce:
1. Create a feature file with a tag like "@api-test"
2. Run ftoc with --tags "api-test"
3. Check the output

Expected behavior:
The output should include only scenarios with the @api-test tag

Actual behavior:
No scenarios are included in the output, even though they have the @api-test tag

Relevant files:
- src/main/java/com/heymumford/ftoc/FtocUtility.java
- src/main/java/com/heymumford/ftoc/formatter/TocFormatter.java

Can you:
1. Analyze the code to find the root cause
2. Propose a fix that follows the project's code patterns
3. Add or update tests to verify the fix
4. Make sure the fix doesn't break existing functionality
```