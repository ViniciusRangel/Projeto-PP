

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
public class PessoaComum extends Agent {
	Usuario usuario;
	private AID[] pessoas;
	private int ofertasDeCompra = 10;
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
		
		//Pesquisando pessoas nas páginas amarelas
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
				addBehaviour(new respostasPessoaComum());
				addBehaviour(new caminharPelaCidade());
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
					reply.setContent("Tudo bem, e com você?");
				
				//Recebe mensagem de Usuário
				}else if(title == "Cade o bagulho?"){
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Não sou traficante");
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
	
	private class caminharPelaCidade extends CyclicBehaviour {
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		public void action() {
		
				// Send the cfp to all sellers
				ACLMessage order = new ACLMessage(ACLMessage.CFP);
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < pessoas.length; ++i) {
					cfp.addReceiver(pessoas[i]);
				} 
				cfp.setContent("Eae cara, tudo bem?");
				cfp.setConversationId("Conversa");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Conversa"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						if(ofertasDeCompra > 0){
							ofertasDeCompra--;
							order.setPerformative(ACLMessage.REFUSE);
							order.addReceiver(reply.getSender());
							order.setContent("Não, obrigado");
							myAgent.send(order);
						}else{
							order.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							order.addReceiver(reply.getSender());
							order.setContent("Virei usuario");
							myAgent.send(order);
							new Usuario();
							takeDown();	
						}
							
						}else if(reply.getPerformative() == ACLMessage.AGREE){
							order.setPerformative(ACLMessage.CONFIRM);
							order.addReceiver(reply.getSender());
							order.setContent("Bom dia");
						}
					repliesCnt++;
					if (repliesCnt >= pessoas.length) {
						// We received all replies
					
					}
				}
				else {
					block();
				}
		}
	
	} 
}
