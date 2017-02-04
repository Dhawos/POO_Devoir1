package ca.uqac.poo.devoir1;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import ca.uqac.poo.devoir1.Commande;

/**
 * Created by dhawo on 03/02/2017.
 */
public class ApplicationClient {
    java.io.PrintStream sortieWriter = System.out;

    private BufferedReader commandesReader;
    //private BufferedWriter sortieWriter2;

    /**
     * prend le fichier contenant la liste des commandes, et le charge dans une
     * variable du type Commande qui est retournée
     */
    public Commande saisisCommande(BufferedReader fichier) {
        try{
            String line = fichier.readLine();
            ArrayList<String> arguments = new ArrayList<String>();
            String[] parts = line.split("#");
            String type =  parts[0];
            String part2 = parts[1];

            while (part2.contains("#")) {
                parts = part2.split("#");
                arguments.add(parts[0]);
                part2 = parts[1];
            }
            arguments.add(parts[1]);
            /**
            switch (type){
                case "compilation":
                    while (part2.contains(",")) {
                        parts = part2.split(",");   //chemin relatif du fichier
                        arguments.add(parts[0]);    //source 1 à n-1
                        part2 = parts[1];
                    }
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //chemin relatif du fichier source n
                    arguments.add(parts[1]);    //chemin relatif des fichiers class
                    break;

                case "chargement":
                    arguments.add(part2);   //nom qualifie de classe
                    break;

                case "creation":
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //nom classe
                    arguments.add(parts[1]);    //identificateur
                    break;

                case "lecture":
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //identificateur
                    arguments.add(parts[1]);    //nom attribut
                    break;

                case "ecriture":
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //identificateur

                    part2 = parts[1];
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //nom attribut
                    arguments.add(parts[1]);    //valeur
                    break;

                case "fonction":
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //identificateur

                    part2 = parts[1];
                    parts = part2.split("#");
                    arguments.add(parts[0]);    //nom fonction

                    part2 = parts[1];           //liste des parametres
                    while (part2.contains(",")) {
                        parts = part2.split(",");   //chemin relatif du fichier
                        arguments.add(parts[0]);    //source 1 à n-1
                        part2 = parts[1];
                    }
                    arguments.add(part2);
                    break;
            }
            return new Commande(type, arguments);
            */
            return new Commande(type, arguments);
        }catch (IOException e){
            e.printStackTrace();
        }

        return new Commande();

    }

    /**
     * initialise : ouvre les différents fichiers de lecture et écriture
     */
    public void initialise(String fichCommandes, String fichSortie) {
        try {
            FileReader fileReader;
            fileReader = new FileReader(fichCommandes);
            this.commandesReader = new BufferedReader(fileReader);
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" );
        }
        catch(IOException ex) {
            System.out.println("Error reading file '");
        }

        try {
            /**FileWriter fileWriter = new FileWriter(fichSortie);
            this.sortieWriter2 = new BufferedWriter(fileWriter);
             */
            sortieWriter = new PrintStream(fichSortie);
        }
        catch(IOException ex) {
            System.out.println("Error writing to file '");
        }
    }

    /**
     * prend une Commande dûment formatée, et la fait exécuter par le serveur. Le résultat de
     * l’exécution est retournée. Si la commande ne retourne pas de résultat, on retourne null.
     * Chaque appel doit ouvrir une connexion, exécuter, et fermer la connexion. Si vous le
     * souhaitez, vous pourriez écrire six fonctions spécialisées, une par type de commande
     * décrit plus haut, qui seront appelées par  traiteCommande(Commande uneCommande)
     */
    public Object traiteCommande(Commande uneCommande) {
        try{
            Socket clientSocket = new Socket("hostname", 6789);
            OutputStream os = clientSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(uneCommande);
            oos.close();
            os.close();

            InputStream is = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            Object objectToReturn = ois.readObject();
            ois.close();
            is.close();
            clientSocket.close();

            return objectToReturn;


        }catch(Exception e){
            System.out.println(e);
        }
        return new Object();
    }



    /**
     * cette méthode vous sera fournie plus tard. Elle indiquera la séquence d’étapes à exécuter
     * pour le test. Elle fera des appels successifs à saisisCommande(BufferedReader fichier) et
     * traiteCommande(Commande uneCommande).
     */
    public void scenario() {
        sortieWriter.println("Debut des traitements:");
        Commande prochaine = saisisCommande(commandesReader);
        while (prochaine != null) {
            sortieWriter.println("\tTraitement de la commande " + prochaine + " ...");
            Object resultat = traiteCommande(prochaine);
            sortieWriter.println("\t\tResultat: " + resultat);
            prochaine = saisisCommande(commandesReader);
        }
        sortieWriter.println("Fin des traitements");
    }

    /**
     * programme principal. Prend 4 arguments: 1) “hostname” du serveur, 2) numéro de port,
     * 3) nom fichier commandes, et 4) nom fichier sortie. Cette méthode doit créer une
     * instance de la classe ApplicationClient, l’initialiser, puis exécuter le scénario
     */
    public static void main(String[] args) {
        ApplicationClient applicationClient = new ApplicationClient();

        applicationClient.initialise("commandes.txt", "sortie.txt");
        applicationClient.scenario();


    }
}
