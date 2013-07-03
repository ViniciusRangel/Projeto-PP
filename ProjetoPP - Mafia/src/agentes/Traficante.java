package agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Traficante extends Agent {
	private AID[] pessoas;
	public void setup() {
		System.out.println("O traficante: " + getAID().getName()
				+ " entrou no jogo");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Traficante");
		sd.setName("Traficante");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
				
		addBehaviour(new TickerBehaviour(this, 10000){
			protected void onTick(){
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID(" ", AID.ISLOCALNAME));
				msg.setLanguage("Portuguese");
				msg.setOntology("Vender Drogas.");
				msg.setContent("Tá afim dum bagulho?");
				send(msg);
			}
		});
			
		addBehaviour(new TickerBehaviour(this, 10000){
			protected void onTick(){
				//DFAgentDescrition[] result = DFService.se 
			}
		});
		
		
	}
	

	public void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("O traficante: " + getAID().getName()
				+ " saiu do jogo");

	}

}
