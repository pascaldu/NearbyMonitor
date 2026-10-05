#!/usr/bin/env python3
"""Build Nearby Monitor's offline MAC manufacturer table."""

from __future__ import annotations

import csv
import datetime as dt
import io
import re
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET_DIR = ROOT / "app" / "src" / "main" / "assets" / "manuf"
OUTPUT = ASSET_DIR / "oui_lookup.tsv"
SOURCE_INFO = ASSET_DIR / "source.txt"
LICENSE_FILE = ASSET_DIR / "LICENSE-macdb.txt"
DATABASE_URL = "https://raw.githubusercontent.com/WH-2099/macdb/main/mac.csv"
SOURCE_PAGE = "https://github.com/WH-2099/macdb"
LICENSE_URL = "https://raw.githubusercontent.com/WH-2099/macdb/main/LICENSE"
HEX = re.compile(r"^[0-9A-F]+$")


def download(url: str) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": "NearbyMonitor/0.6 (+offline manufacturer database)", "Accept": "text/plain,text/csv,*/*"})
    with urllib.request.urlopen(request, timeout=120) as response:
        return response.read()


def clean(value: str | None) -> str:
    return (value or "").replace("\t", " ").replace("\r", " ").replace("\n", " ").strip()


def main() -> int:
    ASSET_DIR.mkdir(parents=True, exist_ok=True)
    raw = download(DATABASE_URL)
    text = raw.decode("utf-8-sig", errors="replace")
    reader = csv.DictReader(io.StringIO(text))
    if not reader.fieldnames:
        raise RuntimeError("Le fichier CSV ne contient pas d'en-tête.")
    entries: dict[str, tuple[str, str]] = {}
    for row in reader:
        registry = clean(row.get("registry") or row.get("Registry"))
        assignment = re.sub(r"[^0-9A-F]", "", clean(row.get("assignment") or row.get("Assignment")).upper())
        org = clean(row.get("org_name") or row.get("Organization Name") or row.get("organization_name"))
        if len(assignment) not in (6, 7, 9) or not HEX.fullmatch(assignment) or not org or org.lower() == "ieee registration authority":
            continue
        entries.setdefault(assignment, (registry or "OUI", org))
    if not entries:
        raise RuntimeError("Aucune entrée OUI n'a pu être extraite.")
    with OUTPUT.open("w", encoding="utf-8", newline="\n") as out:
        out.write("# Nearby Monitor offline MAC manufacturer database\n")
        out.write("# prefix<TAB>registry<TAB>manufacturer\n")
        for prefix in sorted(entries, key=lambda item: (len(item), item)):
            registry, org = entries[prefix]
            out.write(f"{prefix}\t{registry}\t{org}\n")
    try:
        LICENSE_FILE.write_text(download(LICENSE_URL).decode("utf-8", errors="replace"), encoding="utf-8")
    except Exception as exc:
        print(f"Avertissement : licence non téléchargée : {exc}")
    now = dt.datetime.now(dt.timezone.utc).isoformat()
    SOURCE_INFO.write_text("\n".join(["Nearby Monitor manufacturer database", f"Generated: {now}", f"Source page: {SOURCE_PAGE}", f"Database URL: {DATABASE_URL}", "Upstream data are derived from IEEE MA-L, MA-M and MA-S registries.", f"Entries: {len(entries)}", ""]), encoding="utf-8")
    print(f"OK : {len(entries)} préfixes écrits dans {OUTPUT}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"ERREUR : {exc}", file=sys.stderr)
        raise SystemExit(2)
