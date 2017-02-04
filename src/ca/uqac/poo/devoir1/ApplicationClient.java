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
    private String hostname;
    private int port;

    private BufferedReader commandesReader;
    //private BufferedWriter sortieWriter2;

    /**
     * prend le fichier contenant la liste des commandes, et le charge dans une
     * variable du type Commande qui est retournée
     */
    public Commande saisisCommande(BufferedReader fichier) {
        try{
            String line = fichier.readLine();
            if (line == null){
                return null;
            }
            ArrayList<String> arguments = new ArrayList<String>();
            String[] parts = line.split("#");
            String type =  parts[0];
            int i = 1;
            while (i < parts.length){
                arguments.add(parts[i]);
                i++;
            }
            if (type == "fonction"){
                type = "appel";
            }

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
            Socket clientSocket = new Socket(this.hostname, this.port);
            OutputStream os = clientSocket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(uneCommande);
            oos.close();
            os.close();


            InputStream is = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            Object result = ois.readObject();

            int i =0;
            while(result == null && i < 100){
                result = ois.readObject();
                Thread.sleep(10);
                i++;
            }

            String type = uneCommande.getType();
            ois.close();
            is.close();
            clientSocket.close();

            if (result == null){
                return null;
            }
            switch (type){
                case "lecture":
                    return result;
                case "ecriture":
                    return (Boolean)result;
                case "creation":
                    return (Boolean)result;
                case "chargement":
                    return (Boolean)result;
                case "compilation":
                    return (Boolean)result;
                case "appel":
                    if (result instanceof Boolean){
                        return (Boolean)result;
                    }else{
                        return result;
                    }
            }


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

        applicationClient.hostname = args[0];
        applicationClient.port = Integer.parseInt(args[1]);
        String commandes = args[2];
        String sortie = args[3];
        applicationClient.initialise(commandes, sortie);
        applicationClient.scenario();


    }
}
