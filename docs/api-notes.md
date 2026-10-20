# Note technique : API REST Spring MVC, SpringDoc OpenAPI et Tests Postman (Atelier 5 - AutoLoc)

Ce document formalise la conception et l'implémentation de la couche Web (`tn.esprit.elabed_zeineb_4cce11.web.controller`) de l'application **AutoLoc**, l'exposition des services sous forme d'API REST standardisée, la gestion des exceptions centralisée via `GlobalExceptionHandler`, la configuration de la documentation dynamique OpenAPI 3 / Swagger UI avec SpringDoc, les tests fonctionnels sous Postman, ainsi que la résolution des anomalies SonarQube for IDE.

---

## A. Endpoints

Le préfixe commun `/api` est défini une seule fois dans `src/main/resources/application.properties` via la propriété `server.servlet.context-path=/api`. Les routes ci-dessous sont donc exposées sous `http://localhost:8089/api/...`.

| Contrôleur | Verbe HTTP et Route | Service appelé | Code succès | Codes d'erreur | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `ContratController` | `GET /api/contrats` | `contratService.findAll()` | `200 OK` | — | Lister tous les contrats de location. |
| `ContratController` | `GET /api/contrats/{id}` | `contratService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un contrat par son identifiant. |
| `ContratController` | `POST /api/contrats` | `contratService.create(contrat)` | `201 Created` | `400 Bad Request` | Créer un nouveau contrat (avec ou sans paiements imbriqués). |
| `ContratController` | `PUT /api/contrats/{id}` | `contratService.update(id, contrat)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Modifier un contrat (recopie sélective préservant les paiements). |
| `ContratController` | `DELETE /api/contrats/{id}` | `contratService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un contrat et ses paiements associés (cascade REMOVE). |
| `PaiementController` | `GET /api/paiements` | `paiementService.findAll()` | `200 OK` | — | Lister tous les paiements (service en lecture seule). |
| `PaiementController` | `GET /api/paiements/{id}` | `paiementService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un paiement par son identifiant. |
| `PaiementController` | `POST /api/paiements` | *(Aucun - non exposé)* | — | `405 Method Not Allowed` | Tentative de création directe d'un paiement refusée. |
| `AgenceController` | `GET /api/agences` | `agenceService.findAll()` | `200 OK` | — | Lister toutes les agences. |
| `AgenceController` | `GET /api/agences/{id}` | `agenceService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une agence par son identifiant. |
| `AgenceController` | `POST /api/agences` | `agenceService.create(agence)` | `201 Created` | `400 Bad Request` | Créer une agence (rejet si nom ou ville vide). |
| `AgenceController` | `PUT /api/agences/{id}` | `agenceService.update(id, agence)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour les informations d'une agence. |
| `AgenceController` | `DELETE /api/agences/{id}` | `agenceService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer une agence par son identifiant. |
| `EmployeController` | `GET /api/employes` | `employeService.findAll()` | `200 OK` | — | Lister tous les employés. |
| `EmployeController` | `GET /api/employes/{id}` | `employeService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un employé par son identifiant. |
| `EmployeController` | `POST /api/employes` | `employeService.create(employe)` | `201 Created` | `400 Bad Request` | Créer un employé (agence en corps imbriqué `{ "agence": { "idAgence": 1 } }`). |
| `EmployeController` | `PUT /api/employes/{id}` | `employeService.update(id, employe)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour les informations d'un employé. |
| `EmployeController` | `DELETE /api/employes/{id}` | `employeService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un employé par son identifiant. |
| `VehiculeController` | `GET /api/vehicules` | `vehiculeService.findAll()` | `200 OK` | — | Lister l'ensemble du parc automobile. |
| `VehiculeController` | `GET /api/vehicules/{id}` | `vehiculeService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un véhicule par son identifiant. |
| `VehiculeController` | `POST /api/vehicules` | `vehiculeService.create(vehicule)` | `201 Created` | `400 Bad Request` | Créer un véhicule (rejet si tarif journalier négatif). |
| `VehiculeController` | `PUT /api/vehicules/{id}` | `vehiculeService.update(id, vehicule)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour les caractéristiques d'un véhicule. |
| `VehiculeController` | `DELETE /api/vehicules/{id}` | `vehiculeService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un véhicule du parc. |
| `EquipementController` | `GET /api/equipements` | `equipementService.findAll()` | `200 OK` | — | Lister les équipements disponibles. |
| `EquipementController` | `GET /api/equipements/{id}` | `equipementService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un équipement par son identifiant. |
| `EquipementController` | `POST /api/equipements` | `equipementService.create(equipement)` | `201 Created` | `400 Bad Request` | Créer un équipement (rejet si libellé vide). |
| `EquipementController` | `PUT /api/equipements/{id}` | `equipementService.update(id, equipement)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour un équipement. |
| `EquipementController` | `DELETE /api/equipements/{id}` | `equipementService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un équipement par son identifiant. |
| `ClientController` | `GET /api/clients` | `clientService.findAll()` | `200 OK` | — | Lister tous les clients enregistrés. |
| `ClientController` | `GET /api/clients/{id}` | `clientService.findById(id)` | `200 OK` | `404 Not Found` | Consulter un client par son identifiant. |
| `ClientController` | `POST /api/clients` | `clientService.create(client)` | `201 Created` | `400 Bad Request` | Créer un client (rejet si nom ou e-mail vide). |
| `ClientController` | `PUT /api/clients/{id}` | `clientService.update(id, client)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour un profil client. |
| `ClientController` | `DELETE /api/clients/{id}` | `clientService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un client par son identifiant. |
| `ReservationController` | `GET /api/reservations` | `reservationService.findAll()` | `200 OK` | — | Lister toutes les réservations. |
| `ReservationController` | `GET /api/reservations/{id}` | `reservationService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une réservation par son identifiant. |
| `ReservationController` | `POST /api/reservations` | `reservationService.create(reservation)` | `201 Created` | `400 Bad Request` | Créer une réservation (rejet si date de fin antérieure au début). |
| `ReservationController` | `PUT /api/reservations/{id}` | `reservationService.update(id, reservation)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour les dates ou le statut d'une réservation. |
| `ReservationController` | `DELETE /api/reservations/{id}` | `reservationService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer une réservation par son identifiant. |
| `MaintenanceController` | `GET /api/maintenances` | `maintenanceService.findAll()` | `200 OK` | — | Lister toutes les opérations de maintenance. |
| `MaintenanceController` | `GET /api/maintenances/{id}` | `maintenanceService.findById(id)` | `200 OK` | `404 Not Found` | Consulter une opération de maintenance par identifiant. |
| `MaintenanceController` | `POST /api/maintenances` | `maintenanceService.create(maintenance)` | `201 Created` | `400 Bad Request` | Enregistrer une maintenance (rejet si date de fin antérieure). |
| `MaintenanceController` | `PUT /api/maintenances/{id}` | `maintenanceService.update(id, maintenance)` | `200 OK` | `400 Bad Request`, `404 Not Found` | Mettre à jour les données d'une maintenance. |
| `MaintenanceController` | `DELETE /api/maintenances/{id}` | `maintenanceService.deleteById(id)` | `204 No Content` | `404 Not Found` | Supprimer un enregistrement de maintenance. |

---

## B. Annotations Jackson et Sérialisation JSON

Les associations bidirectionnelles JPA provoquent par défaut des boucles de sérialisation infinies avec Jackson (`StackOverflowError` ou `Document nesting depth exceeds the maximum`). Deux stratégies ciblées ont été déployées en fonction de la sémantique métier :

| Association | Entités concernées | Annotation retenue | Emplacement | Justification technique et métier |
| :--- | :--- | :--- | :--- | :--- |
| **Contrat ↔ Paiement** (Composition stricte) | `Contrat`, `Paiement` | `@JsonManagedReference("contrat-paiements")` / `@JsonBackReference("contrat-paiements")` | `@JsonManagedReference` sur `Contrat.paiements` et `@JsonBackReference` sur `Paiement.contrat` | Vraie composition où les enfants `Paiement` sont créés directement imbriqués dans le JSON du parent `Contrat` via `cascade = ALL`. La paire d'annotations permet à Jackson d'instancier les paiements, de leur réinjecter la référence parent `contrat`, tout en neutralisant la sérialisation en boucle du parent côté paiement. |
| **Agence ↔ Vehicule** | `Agence`, `Vehicule` | `@JsonIgnore` | `Agence.vehicules` | Les véhicules sont gérés indépendamment de l'agence. Le véhicule désigne son agence de rattachement par `{ "agence": {"idAgence": 1} }`. La liste inverse dans `Agence` est ignorée pour ne pas charger ni sérialiser l'ensemble du parc automobile lors de la consultation d'une agence. |
| **Agence ↔ Employe** | `Agence`, `Employe` | `@JsonIgnore` | `Agence.employes` | L'employé référence son agence par identifiant ; la liste inverse des employés dans l'agence est masquée à la sérialisation JSON. |
| **Client ↔ Reservation** | `Client`, `Reservation` | `@JsonIgnore` | `Client.reservations` | Les réservations sont créées de manière autonome en désignant le client par `{ "client": {"idClient": 1} }`. La liste inverse est ignorée. |
| **Vehicule ↔ Maintenance** | `Vehicule`, `Maintenance` | `@JsonIgnore` | `Vehicule.maintenances` | La maintenance référence son véhicule ; la collection inverse dans le véhicule est ignorée pour couper la boucle. |
| **Equipement ↔ Vehicule** | `Equipement`, `Vehicule` | `@JsonIgnore` | `Equipement.vehicules` | Association `@ManyToMany` ; le côté non-propriétaire `Equipement` ignore sa collection de véhicules. |
| **Reservation ↔ Contrat** | `Reservation`, `Contrat` | `@JsonIgnore` | `Reservation.contrat` | Association `@OneToOne` bidirectionnelle ; `Reservation` ignore son contrat pour éviter la boucle avec `Contrat.reservation`. |

### Différence fondamentale entre `@JsonBackReference` et `@JsonIgnore`

1. **`@JsonBackReference`** :
   - Fait toujours partie d'un **couple** indissociable avec `@JsonManagedReference`.
   - **Lors de la sérialisation (Java → JSON)** : Le champ annoté `@JsonBackReference` est omis du flux JSON pour éviter la boucle infinie.
   - **Lors de la désérialisation (JSON → Java)** : Jackson réassigne automatiquement à ce champ l'instance de l'objet parent qui l'englobe dans la hiérarchie JSON (`paiement.setContrat(parentContrat)`).
   - **Contre-indication majeure** : Il ne doit jamais être utilisé sur une entité fille créée de manière autonome (ex. `Reservation.client`). En effet, le champ annoté `@JsonBackReference` est systématiquement ignoré à la lecture directe du JSON : une requête POST `{ "client": {"idClient": 1} }` verrait son attribut `client` ignoré et resterait à `null` en base.
2. **`@JsonIgnore`** :
   - Fonctionne de manière **autonome et unilatérale**.
   - Ignore le champ de façon globale lors de la sérialisation JSON.
   - Il convient parfaitement aux listes ou références côté inverse (`mappedBy`) lorsque les entités enfants sont créées individuellement en spécifiant leur parent par clé primaire.

---

## C. Postman et Swagger (SpringDoc OpenAPI) : Complémentarité

| Outil | Rôle principal | Apport spécifique dans l'Atelier 5 | Exemple concret rencontré |
| :--- | :--- | :--- | :--- |
| **Postman** | Client HTTP autonome dédié aux **tests fonctionnels automatisés**, aux assertions programmées et au rejeu de scénarios séquentiels. | Permet d'écrire des scripts de test (onglet *Scripts > After response*), d'extraire des données de réponse pour renseigner dynamiquement l'environnement (`pm.environment.set(...)`), et de valider les codes de retour précis (200, 201, 204, 400, 404, 405). | **Scénario de cascade Contrat ↔ Paiement** : Création d'un contrat avec deux paiements imbriqués via POST (201), extraction automatique de `contratId`, modification avec PUT (200), puis suppression via DELETE (204) et vérification immédiate que les paiements enfants ont bien disparu de la base via GET `/api/paiements/{id}` (404 par cascade REMOVE). |
| **Swagger UI (SpringDoc)** | Interface web interactive et standardisée dédiée à la **documentation vivante (contrat d'API OpenAPI 3)** et à l'exploration interactive. | Génère automatiquement la spécification OpenAPI (`/v3/api-docs`) à partir du code source compilé et offre une interface visuelle (`/swagger-ui.html`) documentant les schémas des entités, les codes HTTP attendus (`@ApiResponses`), les paramètres (`@Parameter`) et les groupes d'endpoints (`@Tag`). | **Exploration interactive immédiate** : Visualisation instantanée des 9 groupes de contrôleurs, consultation de la documentation du schéma JSON de `Contrat`, et exécution rapide de tests unitaires via le bouton *Try it out / Execute* sans configuration préalable de client HTTP. |

---

## D. Analyse de Qualité avec SonarQube for IDE (SonarLint)

Lors de l'analyse statique du package `tn.esprit.elabed_zeineb_4cce11.web.controller`, plusieurs anomalies et règles de qualité majeures ont été identifiées, traitées et documentées :

| Règle SonarQube | Intitulé de la règle | Explication du problème | Correction appliquée |
| :--- | :--- | :--- | :--- |
| **`java:S6813`** | *"Field dependency injection should be avoided"* | L'injection par champ direct via `@Autowired` masque les dépendances réelles de la classe, empêche de déclarer les champs `final` (perte d'immuabilité), et rend impossible l'instanciation de la classe avec `new` lors de tests unitaires isolés. | Remplacement systématique par l'injection par constructeur via des champs `private final` combinés avec l'annotation Lombok **`@RequiredArgsConstructor`** sur tous les contrôleurs REST. |
| **`java:S1128`** | *"Unused imports should be removed"* | Des imports non utilisés subsistaient dans certaines classes suite à des refactorisations successives, polluant le code source et augmentant la complexité de lecture. | Suppression rigoureuse de tous les imports inutiles dans l'ensemble des contrôleurs et entités du projet. |
| **`java:S4684`** | *"Persistent entities should not be used as arguments or return values in @RequestMapping methods"* | L'exposition directe des entités JPA (`Contrat`, `Client`, etc.) dans les paramètres `@RequestBody` et retours de méthodes des contrôleurs REST engendre un couplage fort entre le schéma de persistance relationnel et l'API publique (risque de fuite de données, problème de proxies LAZY, vulnérabilité *mass assignment*). | **Anomalie assumée et documentée pour l'Atelier 5** : Comme indiqué dans le sujet pédagogique, cette exposition directe est provisoire. La séparation stricte et définitive sera formalisée au Chapitre 10 via la mise en place des **DTO** (Data Transfer Objects) et de couches de mapping dédiées. |

---

## E. Réponses aux Questions de Compréhension

### 1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?
Toutes les requêtes HTTP adressées à l'application sont centralisées et interceptées par la **`DispatcherServlet`** (implémentation du patron d'architecture *Front Controller* au cœur de Spring MVC). 
Pour déterminer la méthode cible, la `DispatcherServlet` consulte le composant **`HandlerMapping`** (plus précisément `RequestMappingHandlerMapping`). Ce composant maintient une table de correspondance en mémoire construite au démarrage de l'application, reliant les métadonnées de la requête (méthode HTTP GET/POST/PUT/DELETE, chemin d'URL, en-têtes) aux méthodes des classes annotées avec **`@RestController`** et **`@RequestMapping`** (ou ses variantes `@GetMapping`, `@PostMapping`...).

### 2. Pourquoi `ContratController` dépend-il de `IContratService` et non de `ContratRepository` ni de `ContratServiceImpl` ?
- **Par rapport à `ContratRepository`** : Pour respecter scrupuleusement la séparation des responsabilités en couches. Le contrôleur web ne doit gérer que la couche protocolaire HTTP (désérialisation, validation des requêtes, codes de statut HTTP). Il délègue l'intégralité de la logique métier (contrôle de cohérence temporelle, validation du montant positif, recopie sélective lors de la mise à jour) à la couche **Service**, sans jamais accéder directement aux opérations de persistance brute.
- **Par rapport à `ContratServiceImpl`** : Pour respecter le **Principe d'Inversion des Dépendances (DIP)** des principes SOLID. En dépendant d'une abstraction (`IContratService`) plutôt que d'une implémentation concrète, le contrôleur reste totalement découplé. Cela permet d'interchanger l'implémentation sans modifier le contrôleur et facilite grandement les tests unitaires via des doublons de test (mocks).

### 3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?
- **POST renvoie 201 (`Created`)** : Conformément à la norme HTTP/REST (RFC 9110), le code 201 signifie que la requête a abouti à la création effective d'une nouvelle ressource sur le serveur, et que son identifiant ou URI est fourni dans le corps ou l'en-tête `Location`.
- **DELETE renvoie 204 (`No Content`)** : Ce code indique que l'opération de suppression a été exécutée avec succès sur le serveur et qu'aucun contenu supplémentaire n'a besoin d'être transmis dans le corps de la réponse.
- **404 (`Not Found`) vs 500 (`Internal Server Error`)** : L'absence d'une ressource demandée par son identifiant est une erreur normale relevant du client HTTP (mauvaise URL ou identifiant inexistant). Elle est interceptée proprement par le `GlobalExceptionHandler` et renvoie un code **404**. À l'inverse, le code **500** désigne un crash ou une exception non gérée survenue sur le serveur (ex. `NullPointerException`, échec de connexion à la base), ce qui révélerait une anomalie logicielle non maîtrisée.

### 4. Pourquoi `PaiementController` n'expose-t-il que des GET ? Quelle réponse reçoit un POST /api/paiements ?
- Dans le modèle métier AutoLoc, un `Paiement` n'a pas d'existence autonome lors de sa création ou de sa modification : il appartient au cycle de vie de son agrégat parent `Contrat` (`cascade = ALL`, `orphanRemoval = true`). Les paiements sont créés et supprimés exclusivement par l'intermédiaire du contrat qui les porte. `PaiementService` et `PaiementController` sont donc volontairement conçus **en lecture seule** (`findAll` et `findById`).
- Si un client émet une requête HTTP `POST` sur `/api/paiements`, le serveur ne trouve aucun mapping `@PostMapping` enregistré pour cette route : Spring MVC renvoie immédiatement le statut HTTP **`405 Method Not Allowed`**.

### 5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements` utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ?
- **Risques de l'exposition directe des entités JPA** :
  1. *Fuite d'informations sensibles* et sur-exposition des champs techniques de la base de données.
  2. *Vulnérabilité d'assignation en masse (Mass Assignment)* : Un utilisateur malveillant pourrait écraser des champs internes protégés lors d'une mise à jour.
  3. *Couplage excessif* : Toute modification de la structure de la base de données casse immédiatement les clients de l'API REST.
  4. *Erreurs de sérialisation Jackson* : Boucles infinies (`StackOverflowError`) sur les relations bidirectionnelles et exceptions `ByteBuddyInterceptor` sur les proxys Hibernate non initialisés en mode LAZY.
- **`Contrat.paiements` (`@JsonManagedReference`) vs `Agence.vehicules` (`@JsonIgnore`)** :
  - `Contrat` et `Paiement` forment une **composition parent-enfant** : un contrat est créé avec ses paiements inclus dans le corps JSON (`cascade = PERSIST`). `@JsonManagedReference` (côté parent) et `@JsonBackReference` (côté enfant) permettent à Jackson de désérialiser la liste des paiements imbriqués tout en restaurant automatiquement la référence vers le contrat parent sans provoquer de cycle infini.
  - À l'opposé, les véhicules sont des entités autonomes créées séparément en pointant simplement vers leur agence via `{ "agence": { "idAgence": 1 } }`. Sérialiser la liste de tous les véhicules lors de la consultation d'une agence saturerait le flux JSON et créerait une boucle de référence ; `@JsonIgnore` est donc indispensable sur la liste inverse `Agence.vehicules`.
