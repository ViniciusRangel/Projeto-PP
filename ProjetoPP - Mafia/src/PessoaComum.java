

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class PessoaComum extends Agent {

	private AID[] pessoas;

	public void setup() {
		// Adicionando uma pessoa Comum
		System.out.println("A Pessoa Comum: " + getAID().getName()
				+ " entrou no jogo");
		// Registrando pessoas nas páginas amarelas
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Pessoa Comum");
		sd.setName("Pessoa Comum");
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
				msg.setOntology("Conversa");
				msg.setContent("Eae cara, tudo bem?");
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

	public void takeDown() {
		// Desresgistrando das páginas amarelas
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("A Pessoa Comum: " + getAID().getName()
				+ "saiu do jogo");
	}
}
