# Nearby Monitor

**Version archivée : 0.8.0**

Nearby Monitor est une application Android native en Kotlin / Jetpack Compose destinée à détecter, inventorier et analyser les équipements visibles à proximité via **Wi-Fi, Bluetooth Low Energy et réseau local (LAN)**.

La V0.8 ajoute une fonction de **localisation approximative par trilatération RSSI** à partir de trois mesures ou plus.

## Fonctionnalités principales

### Découverte Wi-Fi

- points d'accès visibles ;
- SSID / BSSID ;
- RSSI ;
- fréquence et canal ;
- sécurité réseau lorsque disponible ;
- identification du fabricant via OUI lorsque l'adresse est publique.

### Découverte Bluetooth Low Energy

- appareils BLE visibles ;
- RSSI ;
- nom annoncé ;
- Bluetooth Company ID ;
- services / UUID ;
- identification du fabricant lorsque possible.

### Découverte LAN

- hôtes IPv4 locaux ;
- passerelle / routeur ;
- adresse IP ;
- hostname ;
- découverte mDNS / DNS-SD ;
- services et ports annoncés ;
- récupération opportuniste de la MAC lorsque disponible.

### Inventaire persistant

La base Room / SQLite conserve :

- première détection ;
- dernière détection ;
- compteur de détections ;
- état Présent / Absent ;
- état Connu / Inconnu ;
- alias personnalisés ;
- notes ;
- historique des observations ;
- informations fabricant et identification probable.

### Identification fabricant

L'application combine plusieurs sources :

- OUI MAC IEEE MA-L / MA-M / MA-S ;
- Bluetooth SIG Company ID ;
- nom de l'équipement ;
- hostname ;
- services mDNS ;
- services BLE.

Le résultat inclut une **source** et un **niveau de confiance**.

### Anti-duplication des MAC randomisées

Pour éviter la création de dizaines de fiches lorsque la MAC change :

- détection des adresses privées / localement administrées ;
- corrélation BLE par nom, Company ID et services ;
- corrélation LAN par hostname / mDNS / services ;
- conservation des adresses successives dans une même fiche ;
- agrégation prudente des équipements impossibles à distinguer ;
- affichage du niveau de confiance de corrélation.

La corrélation est heuristique. Elle ne contourne pas les mécanismes de confidentialité BLE / Wi-Fi et ne prétend pas résoudre cryptographiquement les RPA Bluetooth.

### Interface

- filtres Actifs / Inventaire / Wi-Fi / BLE / LAN / Fabricants / Identifiés / MAC privées / Inconnus ;
- affichage Compact / Détaillé mémorisé ;
- fiches détaillées ;
- historique par équipement ;
- plein écran immersif ;
- boutons compacts.

## Localisation par trilatération RSSI

Disponible pour les équipements fournissant un RSSI exploitable, principalement BLE et points d'accès Wi-Fi.

Procédure :

1. choisir un repère X/Y local en mètres ;
2. prendre au moins trois mesures depuis des positions différentes ;
3. éviter les points alignés ;
4. l'application estime une distance à partir du RSSI ;
5. le solveur calcule la position 2D par moindres carrés ;
6. l'application affiche la position estimée, l'erreur RMS et un indicateur de qualité.

Le modèle utilisé est :

`d = 10 ^ ((RSSI_1m - RSSI) / (10 × n))`

Valeurs initiales :

- BLE : -59 dBm à 1 m ;
- Wi-Fi : -45 dBm à 1 m ;
- exposant radio n : 2,2.

Pour de meilleurs résultats, utiliser 4 ou 5 mesures réparties autour de l'équipement.

## Architecture technique

- Kotlin
- Jetpack Compose
- Room / SQLite
- Coroutines / StateFlow
- Wi-Fi Android APIs
- Bluetooth LE Android APIs
- NSD / mDNS
- minSdk 26
- targetSdk / compileSdk 37
- Java 17+

Organisation :

- `scanner/` : Wi-Fi, BLE, LAN ;
- `identification/` : fabricant, type probable, corrélation d'identité ;
- `data/` : Room, DAO, repository ;
- `localization/` : trilatération RSSI ;
- `ui/` : interface Compose ;
- `tools/` : mise à jour OUI et notebook de migration.

## Compiler

Prérequis : Android Studio récent, JDK 17+, Android SDK 37.

Depuis Android Studio, ouvrir le dossier puis synchroniser Gradle et lancer `app`.

En ligne de commande :

```powershell
.\gradlew.bat clean assembleDebug
```

APK :

`app/build/outputs/apk/debug/app-debug.apk`

## Mise à niveau depuis V0.7

Le notebook suivant est fourni :

`tools/Upgrade_NearbyMonitor_V07_to_V08.ipynb`

Il automatise la création de `NearbyMonitorV08`, la migration, la compilation et l'installation ADB.

## Confidentialité

L'inventaire, l'identification et la corrélation sont réalisés localement sur le téléphone. Les MAC et IP ne sont pas envoyées à un service externe pendant l'utilisation normale.

## Documentation

- `docs/FEATURES.md`
- `docs/ARCHITECTURE.md`
- `docs/BUILD.md`
- `docs/LOCALIZATION.md`
- `docs/SECURITY_PRIVACY.md`
- `CHANGELOG.md`

## Statut

Projet expérimental / laboratoire. La détection LAN, l'identification et surtout la localisation RSSI sont de type **best effort** et dépendent des restrictions Android, des équipements et de l'environnement radio.


## Archive V0.8

Une copie exacte de l'arborescence V0.8 GitHub-ready est conservée sous forme d'archive ZIP encodée en Base64 dans le dossier `archive/`.

Pour la reconstruire :

```powershell
python tools/restore_v08_archive.py
```

Le script vérifie le SHA-256 de l'archive avant de l'écrire. Voir `archive/README.md` pour les détails.
