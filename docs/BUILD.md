# Compiler et installer

## Prérequis

- Android Studio récent
- JDK 17 ou supérieur
- Android SDK 37
- téléphone Android avec débogage USB activé pour l'installation via ADB

## Versions principales

- compileSdk / targetSdk : 37
- minSdk : 26
- Android Gradle Plugin : 9.3.0
- Kotlin Compose plugin : 2.4.10
- Gradle : 9.5.0
- Room : 2.8.5

## Android Studio

1. Ouvrir le dossier du projet.
2. Laisser Gradle synchroniser les dépendances.
3. Sélectionner le téléphone ou un émulateur.
4. Lancer la configuration `app`.

## Ligne de commande Windows

Avec le Gradle Wrapper complet :

```powershell
.\gradlew.bat clean assembleDebug
```

APK attendu :

`app\build\outputs\apk\debug\app-debug.apk`

Installation :

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Mise à niveau depuis V0.7

Le notebook `tools/Upgrade_NearbyMonitor_V07_to_V08.ipynb` automatise la copie du projet, la migration, la compilation et l'installation.
