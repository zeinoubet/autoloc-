# Note technique : Stratégies de Fetch et de Cascade (Atelier 2 - AutoLoc)

Ce document formalise et justifie les choix d'annotations JPA pour les stratégies de chargement (`FetchType`) et de propagation du cycle de vie (`CascadeType` / `orphanRemoval`) appliquées aux entités du domaine **AutoLoc**.

---

## 1. Principes généraux retenus

1. **Stratégie de Fetch (LAZY systématique)** :
   - Toutes les associations (`@ManyToOne`, `@OneToOne`, `@OneToMany`, `@ManyToMany`) sont configurées explicitement en `FetchType.LAZY`.
   - **Raison** : Par défaut, JPA applique `FetchType.EAGER` sur les associations « to-one » (`@ManyToOne`, `@OneToOne`). Cela provoque le chargement récursif en cascade d'arborescences d'objets complètes (effet de bord de requêtes inutiles et risque de requêtes N+1). Le passage en `LAZY` garantit des lectures légères et ciblées.

2. **Stratégie de Cascade** :
   - Une cascade (`PERSIST`, `REMOVE`, ou `ALL`) n'est appliquée que lorsqu'il existe un lien fort de **composition** ou de cycle de vie partagé.
   - Si les entités ont des cycles de vie indépendants (ex. Agrégation ou entités partagées), **aucune cascade** n'est appliquée afin de garantir l'intégrité référentielle et éviter des suppressions accidentelles.

---

## 2. Tableau récapitulatif des associations

| Association | Côté propriétaire | Fetch | Cascade | Justification |
| :--- | :--- | :---: | :---: | :--- |
| **Contrat ↔ Paiement** | `Paiement` (`contrat`) | `LAZY` | `ALL` + `orphanRemoval` (côté `Contrat`) | Un paiement n'a pas d'existence propre hors de son contrat (relation de composition). Supprimer un contrat doit supprimer tous ses paiements (`cascade = ALL`), et retirer un paiement de la collection doit le supprimer en base (`orphanRemoval = true`). Le fetch LAZY évite de charger l'ensemble des paiements lors de la lecture d'un contrat. |
| **Agence → Vehicule** | `Vehicule` (`agence`) | `LAZY` | Aucune | Un véhicule survit à son agence (il peut être réaffecté à une autre agence ou conservé dans le parc). Aucune suppression ne doit se propager. Le fetch LAZY évite de charger tout le parc de véhicules lors de la simple consultation d'une agence. |
| **Agence → Employe** | `Employe` (`agence`) | `LAZY` | Aucune | Les employés ont un cycle de vie propre et indépendant de l'agence (mutation, réaffectation). Le fetch LAZY évite de charger la liste complète des employés lors de l'accès à une agence. |
| **Vehicule ↔ Equipement** | `Vehicule` (`@JoinTable`) | `LAZY` | Aucune | Les équipements (GPS, siège bébé, galerie...) sont partagés entre plusieurs véhicules et gérés de manière autonome dans le catalogue. Supprimer un véhicule ne doit en aucun cas supprimer un équipement. Utilisation d'un `Set` pour éviter les doublons. |
| **Client → Reservation** | `Reservation` (`client`) | `LAZY` | `PERSIST` (côté `Client`) | Permet de persister une ou plusieurs nouvelles réservations créées lors de l'enregistrement initial d'un client. En revanche, aucune cascade de suppression (`REMOVE`) n'est autorisée afin de préserver l'historique comptable et opérationnel des réservations. |
| **Reservation → Vehicule** | `Reservation` (`vehicule`) | `LAZY` | Aucune | Le véhicule existe indépendamment des réservations. L'annulation ou la suppression d'une réservation ne doit avoir aucun impact sur l'état physique du véhicule. |
| **Reservation ↔ Contrat** | `Contrat` (`reservation`) | `LAZY` | `ALL` (côté `Reservation`) | Le contrat est le reflet légal direct d'une réservation (relation 1-à-1 forte). La gestion du cycle de vie de la réservation pilote celui du contrat. Le fetch LAZY évite d'alourdir la consultation d'une réservation lorsque les données contractuelles ne sont pas requises. |
| **Vehicule → Maintenance** | `Maintenance` (`vehicule`) | `LAZY` | `PERSIST` (côté `Vehicule`) | Permet d'enregistrer de nouvelles opérations de maintenance directement attachées au véhicule lors de la sauvegarde de ce dernier. Aucune suppression en cascade n'est appliquée afin de conserver l'historique technique et de traçabilité du parc. |
