

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Usuario extends Agent {
	
	private AID[] pessoas;
	
	public void setup() {
		//Adicionando usuário ao sistema
		System.out.println("O usuário: " + getAID().getName()
				+ " entrou no jogo");
		//Registrando usuário nas páginas amarelas
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
		// Enviando mensagens de conversa
				addBehaviour(new TickerBehaviour(this, 10000) {
					protected void onTick() {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						for (int i = 0; i < pessoas.length; ++i) {
							msg.addReceiver(new AID(pessoas[i].getName(),
									AID.ISLOCALNAME));
						}
						msg.setLanguage("Portuguese");
						msg.setOntology("Procurar Drogas.");
						msg.setContent("Cade o bagulho?.");
						send(msg);
					}
				});

				addBehaviour(new TickerBehaviour(this, 10000) {
					protected void onTick() {

						DFAgentDescription dfd = new DFAgentDescription();
						dfd.setName(getAID());
						try {
							DFAgentDescription[] result = DFService
									.search(myAgent, dfd);
							pessoas = new AID[result.length];
							for (int i = 0; i < result.length; ++i) {
								pessoas[i] = result[i].getName();
							}
						} catch (FIPAException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});


	}
	
	public void takeDown(){
		//Desresgistrando usuários das páginas amarelas
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("O usuário: " + getAID().getName()
				+ " saiu do jogo");

	}

}
