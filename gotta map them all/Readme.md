# Gotta Map'Em All : Projet de génie logiciel et gestion de projet (INFO-F-307)

## Contexte

Durant le mois de Juillet 2016, le jeu Pokémon Go a été lancé sur les plateformes Android et iOS, devenant rapidement un phénomène de société, avec plus de 500 millions de téléchargements en moins de 3 mois.
Pokémon Go est un jeu en réalité augmentée, dont le but est d'utiliser une carte fournie par l'application afin de localiser (et ensuite de capturer) des créatures virtuelles (les Pokémons). 
Chaque pokémon est caractérisé par son nom, son type et ses points de vie (PV), points d'attaque (PA) et points de défense (PD). 

Dans ce projet, je tente de développer un outil collaboratif de création de cartes pour alimenter le jeu. 
L'objectif de ce service est de permettre aux utilisateurs de construire une carte **partagée** sur laquelle chaque utilisateur peut localiser les lieux et les temps d'apparition des différentes créatures et ajouter des informations à ses signalisations.
Les autres utilisateurs du système pourront donc **visualiser** les signalisations présentes sur la carte et valider/invalider une signalisation, sur base de sa véracité. Le service demandé par le client aura donc des similarités avec les applications _Web Pokemap_ et _Pokemon Radar_. 

Le programme sera divisé en **deux** applications : une application de bureau et un serveur. L'application de bureau permettra à un utilisateur de créer sa propre carte et d'y ajouter ses signalisations. Toutes ces données seront alors enregistrées localement. 
L'utilisateur pourra se connecter au serveur afin de **synchroniser les signalisations** avec les autres utilisateurs, et de pouvoir interagir avec les autres signalisations. Le serveur s'occupera de toute la gestion en ligne des documents.

## But

Le projet consiste à développer une application pour la création d'une cartographie sociale assistée par ordinateur. 
Le langage choisi étant le _**JAVA**_, le tout doit être **exécutable** sous _**Linux, Windows et MacOS**_.

# Utilisation

Version Java : **JAVA 1.8**
Libraires externes : 
- `sqlite-jdbc-3.16.1` (documentation disponible [ici](https://bitbucket.org/xerial/sqlite-jdbc/overview) )
- `Jackson` (pour générer des objets JSON)
- `javax.mail` (documentation [ici](https://javamail.java.net/nonav/docs/api/) )
- `GMapsFX` (documentation disponible [ici](http://rterp.github.io/GMapsFX/apidocs/) )
- `slf4j-api` (documentation disponible [ici](https://www.slf4j.org/docs.html) )
- `Jersey`

(La plupart des librairies sont disponibles depuis le dossier `lib`, les autres 
sont téléchargées grâce au gestionnaire de dépendances Maven.)

## Compilation

Le projet est développé en utilisant l'IDLE eclispe, il est divisé en 2 parties : Client/Serveur.

Pour le compiler :
- Ouvrir eclipse
- Importer le code source depuis `git` (utiliser le plugin `egit` pour plus de faciliter)
- Sélectionner la branche `master`

### Partie cliente :
- Aller dans le package `be.ac.ulb.infof307.g04`
- Sélectionner la classe `Main`
- Lancer la compilation et exécuter (à noter qu'il faut avoir d'abord lancé le serveur)

### Partie Serveur :
- Aller dans le package `be.ac.ulb.infof307.g04.model.server`
- Sélectionner la classe `Server`
- Lancer la compilation et exécuter


## Démarrage 

Lancer le serveur, une fois lancée, exécuter le client.
Le projet est conçu pour avoir le serveur et le client sur le **_même ordinateur_**, pas de fichier externe de config permettant de se connecter à une machine externe.

## Note importante suite à l'itération 4
La librairie Jersey pose problème lorsqu'on lance l'application depuis un fichier JAR. 
Cela est indépendant de notre volonté malheureusement.
Par contre, tout fonctionne niquel lorsque le projet est lancé depuis Eclipse.

# Configuration :

## Serveur

- Framework : `Jersey`([Documentation](http://www.oracle.com/splash/java.net/maintenance/index.html))
- Architecture : `REST`([Documentation](https://en.wikipedia.org/wiki/Representational_state_transfer))
- Communication client/serveur : `Jackson`([Documentation](https://en.wikipedia.org/wiki/Jackson_(API)))
- Pattern : `DAO`([Documentation](http://www.javadocworld.com/daoDesignPatternExamples))

## Cient

- Interface graphique : `JavaFX`([Documentation](http://docs.oracle.com/javase/8/javase-clienttechnologies.htm))
- Pattern : `MVC`([Documentation](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller))


# Tests

Pour tester une certaine classe:
- Aller dans le dossier `test/be/ac/ulb/infof307/g04/model`
- Choisir si l'on souhaite tester une classe concernant la communication client/serveur ou les `DAO`([`Data Access Object`](http://www.oracle.com/technetwork/java/dataaccessobject-138824.html))
- Lancer la compilation et l'éxécution de la classe souhaitée.

# Misc

            

## Screenshot

![Fenêtre du jeu](http://img4.hostingpics.net/pics/260280screen1.png)
![écran de connexion](https://img4.hostingpics.net/pics/863898first.png)
![écran de création de compte](https://img4.hostingpics.net/pics/556707creation.png)
![écran général](https://img4.hostingpics.net/pics/171177overall.png)
![Utilisation de la navigation](https://img4.hostingpics.net/pics/708502navigation.png)
![Ajout d'un Pokemon](https://img4.hostingpics.net/pics/116687ajout.png)

## License
