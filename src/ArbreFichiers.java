public class ArbreFichiers {

    private ArbreFichiers pere;
    // Par ordre alphabétique
    private ArbreFichiers fils;
    private ArbreFichiers frereGauche;
    private ArbreFichiers frereDroite;
    // Sans espace
    private String nomFichier;
    // Fichier : true | Dossier : false
    private boolean estFichier;
    // Fichier : Contenu | Dossier : null
    private String contenu;
    // Taille en octets
    private int tailleEnOctets;

    //Constructeur vide : Construit le dossier racine !
    public ArbreFichiers() {
        this.pere = null;
        this.fils = null;
        this.frereGauche = null;
        this.frereDroite = null;
        this.nomFichier = "racine";
        this.estFichier = false;
        this.contenu = null;
        this.tailleEnOctets = 0;
    }

    //Constructeur Noeud sans fils/freres
    public ArbreFichiers(ArbreFichiers pere, String nomFichier, boolean estFichier, String contenu) {
        this.pere = pere;
        this.fils = null;
        this.frereGauche = null;
        this.frereDroite = null;
        this.nomFichier = nomFichier;
        this.estFichier = estFichier;
        this.contenu = contenu;
        this.tailleEnOctets = (this.contenu == null) ? 0 : contenu.length();
    }

    // Méthode 1 : equivalent touch/mkdir
    public void addNoeud(ArbreFichiers nouveauNoeud) {

        nouveauNoeud.pere = this;

        if (this.fils == null) {
            this.fils = nouveauNoeud;
        } else {

            // On cherche à droite
            if (this.fils.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) <= 0) {

                ArbreFichiers noeudActuel = this.fils.frereDroite;

                if (noeudActuel != null) {

                    //Si le nouveauNoeud doit se placer entre le fils et son frereDroite direct, alors on l'ajoute
                    if (noeudActuel.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) > 0) {
                        this.fils.frereDroite = nouveauNoeud;
                        nouveauNoeud.frereGauche = this.fils;
                        nouveauNoeud.frereDroite = noeudActuel;
                        noeudActuel.frereGauche = nouveauNoeud;
                    }
                    //Sinon on itère sur le reste de la chaine horizontale
                    else {
                        while (noeudActuel.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) <= 0) {
                            // Bout de chaine horizontale

                            if (noeudActuel.frereDroite == null) {
                                noeudActuel.frereDroite = nouveauNoeud;
                                nouveauNoeud.frereGauche = noeudActuel;
                                nouveauNoeud.frereDroite = null;
                                //On sort du while
                                break;
                            }

                            // On compare avec le prochain filsDroite si on est au bon endroit
                            if (noeudActuel.frereDroite.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) > 0) {
                                // On insert via un systeme de pivot entre noeudActuel et noeudActuel.frereDroite
                                ArbreFichiers ancienFrereDroite = noeudActuel.frereDroite;
                                noeudActuel.frereDroite = nouveauNoeud;
                                nouveauNoeud.frereGauche = noeudActuel;
                                nouveauNoeud.frereDroite = ancienFrereDroite;
                                ancienFrereDroite.frereGauche = nouveauNoeud;
                            }

                            noeudActuel = noeudActuel.frereDroite;
                        }
                    }


                } else {
                    this.fils.frereDroite = nouveauNoeud;
                    nouveauNoeud.frereGauche = this.fils;
                    nouveauNoeud.frereDroite = null;
                }
            }

            // Sinon, on cherche à gauche...
            else {
                ArbreFichiers noeudActuel = this.fils.frereGauche;
                if (noeudActuel != null) {
                    while (noeudActuel.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) > 0) {
                        // Bout de chaine horizontale
                        if (noeudActuel.frereGauche == null) {
                            noeudActuel.frereGauche = nouveauNoeud;
                            nouveauNoeud.frereDroite = noeudActuel;
                            nouveauNoeud.frereGauche = null;
                            //On met a jour le premier fils du pere
                            nouveauNoeud.pere.fils = nouveauNoeud;
                            //On sort du while
                            break;
                        }

                        // On compare avec le prochain filsGauche si on est au bon endroit
                        if (noeudActuel.frereGauche.nomFichier.compareToIgnoreCase(nouveauNoeud.nomFichier) <= 0) {
                            // On insert via un systeme de pivot entre noeudActuel et noeudActuel.frereGauche
                            ArbreFichiers ancienFrereGauche = noeudActuel.frereGauche;
                            noeudActuel.frereGauche = nouveauNoeud;
                            nouveauNoeud.frereDroite = noeudActuel;
                            nouveauNoeud.frereGauche = ancienFrereGauche;
                            ancienFrereGauche.frereDroite = nouveauNoeud;
                        }

                        noeudActuel = noeudActuel.frereGauche;
                    }
                } else {
                    this.fils.frereGauche = nouveauNoeud;
                    nouveauNoeud.frereDroite = this.fils;
                    nouveauNoeud.frereGauche = null;
                    //On met a jour le premier fils du pere
                    nouveauNoeud.pere.fils = nouveauNoeud;
                }
            }
        }

        // Mise a jour des tailles des dossiers
        this.tailleEnOctets += nouveauNoeud.tailleEnOctets;
        ArbreFichiers pereDuPere = this.pere;

        while (pereDuPere != null) {
            pereDuPere.tailleEnOctets += nouveauNoeud.tailleEnOctets;
            pereDuPere = pereDuPere.pere;
        }

    }

    // Méthode 2 : equivalent rm
    public void removeNoeud() {

        //Traitement selon le cas (premier fils ou non)
        if (this.pere.fils.equals(this)) this.pere.fils = this.frereDroite;
        else this.frereGauche.frereDroite = this.frereDroite;

        //Traitement commun
        this.frereDroite.frereGauche = this.frereGauche;

        // Mise a jour des tailles des dossiers
        ArbreFichiers pereDuNoeud = this.pere;

        while (pereDuNoeud != null) {
            pereDuNoeud.tailleEnOctets -= this.tailleEnOctets;
            pereDuNoeud = pereDuNoeud.pere;
        }

    }

    // Méthode 3 : equivalent ls
    public String listeInfosNoeud() {
        String result = "";

        ArbreFichiers noeud = this.fils;

        while (noeud != null) {
            //Fichier ou dossier
            String lettreType = (noeud.estFichier) ? " " : "d";
            //Nouvelle ligne contenant les information du noeud actuel
            result = result.concat(lettreType).concat("   ").concat(noeud.nomFichier).concat("   ").concat(String.valueOf(noeud.tailleEnOctets)).concat(" octets \n");
            //Mise à jour variable pour boucle while
            noeud = noeud.frereDroite;
        }

        return result;
    }

    // Méthode 4 : equivalent pwd
    public String repertoireCourant() {
        String result = "";

        ArbreFichiers noeud = this;

        while (noeud != null) {
            //Nouvelle ligne contenant les information du noeud actuel
            result = "/".concat(noeud.nomFichier).concat(result);
            //Mise à jour variable pour boucle while
            noeud = noeud.pere;
        }

        return result;
    }

    // Méthode 5 : equivalent cd
    public ArbreFichiers reference(String cheminRelatif) throws AucuneCorrespondanceCheminException {
        if (cheminRelatif.equals("..")) {
            return (this.pere == null) ? this : this.pere;
        } else {
            ArbreFichiers noeud = this.fils;

            while (noeud != null) {
                //Si le chemin correspond au noeud actuel, on renvoie alors une reference vers celui-ci
                if (cheminRelatif.equals(noeud.nomFichier)) {
                    return noeud;
                }
                //Mise à jour variable pour boucle while
                noeud = noeud.frereDroite;
            }

            // Erreur ici (aucune correspondance) -> à gerer dans les appels
            throw new AucuneCorrespondanceCheminException("Aucun dossier ne correspond à la recherche !");

        }
    }


    //Accesseurs
    public ArbreFichiers getPere() {
        return pere;
    }

    public boolean isEstFichier() {
        return estFichier;
    }

    public String getContenu() {
        return contenu;
    }

    //Nécessaires aux appels récursifs dans la fonction Main.find()
    public ArbreFichiers getFrereDroite() {
        return frereDroite;
    }

    public ArbreFichiers getFils() {
        return fils;
    }

}

