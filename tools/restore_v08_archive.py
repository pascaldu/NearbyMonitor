#!/usr/bin/env python3
"""Reconstruit l'archive complète Nearby Monitor V0.8 depuis les fragments Base64."""

from __future__ import annotations

import base64
import hashlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ARCHIVE_DIR = ROOT / "archive"
OUTPUT = ROOT / "NearbyMonitorV08_GitHub_COMPLETE.zip"

EXPECTED_SHA256 = "74893d6b336ed39b82cba4efca9a6ae8f2c2983e9f1bd598394f5aab1de7a563"
EXPECTED_B64_LENGTH = 83328

parts = sorted(ARCHIVE_DIR.glob("NearbyMonitorV08_GitHub.zip.b64.part*"))

if not parts:
    raise SystemExit("Aucun fragment d'archive trouvé.")

payload = "".join(part.read_text(encoding="ascii").strip() for part in parts)

if len(payload) != EXPECTED_B64_LENGTH:
    raise SystemExit(
        f"Longueur Base64 inattendue : {len(payload)} au lieu de {EXPECTED_B64_LENGTH}."
    )

data = base64.b64decode(payload, validate=True)
digest = hashlib.sha256(data).hexdigest()

if digest != EXPECTED_SHA256:
    raise SystemExit(
        "SHA-256 incorrect :\n"
        f"  obtenu  : {digest}\n"
        f"  attendu : {EXPECTED_SHA256}"
    )

OUTPUT.write_bytes(data)

print(f"Archive reconstruite : {OUTPUT}")
print(f"Taille : {len(data)} octets")
print(f"SHA-256 : {digest}")
print("OK : archive V0.8 vérifiée.")
