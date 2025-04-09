# Setting Up GitHub Secrets for FTOC

To enable all features of the CI/CD pipeline, you need to set up the following GitHub secrets:

## Required Secrets

### `GITHUB_TOKEN`

This is automatically provided by GitHub and does not need to be set manually.

## Optional Secrets

### `NVD_API_KEY`

The NVD (National Vulnerability Database) API key is used by the OWASP Dependency Check to scan dependencies for security vulnerabilities.

1. **Get an API key**: Register for a free API key at [https://nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key)
2. **Add the secret**:
   - Go to your GitHub repository
   - Click on "Settings" → "Secrets and variables" → "Actions"
   - Click "New repository secret"
   - Name: `NVD_API_KEY`
   - Value: Your API key from NVD
   - Click "Add secret"

### `SLACK_WEBHOOK_URL`

If you want to enable Slack notifications for CI/CD events:

1. **Create a Slack app** in your workspace
2. **Add an Incoming Webhook** to the app
3. **Add the secret**:
   - Go to your GitHub repository
   - Click on "Settings" → "Secrets and variables" → "Actions"
   - Click "New repository secret"
   - Name: `SLACK_WEBHOOK_URL`
   - Value: Your Slack webhook URL
   - Click "Add secret"

## Verifying Secrets

To verify that your secrets are properly set up:

1. Go to your GitHub repository
2. Click on "Actions"
3. Select the "CI/CD Pipeline" workflow
4. Click "Run workflow" and use the dropdown menu
5. Enable the "Debug mode" option
6. Click "Run workflow"

This will run the workflow with extra debugging information, which can help you identify any issues with your secrets.