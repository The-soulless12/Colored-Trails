import jade.core.Agent;

public class Main extends Agent {
    protected void setup() {
        System.out.println("Bonjour ! Je suis " + getLocalName());
    }
}
