# UpdateDialogue — Locked Link Build

The remote JSON link is locked inside the app at build time. It is never
stored as a plain string, and the app only fetches it after two checks pass:

1. **SHA-256** — the decoded link's hash must match the baked-in hash.
2. **RSA** — the link's `SHA256withRSA` signature must verify against the
   baked-in RSA public key.

Release builds also run **R8** (shrink + obfuscate) and are signed with an
RSA key using APK Signature Scheme v2/v3 (SHA-256 digests).

## Change the link (automatic)

Option A — edit one line in `dialogue.properties`, then rebuild:

```properties
remote.url=https://pastebin.com/raw/YOUR_NEW_ID
```

Option B — GitHub Actions: run the workflow manually and paste the link
into the **remote_url** input.

Option C — command line:

```bash
gradle assembleRelease -PdialogueRemoteUrl="https://pastebin.com/raw/YOUR_NEW_ID"
```

Every build regenerates `SecureLinkConfig.java` automatically
(encoded link + SHA-256 hash + RSA signature). Do not edit that file by hand.

## Signing

- Test builds sign with the debug key automatically.
- Production: add repo secrets `RELEASE_KEYSTORE_BASE64`,
  `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`
  (RSA keystore) and the workflow signs with your key.
- Optional: `RSA_PRIVATE_KEY_PEM` secret signs the locked link with your
  own RSA private key. Without it, each build generates a temporary
  RSA-2048 key pair and deletes the private key after signing — the
  resulting APK still verifies correctly.

## Generated MainActivity call

Every generate also creates a fresh bootstrap-style bridge class with
randomized framework-neutral names (package, class and method names all
change each run, e.g. `platform.config.RuntimeBinder.bind(this)`) and
rewrites the `GENERATED-CALL` blocks in `MainActivity` to call it.
`MainActivity` therefore contains no direct dialogue call - only an
ordinary-looking lifecycle/bootstrap call with harmless display/config
profile code around it. R8 obfuscates the dialogue classes behind the
bridge; only the bridge entry points are kept, under their generated
names (`app/proguard-generated.pro`), so the ready-to-paste Java/smali
snippet in `generated/mainactivity-call.txt` stays valid for that build.
Names are neutral bootstrap-style names, not actual Android system
classes. Note the dialogue's own text and JSON key strings still exist
in the dex - this hides the call site, it does not encrypt strings.

## Honest limits

This stops casual extraction and link swapping (plain-text search,
preference edits, string replacement). It is not unbreakable: someone who
patches the verification code itself can still bypass it, like any
client-side check. R8 obfuscation makes that patching harder, not impossible.
