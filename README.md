# INFO 901 DOC

## Architecture

Le projet est divisé en plusieurs modules :
- app: l'application avec le fichier main à lancer
- domain: contient toutes les entités du projet
- communication: implémentation du système de communication avec Guava

## Lancer le projet
Pour lancer le projet, utilisez la commande suivante, à la source du projet :
```
./gradlew run
```

## Explication du système

`Message` : correspond à un message envoyé entre les processus. 
Plusieurs types de messages permettent d'identifier le but d'un message (système, utilisateur...)

`ClockMessage` : message reçu par le communicator. Il contient le message, son horloge de Lamport et une indication spécifiant s'il faut envoyer un message de confirmation à l'envoyeur.

`MailBox` : contient des messages, accessible par un processus par exemple.

`Process` : Un processus à lancer.

`Communicator` : Assure la communication entre les processus.

### Le communicator

Je vais revenir sur certains choix d'implémentation réalisés pour le communicator.

`sendToSync()` : Permet d'envoyer un message et de s'assurer que ce dernier ai été reçu par le destinaire.
Pour ce faire, un message est envoyé avec une indication de callback. Le destinataire envoie alors un message de callback.
Une fois reçu, ce message est ajouté dans une mail box de messages de callback.
On boucle tant qu'aucun message de callback du destinataire ne soit dans la mail box.

`broadcastSync()` : Même système que pour `sendToSync()` mais on attends d'avoir x messages de callbacks correspondants au nombre de processus ayant reçu le message.

L'id d'un processus est donné par son communicator.