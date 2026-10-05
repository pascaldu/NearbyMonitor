# Architecture

## Stack

- Kotlin
- Android / Jetpack Compose
- Room / SQLite
- Coroutines / StateFlow
- Android Wi-Fi APIs
- Android Bluetooth LE APIs
- Android NSD / mDNS

## Organisation

`scanner/`
: acquisition Wi-Fi, BLE et LAN.

`identification/`
: identification fabricant, classification et corrélation des MAC privées.

`data/`
: entités Room, DAO, base et repository.

`model/`
: modèles de domaine.

`localization/`
: solveur de trilatération RSSI.

`ui/`
: interface Jetpack Compose, vue principale et dialogue de localisation.

## Flux principal

1. Les scanners produisent des `NearbyDevice`.
2. `MonitorRepository` enrichit les détections, applique la corrélation d'identité et persiste l'inventaire.
3. Room fournit un flux d'inventaire persistant.
4. `MonitorViewModel` transforme ces données en état UI.
5. Compose affiche l'inventaire, les détails, l'historique et la localisation.

## Base de données

Version Room V5.

Tables principales :

- `devices`
- `observations`
- `location_measurements`
