package ca.uqac.poo.devoir1;

import java.io.BufferedReader;

/**
 * Created by dhawo on 03/02/2017.
 */
public class ApplicationClient {

    /**
     * prend le fichier contenant la liste des commandes, et le charge dans une
     * variable du type Commande qui est retournée
     */
    public Commande saisisCommande(BufferedReader fichier) {

    }

    /**
     * initialise : ouvre les différents fichiers de lecture et écriture
     */
    public void initialise(String fichCommandes, String fichSortie) {

    }

    /**
     * prend une Commande dûment formatée, et la fait exécuter par le serveur. Le résultat de
     * l’exécution est retournée. Si la commande ne retourne pas de résultat, on retourne null.
     * Chaque appel doit ouvrir une connexion, exécuter, et fermer la connexion. Si vous le
     * souhaitez, vous pourriez écrire six fonctions spécialisées, une par type de commande
     * décrit plus haut, qui seront appelées par  traiteCommande(Commande uneCommande)
     */
    public Object traiteCommande(Commande uneCommande) {

    }


    /**
     * cette méthode vous sera fournie plus tard. Elle indiquera la séquence d’étapes à exécuter
     * pour le test. Elle fera des appels successifs à saisisCommande(BufferedReader fichier) et
     * traiteCommande(Commande uneCommande).
     */
    public void scenario() {

    }

    /**
     * programme principal. Prend 4 arguments: 1) “hostname” du serveur, 2) numéro de port,
     * 3) nom fichier commandes, et 4) nom fichier sortie. Cette méthode doit créer une
     * instance de la classe ApplicationClient, l’initialiser, puis exécuter le scénario
     */
    public static void main(String[] args) {

    }
}
