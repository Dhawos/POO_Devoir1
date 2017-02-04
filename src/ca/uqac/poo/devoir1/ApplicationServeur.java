package ca.uqac.poo.devoir1;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by dhawo on 03/02/2017.
 */
public class ApplicationServeur {
    private ServerSocket welcomeSocket = null;
    private String sourceFolder; //Chemin vers dossier des sourcs
    private String classFolder; //Chemin vers dossier de classes
    private String outputFile; //
    private Socket connectionSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HashMap<String,Object> objects;
    private ClassLoader loader;
    /**
     * prend le numéro de port, crée un SocketServer sur le port
     */
    public ApplicationServeur (int port){
        try{
            welcomeSocket = new ServerSocket(port);
            objects = new HashMap<>();
        }catch(IOException ex){
            log("Le Socket n'as pas pu être bindé sur le port donné.");
        }
    }

    /**
     * Se met en attente de connexions des clients. Suite aux connexions, elle lit
     * ce qui est envoyé à travers la Socket, recrée l’objet Commande envoyé par
     * le client, et appellera traiterCommande(Commande uneCommande)
     */
    public void aVosOrdres() {
        while(true){
            try{
                log("En attente de connection");
                connectionSocket = welcomeSocket.accept(); //Accepte la connexion (une seule à la fois dans ce cas)
                log("Connexion acceptee venant de : " + connectionSocket.getInetAddress() + ":" + connectionSocket.getPort());
                in = new ObjectInputStream(connectionSocket.getInputStream());
                out = new ObjectOutputStream(connectionSocket.getOutputStream());
                out.flush();
                Commande commande = (Commande)in.readObject(); //Récupération de la commande
                traiteCommande(commande); //Traitement de la commande
                out.close();
                in.close();
                connectionSocket.close();
                log("Connexion fermee avec : " + connectionSocket.getInetAddress() + ":" + connectionSocket.getPort());
            }catch(IOException ex){
                System.out.println("La connexion n'as pas pu etre acceptée");
            }catch(ClassNotFoundException ex){
                System.out.println("Le message recu n'est pas une commande");
            }
        }
    }

    /**
     * prend uneCommande dument formattée, et la traite. Dépendant du type de commande,
     * elle appelle la méthode spécialisée
     */
    public void traiteCommande(Commande uneCommande) {
        switch(uneCommande.getType()){
            case "lecture":
                String id_read = uneCommande.getArgument(0);
                String attribut_read = uneCommande.getArgument(1);
                Object pointeurObject_read = objects.get(id_read);
                traiterLecture(pointeurObject_read,attribut_read);
                break;
            case "ecriture":
                String id_write = uneCommande.getArgument(0);
                String attribut_write = uneCommande.getArgument(1);
                String valeur_write = uneCommande.getArgument(2);
                Object pointeurObject_write = objects.get(id_write);
                traiterEcriture(pointeurObject_write,attribut_write,valeur_write);
                break;
            case "creation":
                String nomQualifie_create = uneCommande.getArgument(0);
                String id_create = uneCommande.getArgument(1);
                try{
                    traiterCreation(Class.forName(nomQualifie_create,true,loader),id_create);
                }catch(ClassNotFoundException ex){
                    log(ex.getMessage());
                }
                break;
            case "chargement":
                String nomQualifie = uneCommande.getArgument(0);
                traiterChargement(nomQualifie);
                break;
            case "compilation":
                String sourceFiles = uneCommande.getArgument(0);
                String classPath = uneCommande.getArgument(1);
                traiterCompilation(sourceFiles);
                break;
            case "fonction":
                String id_call = uneCommande.getArgument(0);
                Object obj = objects.get(id_call);
                String nom_fonction = uneCommande.getArgument(1);
                ArrayList<String> valeurs = new ArrayList<>();
                ArrayList<Object> types = new ArrayList<>();
                if(uneCommande.getNbArguments() == 3){
                    String[] parametres = uneCommande.getArgument(2).split(",");
                    for(String parametre : parametres){
                        String[] splitted_param = parametre.split(":");
                        types.add(splitted_param[0]);
                        valeurs.add(splitted_param[1]);
                    }
                }
                String[] types_array = new String[types.size()];
                types.toArray(types_array);
                traiterAppel(obj,nom_fonction, types_array,valeurs.toArray());
                break;
        }
    }

    /**
     * traiterLecture : traite la lecture d’un attribut. Renvoies le résultat par le
     * socket
     */
    public void traiterLecture(Object pointeurObjet, String attribut) {
        Class c = pointeurObjet.getClass();
        Object value = null;
        try{
            Field field = c.getDeclaredField(attribut);
            if(Modifier.isPrivate(field.getModifiers())){
                String upperAttribute = attribut.substring(0, 1).toUpperCase() + attribut.substring(1);
                Method getter = c.getMethod("get" + upperAttribute);
                value = field.getType().cast(getter.invoke(pointeurObjet,null));
            }else{
                value = (field.getType().cast(field.get(pointeurObjet)));
            }
            out.writeObject(value);
            out.flush();
        }
        catch(NoSuchFieldException ex){
            log(ex.getMessage());
        }
        catch (NoSuchMethodException ex){
            log(ex.getMessage());
        }
        catch(InvocationTargetException ex){
            log(ex.getMessage());
        }
        catch(IllegalAccessException ex){
            log(ex.getMessage());
        }
        catch(IOException ex){
            log(ex.getMessage());
        }
    }

    /**
     * traiterEcriture : traite l’écriture d’un attribut. Confirmes au client que l’écriture
     * s’est faite correctement.
     */
    public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur) {
        Class c = pointeurObjet.getClass();
        try{
            Field field = c.getDeclaredField(attribut);
            if(Modifier.isPrivate(field.getModifiers())){
                String upperAttribute = attribut.substring(0, 1).toUpperCase() + attribut.substring(1);
                Method setter = c.getMethod("set" + upperAttribute, field.getType());
                setter.invoke(pointeurObjet,valeur);
            }else{
                field.set(pointeurObjet,valeur);
            }
            out.writeObject(new Boolean(true));
            out.flush();
        }
        catch(NoSuchFieldException ex){
            log(ex.getMessage());
        }
        catch (NoSuchMethodException ex){
            log(ex.getMessage());
        }
        catch(InvocationTargetException ex){
            log(ex.getMessage());
        }
        catch(IllegalAccessException ex){
            log(ex.getMessage());
        }
        catch(IOException ex){
            log(ex.getMessage());
        }
    }

    /**
     * traiterCreation : traite la création d’un objet. Confirme au client que la création
     * s’est faite correctement.
     */
    public void traiterCreation(Class classeDeLobjet, String identificateur) {
        try{
            Object newObject = classeDeLobjet.newInstance();
            objects.put(identificateur,newObject);
            out.writeObject(new Boolean(true));
            out.flush();
        }catch(InstantiationException ex){
            log(ex.getMessage());
        }catch(IllegalAccessException ex){
            log(ex.getMessage());
        }catch (IOException ex){
        log(ex.getMessage());
        }
    }

    /**
     * traiterChargement : traite le chargement d’une classe. Confirmes au client que la création
     * s’est faite correctement.
     */
    public void traiterChargement(String nomQualifie) {
        try{
            loader.loadClass(nomQualifie);
            out.writeObject(new Boolean(true));
            out.flush();
        }catch (IOException ex){
            log(ex.getMessage());
        }catch (ClassNotFoundException ex){
            log(ex.getMessage());
        }
    }

    /**
     * traiterCompilation : traite la compilation d’un fichier source java. Confirme au client
     * que la compilation s’est faite correctement. Le fichier source est donné par son chemin
     * relatif par rapport au chemin des fichiers sources.
     */
    public void traiterCompilation(String cheminRelatifFichierSource) {
        try{
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String[] sourceFileNames = cheminRelatifFichierSource.split(",");
            ArrayList<File> fileArrayList = new ArrayList<>();
            for(String filename : sourceFileNames){
                fileArrayList.add(new File(sourceFolder + File.separator + filename));
            }
            StandardJavaFileManager sjfm = compiler.getStandardFileManager(null, null, null);

            String[] options = new String[] { "-d", classFolder };
            File[] javaFiles = new File[fileArrayList.size()];
            fileArrayList.toArray(javaFiles);
            JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, null, null,
                    Arrays.asList(options),
                    null,
                    sjfm.getJavaFileObjects(javaFiles)
            );

            compilationTask.call();


            out.writeObject(new Boolean(true));
            out.flush();
        }catch (IOException ex){
            log(ex.getMessage());
        }

    }

    /**
     * traiterAppel : traite l’appel d’une méthode, en prenant comme argument l’objet
     * sur lequel on effectue l’appel, le nom de la fonction à appeler, un tableau de nom de
     * types des arguments, et un tableau d’arguments pour la fonction. Le résultat de la
     * fonction est renvoyé par le serveur au client (ou le message que tout s’est bien
     * passé)
     */
    public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types, Object[] valeurs){
        Method method = null;
        try{
            Class c = pointeurObjet.getClass();
            ArrayList<Class> classes = new ArrayList<>();
            for(String type : types){
                try{
                    classes.add(Class.forName(type,true,loader));
                }
                catch(ClassNotFoundException ex){
                    if(type.equals("float")){
                        classes.add(float.class);
                    }
                }
            }
            Class[] types_array = new Class[classes.size()];
            classes.toArray(types_array);
            for(int i = 0; i < valeurs.length; i++){
                Object valeur = valeurs[i];
                if(valeur instanceof String){
                    String str = (String)valeur;
                    if(str.matches("ID\\(.*\\)")){
                        String id = ((String) valeur).substring(3,((String) valeur).length()-1);
                        valeurs[i] = types_array[i].cast(objects.get(id));
                    }else if(types_array[i].equals(float.class)){
                        valeurs[i] = Float.parseFloat((String)valeur);
                    }
                }
            }
            method = c.getMethod(nomFonction,types_array);
            Object returnValue = method.invoke(pointeurObjet,valeurs);
            out.writeObject(returnValue);
            out.flush();
        }catch(NoSuchMethodException ex){
            log(ex.getMessage());
        }catch(InvocationTargetException ex){
            try{
                out.writeObject(null);
                out.flush();
                log("La méthode appellée : " + method.getName() + " a renvoyé une exception : " + ex.getTargetException().getMessage());
            }catch (IOException e){
                log(e.getMessage());
            }
        }catch(IllegalAccessException ex){
            log(ex.getMessage());
        }catch(IOException ex){
            log(ex.getMessage());
        }
    }

    /**
     * programme principal. Prend 4 arguments: 1) numéro de port, 2) répertoire source, 3)
     * répertoire classes, et 4) nom du fichier de traces (sortie)
     * Cette méthode doit créer une instance de la classe ApplicationServeur, l’initialiser
     * puis appeler aVosOrdres sur cet objet
     */
    public static void main(String[] args) {
        if(args.length != 4){
            System.out.println("Mauvais arguments");
            System.out.println("Utilisation : ");
            System.out.println("1) numéro de port, 2) répertoire source, 3) répertoire classes, et 4) nom du fichier de traces (sortie)");
            return;
        }
        int port = Integer.parseInt(args[0]);
        ApplicationServeur serveur = new ApplicationServeur(port);
        serveur.sourceFolder = args[1];
        serveur.classFolder = args[2];
        serveur.outputFile = args[3];
        File file = new File(serveur.classFolder);
        try{
            // Convert File to a URL
            URL url = file.toURI().toURL();
            URL[] urls = new URL[] { url };
            serveur.loader = new URLClassLoader(urls);
        }catch (MalformedURLException ex){
            serveur.log(ex.getMessage());
        }
        if(serveur.welcomeSocket != null){
            serveur.aVosOrdres();
        }
    }

    private void log(String message){
        try
        {
            System.out.println(LocalDateTime.now().toString() + " : " + message);
            System.out.println("\r\n");
            FileWriter fw = new FileWriter(outputFile);
            fw.write (LocalDateTime.now().toString() + " : " + message);
            fw.write ("\r\n");
            fw.close();
        }
        catch (IOException exception)
        {
            System.out.println ("Erreur lors de l'écriture de l'erreur dans le fichier de log : " + exception.getMessage());
        }
    }
}