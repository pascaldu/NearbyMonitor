# Localisation par trilatération RSSI

## Principe

Le RSSI est converti en distance approximative avec le modèle log-distance :

`d = 10 ^ ((RSSI_1m - RSSI) / (10 × n))`

avec :

- `RSSI_1m` : puissance mesurée / supposée à 1 m ;
- `n` : exposant de pertes radio.

Valeurs initiales utilisées par l'application :

- BLE : -59 dBm à 1 m ;
- Wi-Fi : -45 dBm à 1 m ;
- n : 2,2.

## Procédure

1. Choisir un repère local en mètres.
2. Prendre une mesure à au moins trois positions différentes.
3. Éviter des points alignés.
4. L'application résout la position 2D aux moindres carrés.
5. Une erreur RMS est calculée pour aider à juger la cohérence des mesures.

## Recommandation

Quatre ou cinq mesures réparties autour de l'équipement donnent généralement une estimation plus stable que trois mesures.

## Limites

Le résultat ne doit pas être interprété comme une mesure métrique garantie. En intérieur, multipath et obstacles peuvent modifier fortement le RSSI.
