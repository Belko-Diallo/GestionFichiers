import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    private static ArbreFichiers arborescenceDeFichiers;

    public static void main(String[] args) {
        try {
            //Appel de l'initialisation de l'arborescence via fichier source si paramètre, sinon arborescence racine
            try {
                if (args[0] != null) initArborescence(args[0]);
            } catch (IndexOutOfBoundsException e) {
                arborescenceDeFichiers = new ArbreFichiers();
            }

            System.out.println("Création de l'arborescence initiale : OK !");

        } catch (IOException e) {
            System.out.println("Création de l'arborescence initiale : KO !");
            throw new FichierSourceArboNotFound("Fichier introuvable, vérifiez le chemin...");
        }

        //Appel méthode simulation Terminal Linux
        simulationTerminalLinux(arborescenceDeFichiers);

    }

    private static void simulationTerminalLinux(ArbreFichiers noeudCourant) {
        //Scanner input
        Scanner sc = new Scanner(System.in);
        String commandePassee;
        String[] commandePasseeSplit;
        boolean continueWhile = true;
        //Boucle "infini" tant que l'utilisateur n'entre pas exit ou quit
        while (continueWhile) {
            System.out.print("> ");
            commandePassee = sc.nextLine();
            commandePasseeSplit = commandePassee.split(" ");
            //On fait un switch sur le premier
            switch (commandePasseeSplit[0]) {

                //Quitte le programme
                case "quit":
                case "exit":
                    continueWhile = false;
                    break;

                //Affiche les dossiers/fichiers du noeud courant
                case "ls":
                    ls(noeudCourant);
                    break;

                //Change de répertoire selon le paramètre passé après la commande
                case "cd":
                    noeudCourant = cd(commandePasseeSplit, noeudCourant);
                    break;

                //Crée un dossier
                case "mkdir":
                    mkdir(commandePasseeSplit, noeudCourant);
                    break;

                //Crée un fichier
                case "mkfile":
                    mkfile(commandePasseeSplit, noeudCourant);
                    sc.nextLine();
                    break;

                //Affiche le contenu d'un fichier
                case "less":
                    less(commandePasseeSplit, noeudCourant);
                    break;

                //Affiche le chemin complet du repertoire courant en remontant les pères
                case "pwd":
                    pwd(noeudCourant);
                    break;

                //Supprime un noeud du repertoire courant
                case "rm":
                    rm(commandePasseeSplit, noeudCourant);
                    break;

                //find -name nomFichier
                case "find":
                    find(commandePasseeSplit);
                    break;

                //grep mot fichier
                case "grep":
                    grep(commandePasseeSplit, noeudCourant);
                    break;

                //Aucune des commandes connues --> On le signale à l'utilisateur via un message
                default:
                    System.out.println("Commande non reconnue");
                    break;

            }
        }

        System.out.println("Fin du programme !");
        sc.close();
    }

    private static void find(String[] commandePasseeSplit) {

        String nomFichierRecherche = commandePasseeSplit[1];
        //Appel fonction récursive de parcours d'arbre sur l'arborescence complete
        parcoursRecursifCorrespondance(arborescenceDeFichiers, nomFichierRecherche);

    }

    //Algo parcours d'arbre -> Si reference(noeudRecherché) != null, alors on affiche le pwd du noeudCourant
    private static void parcoursRecursifCorrespondance(ArbreFichiers noeudActuel, String nomFichierRecherche) {
        //Si le noeud contient le fichier recherché, on affiche son chemin
        try {
            if (noeudActuel.reference(nomFichierRecherche) != null) {
                System.out.println(noeudActuel.reference(nomFichierRecherche).repertoireCourant());
            }
        }
        //Si on ne trouve pas de correspondance -> on ignore l'exception
        catch (AucuneCorrespondanceCheminException ignore) {
        }

        //Si le noeud a un frereDroite, on appelle la fonction récursivement
        if (noeudActuel.getFrereDroite() != null) {
            parcoursRecursifCorrespondance(noeudActuel.getFrereDroite(), nomFichierRecherche);
        }

        //Si le noeud a un fils, on appelle la fonction récursivement
        if (noeudActuel.getFils() != null) {
            parcoursRecursifCorrespondance(noeudActuel.getFils(), nomFichierRecherche);
        }

        //L'algorithme n'est plus appelé récursivement lors du dernier appel, sur le noeud tout en bas à droite de l'arborescence (pas de fils ni de frereDroite)

    }

    private static void ls(ArbreFichiers noeudCourant) {
        System.out.println(noeudCourant.listeInfosNoeud());
    }

    private static ArbreFichiers cd(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        try {
            //Si le chemin recherché est un dossier -> noeudCourant prend sa valeur
            if (!noeudCourant.reference(commandePasseeSplit[1]).isEstFichier()) {
                return noeudCourant.reference(commandePasseeSplit[1]);
            }
            //Si le chemin recherché est un fichier -> On ne fait rien et on le signale à l'utilisateur
            else {
                System.out.println("Vous essayez d'utiliser la méthode 'cd' pour accéder à un fichier ! Utilisez plutôt 'less'...");
            }
        } catch (AucuneCorrespondanceCheminException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de dossier en paramètre !");
        }
        return noeudCourant;
    }

    private static void mkdir(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        try {
            String nomDossier = commandePasseeSplit[1];
            int numDossierIter = 0;
            String extensionNumDossier = "";
            //On test si il existe déjà un noeud avec un nom equivalent
            try {

                //Boucle infini, la seule sortie est l'exception AucuneCorrespondanceCheminException nous informant qu'il n'existe aucun noeud ayant le meme nom
                while (true) {
                    //On remet à "zéro" le nom du noeud recherché
                    nomDossier = commandePasseeSplit[1];
                    //Si c'est la premiere itération, pas d'extension. Sinon, extension de la forme (1), (2), ...
                    extensionNumDossier = (numDossierIter == 0) ? "" : "(" + numDossierIter + ")";
                    //On recherche la reference
                    //Si il la trouve une correspondance, on continue la boucle
                    //Sinon, l'exception AucuneCorrespondanceCheminException est levée
                    noeudCourant.reference(nomDossier.concat(extensionNumDossier));
                    //Si on continue la boucle, on itère numDossierIter pour continuer de rechercher un nom disponible
                    numDossierIter++;
                }

            } catch (AucuneCorrespondanceCheminException e) {
                //Ajout du dossier !
                noeudCourant.addNoeud(new ArbreFichiers(noeudCourant, nomDossier.concat(extensionNumDossier), false, null));
            }

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de dossier en paramètre !");
        }
    }

    private static void mkfile(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        Scanner scMk = new Scanner(System.in);
        System.out.println("Merci de saisir le contenu du fichier :");
        String contenuTotal = "";
        String ligneTexte = "";
        boolean demandeNouvelleLigne;
        try {
            do {
                ligneTexte = scMk.nextLine(); //On accepte la saisie d'une nouvelle ligne
                if (ligneTexte.substring(ligneTexte.length() - 3).equals("___")) {//L'utilisateur a demandé une nouvelle ligne via "___"
                    demandeNouvelleLigne = true;
                    /////////////////////////////////////////// (cf case "less")
                    ligneTexte = ligneTexte.substring(0, ligneTexte.length() - 3).concat("\n");  //On remplace les "___" par un retour à la ligne : "\n"
                    ///////////////////////////////////////////

                } else {
                    //Si l'utilisateur n'a pas demandé de nouvelle ligne, on sort du while à la fin de cette itération
                    demandeNouvelleLigne = false;
                }
                //On ajoute la ligne au contenu total
                contenuTotal = contenuTotal.concat(ligneTexte);
            } while (demandeNouvelleLigne); //Tant que l'utilisateur demande une ligne
        } catch (IndexOutOfBoundsException ignore) {
            //Cette exception est declanchée lorsque le contenu du fichier ne fait pas plus de 2 caractères
            //On ajoute simplement la ligne de moins de deux caractères à notre contenu total
            contenuTotal = contenuTotal.concat(ligneTexte);
        }

        //AJOUT DU FICHIER !
        try {
            String nomFichier = commandePasseeSplit[1];
            int numFichierIter = 0;
            String extensionNumFichier = "";

            //On test si il existe déjà un noeud avec un nom equivalent
            try {

                //Boucle infini, la seule sortie est l'exception AucuneCorrespondanceCheminException nous informant qu'il n'existe aucun noeud ayant le meme nom
                while (true) {
                    //On remet à "zéro" le nom du noeud recherché
                    nomFichier = commandePasseeSplit[1];
                    //Si c'est la premiere itération, pas d'extension. Sinon, extension de la forme (1), (2), ...
                    extensionNumFichier = (numFichierIter == 0) ? "" : "(" + numFichierIter + ")";
                    //On recherche la reference
                    //Si il la trouve une correspondance, on continue la boucle
                    //Sinon, l'exception AucuneCorrespondanceCheminException est levée
                    noeudCourant.reference(nomFichier.concat(extensionNumFichier));
                    //Si on continue la boucle, on itère numFichierIter pour continuer de rechercher un nom disponible
                    numFichierIter++;
                }

            } catch (AucuneCorrespondanceCheminException e) {
                //Ajout du dossier !
                noeudCourant.addNoeud(new ArbreFichiers(noeudCourant, nomFichier.concat(extensionNumFichier), true, contenuTotal));
            }

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de dossier en paramètre !");
        }
    }

    private static void less(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        try {
            //Les "___" sont gérés lors de l'ajout du contenu (cf. case "mkfile")
            //Ceci permet à l'utilisateur d'écrire du texte contenant "___" sans pour autant les remplacer
            System.out.println(noeudCourant.reference(commandePasseeSplit[1]).getContenu());
            //Si on veut le gérer ici, on peut replacer la ligne du dessus par la suivante :
            //System.out.println(noeudCourant.reference(commandePasseeSplit[1]).getContenu().replaceAll("___", "\n"));
            //Et supprimer la section entre /////////////////////////////////////////// sur le case "mkfile"
        } catch (AucuneCorrespondanceCheminException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de fichier/dossier en paramètre !");
        }
    }

    private static void pwd(ArbreFichiers noeudCourant) {
        System.out.println(noeudCourant.repertoireCourant());
    }

    private static void rm(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        try {
            //Le noeud à supprimer est recherché via la méthode reference()
            noeudCourant.reference(commandePasseeSplit[1]).removeNoeud();
        } catch (AucuneCorrespondanceCheminException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de fichier/dossier en paramètre !");
        }
    }

    private static void grep(String[] commandePasseeSplit, ArbreFichiers noeudCourant) {
        try {
            //On récupère le fichier recherché
            ArbreFichiers fichierF = noeudCourant.reference(commandePasseeSplit[2]);
            //On crée un Scanner sur le contenu
            Scanner scanner = new Scanner(fichierF.getContenu());
            try {
                String ligne = scanner.nextLine();
                //Boucle infini, seul sortie étant l'exception de fin de fichier
                while (true) {
                    //Si la ligne contient le mot recherché, on l'affiche
                    if (ligne.contains(commandePasseeSplit[1])) {
                        System.out.println(" - " + ligne);
                    }

                    //On passe à la ligne suivante
                    ligne = scanner.nextLine();
                }
            } catch (NoSuchElementException ignore) {
                //Fin de fichier, on ignore
            }

            scanner.close();
        } catch (AucuneCorrespondanceCheminException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Veuillez passer un nom de fichier/dossier en paramètre !");
        }
    }

    private static void initArborescence(String filePath) throws IOException {
        BufferedReader lecteur = new BufferedReader(new FileReader(filePath));

        //Première ligne
        String ligne = lecteur.readLine();
        assert ligne.equals("racine") : "Le fichier source doit commencer par 'racine' et finir par 'fin' !";

        String[] splitLigne;

        //Variables dont on a besoin pour l'initialisation de l'arborescence
        String titre, type;
        ArbreFichiers arborescenceTotale = new ArbreFichiers();
        ArbreFichiers dossierParentActuel = arborescenceTotale;

        //On itère sur le fichier source tant qu'on n'a pas atteint la derniere ligne
        while (!ligne.equals("fin")) {
            titre = "";
            type = "";

            splitLigne = ligne.split(" ");

            //Si il s'agit d'une ligne de déclaration de fichier/dossier ou de 'fin'
            if (splitLigne[0].charAt(0) == '*') {
                //Si creation fichier/dossier
                if (!splitLigne[1].equals("fin")) {
                    titre = splitLigne[1];
                    type = splitLigne[2];
                }
                //Sinon c'est un 'fin', on remonte d'un cran de dossier
                else {
                    dossierParentActuel = dossierParentActuel.getPere();
                }

            }

            switch (type) {
                case "f":
                    //Creation fichier et readLine pour le contenu présent sur la ligne suivante)
                    dossierParentActuel.addNoeud(new ArbreFichiers(dossierParentActuel, titre, true, lecteur.readLine().replace("___", "\n")));
                    break;
                case "d":
                    //Creation dossier et changement de repertoire actuel (cd nouveauDossier)
                    ArbreFichiers nouveauDossier = new ArbreFichiers(dossierParentActuel, titre, false, null);
                    dossierParentActuel.addNoeud(nouveauDossier);
                    dossierParentActuel = nouveauDossier;
                    break;
                default:
                    break;
            }


            ligne = lecteur.readLine();

        }


        arborescenceDeFichiers = arborescenceTotale;
        lecteur.close();
    }


}

