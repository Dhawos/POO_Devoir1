package ca.uqac.poo.devoir1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by dhawo on 03/02/2017.
 */
public class Commande implements Serializable {
    private String type;
    private ArrayList<String> arguments;

    public Commande(){
        this.type = "type";
        this.arguments = new ArrayList<String>();
    }
    public Commande(String type, ArrayList<String> arguments){
        this.arguments = arguments;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArgument(int i){
        return arguments.get(i);
    }
}
