
---

# 3) `chatgpt.md` (paste this file into your repo root)

```markdown
# ChatGPT Playbook

**Purpose**  
This file tells ChatGPT how to help on this project: adding features, fixing builds, capturing lessons learned, and safely referencing any confidential snippets.

---

## How ChatGPT should use this repo

When I share a link to this file (or to `Directions.md`):

1) **Read `Directions.md` first** to follow the standard flow:
   - Add/modify the requested feature (e.g., About screen),
   - Trigger CI and download artifacts,
   - Guide DeployGate upload.

2) If I ask for feature work, suggest minimal diffs (file paths + exact code blocks).  
3) If builds fail, **quote the error lines** and propose a patch in a small PR-style chunk.  
4) Offer to create a succinct commit message and a follow-up task list.

---

## Logging “What went wrong” (Lessons Learned)

Append to this section when we hit snags. Use short entries to help future debugging.

**Template**
YYYY-MM-DD – <one-liner of the issue>

Symptom: <copy exact error>
Root cause: <what was actually wrong>
Fix: <what we changed>
Verification: <how we proved it worked>
Follow-ups: <backlog items, if any>


**Examples**


2025-08-31 – AndroidX not enabled

Symptom: Gradle error about AndroidX on debugRuntimeClasspath.
Root cause: Missing android.useAndroidX=true in gradle.properties.
Fix: Added android.useAndroidX=true.
Verification: CI green; APK artifact generated.
Follow-ups: none.

2025-09-01 – Missing ic_launcher

Symptom: AAPT error: mipmap/ic_launcher not found.
Root cause: App icon resources missing.
Fix: Added res/mipmap-* launcher icons and referenced in manifest.
Verification: CI succeeded; app installed via DeployGate.
Follow-ups: consider adaptive icon variant.


---

## Asking ChatGPT for help – examples

- “Please add an **About** screen; use XML layout; wire a menu item from MainActivity; then trigger CI and guide me to DeployGate.”  
- “CI failed with `ManifestMerger2$MergeFailureException` — read the latest run logs and propose a one-file fix.”  
- “Create a step-by-step checklist to add a Wear module and include a minimal watch tile.”  

---

## Optional: Encrypted snippets (avoid repo secrets!)

> Prefer **not** to commit secrets. If you *must* keep a confidential note (e.g., a device-specific token or a one-off incident number), encrypt it and **never** commit the passphrase.

**Recommended (modern): `age` with passphrase**

Encrypt:
```bash
echo "confidential text here" > secrets.txt
age -p -o secrets.enc secrets.txt
shred -u secrets.txt


Decrypt:

age -d -o secrets.txt secrets.enc
# you'll be prompted for the passphrase (DO NOT COMMIT IT)


Alternative (broadly available): OpenSSL AES-256

Encrypt:

echo "confidential text here" > secrets.txt
openssl enc -aes-256-cbc -pbkdf2 -salt -in secrets.txt -out secrets.enc
shred -u secrets.txt


Decrypt:

openssl enc -d -aes-256-cbc -pbkdf2 -in secrets.enc -out secrets.txt


Git hygiene

Add to .gitignore:

secrets.txt
*.keystore
*.jks


Optional .gitattributes:

*.enc binary


Important: If I say “use passphrase provided at runtime,” ChatGPT should ask me for it during the session; it must not use or store any passphrase in repo content.

Backlog ideas ChatGPT can propose

Convert About UI to Compose.

Add a “Send test heartbeat” button that hits the Pi’s /heartbeat.

Introduce a “Draft tree” screen with local Room DB and a share/export action.

CI job to attach APK to a GitHub Release.

Optional Wear milestone: a simple tile to submit a quick note/step.


---

# 4) Commit both files

```bash
cd ~/decision-tree-assistant
git add Directions.md chatgpt.md
git commit -m "docs: add Directions.md and chatgpt.md for build & ChatGPT workflows"
git push

Anything else to add?

If you want, I can also drop a tiny CI job that attaches the APK to a GitHub Release each time you push a tag (makes it friendly for tools like Obtainium).

When you’re ready for Wear OS OTA installs, I’ll add a Play Internal Testing checklist (no code hosting required, just uploads through Play Console).
