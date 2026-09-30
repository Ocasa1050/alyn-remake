---
name: GitHub push authentication
description: In this workspace, GitHub git pushes may require the x-access-token URL form.
---

When pushing to GitHub from this workspace, the PAT-based URL form `https://x-access-token:<token>@github.com/<owner>/<repo>.git` has worked when an Authorization bearer extraheader was rejected.

**Why:** GitHub rejected the bearer-header attempt but accepted the standard git HTTPS token form.

**How to apply:** Keep the token out of logs and command output; use the approved `GITHUB_PERSONAL_ACCESS_TOKEN` secret only, and verify the remote branch hash after pushing.