# Archive source complète — Nearby Monitor V0.8

Ce dossier contient une copie exacte de l'archive source complète de Nearby Monitor V0.8, encodée en Base64 et découpée en plusieurs fichiers texte afin de pouvoir être conservée dans le dépôt.

## Fichiers

Les fragments sont :

`NearbyMonitorV08_GitHub.zip.b64.part01` à `part08`.

Taille Base64 totale attendue : **83 328 caractères**.

Archive ZIP reconstruite attendue :

`NearbyMonitorV08_GitHub_COMPLETE.zip`

SHA-256 attendu :

`74893d6b336ed39b82cba4efca9a6ae8f2c2983e9f1bd598394f5aab1de7a563`

## Reconstruction

Depuis la racine du dépôt :

```powershell
python tools/restore_v08_archive.py
```

Le script concatène les fragments dans l'ordre, décode le Base64, écrit le ZIP et vérifie son SHA-256.

Cette archive contient l'ensemble du projet V0.8 GitHub-ready, y compris les sources Android, la documentation, les scripts et le notebook de migration.
