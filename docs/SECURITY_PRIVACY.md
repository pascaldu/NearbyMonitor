# Sécurité et confidentialité

## Traitement local

Nearby Monitor effectue l'inventaire, l'identification et la corrélation localement sur le téléphone.

Les adresses MAC, IP et empreintes d'identité ne sont pas envoyées à une API externe pour l'identification pendant l'utilisation normale.

## Base OUI

La base fabricant est embarquée dans l'application. Le script `tools/update_oui.py` permet de régénérer cette base avant compilation.

## MAC randomisées

L'application ne prétend pas casser les mécanismes de confidentialité BLE ou Wi-Fi. Une adresse BLE RPA ne peut pas être résolue cryptographiquement sans la clé IRK correspondante.

La corrélation V0.7/V0.8 est heuristique et affiche un niveau de confiance. Elle peut donc produire des faux regroupements entre appareils présentant des annonces très similaires.

## Données sensibles

Ne jamais versionner :

- `local.properties` ;
- clés de signature Android ;
- secrets/API keys ;
- fichiers `.env` ;
- mots de passe ou jetons.

Le `.gitignore` du dépôt exclut ces catégories.
