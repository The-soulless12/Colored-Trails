# Colored-Trails
Développement d'un environnement multi-agents pour le jeu Colored Trails, utilisant JADE pour gérer les interactions entre agents et prendre des décisions autonomes afin de finir le jeu.

# Fonctionnalités 
- Visualisation des déplacements des agents sur la grille, de leur case de départ à leur destination.
- Affichage des communications entre agents pendant la partie (négociations de jetons et transactions) via le bouton bulle de discussion en haut à droite.
- Visualisation de l’environnement propre à chaque agent, accessible via le bouton paramètres à chaque étape du jeu. On peut y suivre la position de l'agent par rapport à sa case but, son nombre de blocages dans la partie (X), son facteur de loyauté vu par les autres agents (α) ainsi que le nombre de jetons en sa possession avec le détail des occurrences par couleur.
- Lorsqu’un agent est bloqué, il peut envoyer une demande de jeton aux autres agents. Ces derniers peuvent alors faire une offre selon ces quatre stratégies disponibles :
- - Stratégie de l’Exploration Opportuniste : L’agent propose une couleur aléatoire parmi ses jetons, différente de celle demandée ou non encore possédée. Une façon de tester le terrain sans trop s’exposer.
- - Stratégie du Ciblage Incomplet : L’agent identifie une couleur manquante sur son chemin et la réclame afin de compléter sa collection. Une approche basée sur l’observation plutôt que la ruse.
- - Stratégie d’Optimisation Fréquente : L’agent sélectionne la couleur la plus présente sur son chemin futur parmi celles qu’il ne possède pas encore. Elle maximise les chances de progression au détriment de la loyauté.
- - Stratégie du Leurre Malicieux : L’agent s'engage volontairement à donner une couleur qu’il ne possède pas en échange d'une couleur dont il a besoin; une stratégie risquée et coûteuse en loyauté.
- La partie s’arrête dans si un joueur atteint la case but ou lorsqu’un joueur est bloqué pendant trois tours.
- À la fin du jeu, un bonus de 5 points est accordé pour chaque jeton restant. Si l’agent atteint le but, il reçoit un bonus de 100 points sinon, une pénalité de 10 points est appliquée par case manquante sur son chemin.

# Structure du projet
- Images/ : Contient les icônes et images des agents utilisées dans le jeu.
- lib/ : Contient le fichier `jade.jar`, nécessaire pour les communications entre agents.
- out/ : Dossier de sortie contenant les fichiers `.class` générés lors de la compilation du projet.
- src/ : Contient les fichiers sources `.java` du projet (CaseChemin, Grille, Joueur, Main, Master, Offre & Position).
- run.bat : Script de compilation et d’exécution du projet qui compile les fichiers Java puis lance le fichier Main.
- APDescription.txt : Fichier de description de la plateforme JADE, définissant l’adresse de l’agent principal et les services accessibles.
- MTPs-Main-Container.txt : Fichier de configuration spécifiant l’adresse HTTP de l’agent JADE principal.

# Prérequis
- Java (JDK).

# Note
- Pour exécuter le projet, saisissez la commande suivante `.\run.bat` dans votre terminal.