

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
public class Policial extends Agent {

	private AID[] pessoas;	
	public void setup() {
		// Adicinando um policial
		System.out.println("O policial: " + getAID().getName()
				+ " entrou no jogo");
		// Registrando um policial nas páginas amarelas
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Policial");
		sd.setName("Policial");
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
				addBehaviour(new respostasPolicial());
				addBehaviour(new investigar());
			}
		});
		
	}

	public void takeDown() {
		// Desregistrando das páginas amarelas
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("O policial: " + getAID().getName()
				+ " saiu do jogo");

	}
	public class respostasPolicial extends CyclicBehaviour {

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
						reply.setPerformative(ACLMessage.CANCEL);
						reply.setContent("Eae Brother, hoje o serviço ta puxado");
				//Recebe Mensagem de PessoaComum
				}else if(title == "Eae cara, tudo bem?"){
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("Sim, e você?");
				//Recebe mensagem de Usuário
				}else if(title == "Cade o bagulho?"){
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("Larga das drogas brother");
				//Recebe mensagem do traficante
				}else if(title == "Eai mano, ta afim do bagulho?"){
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Ta preso seu safado");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
	
	private class investigar extends CyclicBehaviour {
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		public void action() {
	
				ACLMessage order = new ACLMessage(ACLMessage.CFP);
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < pessoas.length; ++i) {
					cfp.addReceiver(pessoas[i]);
				} 
				cfp.setContent("Encosta, vai levar bacu.");
				cfp.setConversationId("Abordado");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Abordado"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.CANCEL) {
						// A resposta partiu de um policial
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Foi mal mano, somos irmãos de farda");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.REFUSE){
						//Resposta partiu de um usuário
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Se vir um traficante me avisa");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
						//Resposta partiu de uma pessoa comum
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Pego no flagrante, vai em cana");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.AGREE){
						//Resposta partiu de outro traficante
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Você deveria fazer denuncias");
						myAgent.send(order);
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
