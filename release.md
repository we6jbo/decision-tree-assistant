<!-- MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json -->
2) GITHUB-SAFE DIRECTIONS (add to repo)

Create a file RELEASE_NOTES.md (or BUILDING.md) in your repo with the content below (no secrets):

# Decision Tree Assistant – Build & Distribute

This doc explains how to build the mobile APK via GitHub Actions and publish a new test build for phones using DeployGate.

## Prerequisites
- Repo: `we6jbo/decision-tree-assistant`
- Branch: `main`
- Workflow: `.github/workflows/android.yml`
- GitHub CLI installed (`gh`)

## Build steps

1. Make changes in the `mobile/` module (and `wear/` if present).  
   Bump `versionCode` and `versionName` in each module’s `build.gradle.kts` when appropriate.

2. Commit & push:
   ```bash
   git add -A
   git commit -m "feat: <short description>"
   git push


Monitor CI:

export GH_REPO=we6jbo/decision-tree-assistant
gh run list --workflow android.yml --limit 1 --repo "$GH_REPO"
export RUN_ID=$(gh run list --workflow android.yml --limit 1 --json databaseId --repo "$GH_REPO" -q '.[0].databaseId')
gh run view "$RUN_ID" --log --repo "$GH_REPO"


Download artifacts (when the run succeeds):

mkdir -p ~/Downloads/dt-artifacts
gh run download "$RUN_ID" \
  --name android-apks \
  --dir ~/Downloads/dt-artifacts \
  --repo "$GH_REPO"


Expected outputs:

~/Downloads/dt-artifacts/mobile-debug.apk

~/Downloads/dt-artifacts/wear-debug.apk (when the wear module is enabled)

Distribute with DeployGate:

Open the DeployGate app page for the package.

Upload the mobile-debug.apk.

On the phone, install/update via the DeployGate app.

Notes

The CI builds debug variants by default. When release signing is added, we’ll document a :mobile:assembleRelease flow and a secure keystore setup.

For watches, distribution can be via DeployGate or Play Internal Testing. ADB is also an option during development.


Commit it:
```bash
cd ~/decision-tree-assistant
git add RELEASE_NOTES.md
git commit -m "docs: add build & distribution instructions"
git push
