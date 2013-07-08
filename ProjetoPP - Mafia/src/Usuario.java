

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class Usuario extends Agent {
	
	private AID[] pessoas;
	private int dinheiro = 1000;
	public void setup() {
		//Adicionando usu�rio ao sistema
		System.out.println("O usu�rio: " + getAID().getName()
				+ " entrou no jogo");
		//Registrando usu�rio nas p�ginas amarelas
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
		
		//Pesquisando pessoas nas p�ginas amarelas
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
						msg.setContent("Cade o bagulho?");
						send(msg);
					}
				});

				


	}
	
	public void takeDown(){
		//Desresgistrando usu�rios das p�ginas amarelas
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("O usu�rio: " + getAID().getName()
				+ " saiu do jogo");

	}
	
	private class respostasPessoaComum extends CyclicBehaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate
					.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				
				//Recebe Mensagem de Policial
				if (title == "Encosta, vai levar bacu.") {
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent("Tudo bem");
				//Recebe Mensagem de PessoaComum
				}else if(title == "Eae cara, tudo bem?"){
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("Tudo bem, e com voc�?");
				
				//Recebe mensagem de Usu�rio
				}else if(title == "Cade o bagulho?"){
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("N�o sou traficante");
				}
				//Recebe mensagem de outro Traficante
				else if(title == "Eai mano, ta afim do bagulho?"){
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Sou pessoa de bem");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}

}
