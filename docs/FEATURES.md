# Fonctionnalités

## Découverte

Nearby Monitor combine trois sources de découverte :

- Wi-Fi : points d'accès visibles, BSSID, RSSI, fréquence, canal et sécurité quand disponibles.
- Bluetooth Low Energy : appareils BLE visibles, RSSI, nom annoncé, Company ID et services.
- LAN : hôtes IPv4 locaux, passerelle, hostname et services mDNS/DNS-SD lorsque disponibles.

## Inventaire persistant

La base Room conserve :

- première et dernière détection ;
- compteur de détections ;
- état connu / inconnu ;
- alias et notes ;
- historique des observations ;
- informations réseau, fabricant et identification probable.

## Identification fabricant

L'identification est réalisée localement à partir de :

- Bluetooth SIG Company ID pour le BLE ;
- base OUI MAC MA-L / MA-M / MA-S pour les adresses publiques ;
- heuristiques de nom, hostname et services lorsque nécessaire.

Les MAC localement administrées / randomisées ne sont pas attribuées artificiellement à un fabricant via l'OUI.

## Anti-duplication des MAC randomisées

Pour le BLE et certains cas LAN, l'application construit une empreinte prudente à partir d'indices plus stables que l'adresse MAC : nom annoncé, Company ID, services, hostname et mDNS.

Lorsque la corrélation est suffisamment fiable, les rotations de MAC enrichissent la même fiche au lieu de créer une nouvelle entrée. Les cas non discriminants sont explicitement agrégés pour éviter de polluer l'inventaire.

## Interface

- filtres Actifs / Inventaire / Wi-Fi / BLE / LAN / Fabricants / Identifiés / MAC privées / Inconnus ;
- mode Compact / Détaillé mémorisé ;
- fiche détaillée par équipement ;
- boutons compacts ;
- plein écran immersif.

## Localisation V0.8

Pour les équipements fournissant un RSSI, principalement BLE et points d'accès Wi-Fi :

- minimum 3 mesures depuis des positions X/Y connues ;
- estimation de la distance via un modèle log-distance ;
- trilatération 2D ;
- support de 4, 5 mesures ou plus ;
- erreur RMS indicative ;
- indicateur de qualité ;
- mini-plan 2D.

La localisation RSSI est une estimation : murs, mobilier, multipath, corps humain et puissance d'émission peuvent entraîner plusieurs mètres d'erreur.
