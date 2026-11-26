#!/usr/bin/env python3
"""
Build the standalone dialogue-only dex (assets/dialogue.dex).

Compiles only the dialogue sources (UpdateDialogue, SecureLinkConfig and
the generated bridge) and runs R8 on them, so every dialogue class name
is obfuscated while the generated bridge entry point keeps its generated
name (see app/proguard-generated.pro). The in-app Generate button copies
this dex into the output zip, and the cloud workflow ships the same zip
as a build artifact.

Requires: JDK 17 (javac/java on PATH or JAVA_HOME), Android SDK with
platforms;android-34 and build-tools;34.0.0 (ANDROID_HOME env or
sdk.dir in local.properties).
"""

import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA_SRC = ROOT / "app/src/main/java"
OUT_DEX = ROOT / "app/src/main/assets/dialogue.dex"


def find_sdk() -> Path:
    for env in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        if os.environ.get(env):
            return Path(os.environ[env])
    local_props = ROOT / "local.properties"
    if local_props.is_file():
        for line in local_props.read_text().splitlines():
            if line.startswith("sdk.dir="):
                return Path(line.split("=", 1)[1].strip().replace("\\:", ":"))
    sys.exit("Android SDK not found: set ANDROID_HOME or sdk.dir in local.properties")


def java_tool(name: str) -> str:
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidate = Path(java_home) / "bin" / name
        if candidate.is_file():
            return str(candidate)
    return name


def run(cmd):
    print("+", " ".join(str(c) for c in cmd))
    subprocess.run([str(c) for c in cmd], check=True)


def main():
    sdk = find_sdk()
    android_jar = sdk / "platforms/android-34/android.jar"
    d8_jar = sdk / "build-tools/34.0.0/lib/d8.jar"
    for required in (android_jar, d8_jar):
        if not required.is_file():
            sys.exit(f"Missing SDK component: {required}")

    manifest = ROOT / "generated/bridge-manifest.txt"
    if not manifest.is_file():
        sys.exit("generated/bridge-manifest.txt missing - run generate_secure_link.py first")
    bridge_rel = manifest.read_text().strip()
    bridge_src = ROOT / bridge_rel
    if not bridge_src.is_file():
        sys.exit(f"Generated bridge not found: {bridge_rel}")

    sources = [
        JAVA_SRC / "com/error/updatedialogue/UpdateDialogue.java",
        JAVA_SRC / "com/error/updatedialogue/SecureLinkConfig.java",
        bridge_src,
    ]

    work = ROOT / "build/dialogue-dex"
    classes_dir = work / "classes"
    dex_dir = work / "dex"
    classes_dir.mkdir(parents=True, exist_ok=True)
    dex_dir.mkdir(parents=True, exist_ok=True)
    for stale in classes_dir.rglob("*.class"):
        stale.unlink()

    run(
        [
            java_tool("javac"),
            "-source", "8",
            "-target", "8",
            "-classpath", android_jar,
            "-d", classes_dir,
            *sources,
        ]
    )
    class_files = sorted(str(p) for p in classes_dir.rglob("*.class"))
    if not class_files:
        sys.exit("javac produced no class files")

    run(
        [
            java_tool("java"),
            "-cp", d8_jar,
            "com.android.tools.r8.R8",
            "--release",
            "--min-api", "21",
            "--lib", android_jar,
            "--pg-conf", ROOT / "app/proguard-generated.pro",
            "--pg-conf", ROOT / "app/proguard-dialogue-dex.pro",
            "--output", dex_dir,
            *class_files,
        ]
    )
    built = dex_dir / "classes.dex"
    if not built.is_file():
        sys.exit("R8 did not produce classes.dex")
    OUT_DEX.parent.mkdir(parents=True, exist_ok=True)
    OUT_DEX.write_bytes(built.read_bytes())
    print(f"Standalone dialogue dex written: {OUT_DEX} ({OUT_DEX.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
