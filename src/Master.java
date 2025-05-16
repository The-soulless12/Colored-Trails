import java.util.*;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Master extends Agent {
    private List<String> agentNames;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof List) {
            List<?> rawList = (List<?>) args[0];
            agentNames = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof String) {
                    agentNames.add((String) item);
                }
            }
        } else {
            agentNames = new ArrayList<>();
        }

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent("jour");
        for (String name : agentNames) {
            message.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        send(message);
        doDelete();
    }
}
