

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
	private int reabilitacao = 10;
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
				addBehaviour(new respostasUsuario());
				addBehaviour(new comprarDrogas());
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
	
	private class respostasUsuario extends CyclicBehaviour {

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
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("Sou pessoa de bem");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
	private class comprarDrogas extends CyclicBehaviour {
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		public void action() {
	
				ACLMessage order = new ACLMessage(ACLMessage.CFP);
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < pessoas.length; ++i) {
					cfp.addReceiver(pessoas[i]);
				} 
				cfp.setContent("Cade o bagulho?");
				cfp.setConversationId("Procurar Drogas.");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Procurar Drogas."),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// A resposta partiu de um policial
						if(reabilitacao > 0){
						reabilitacao--;
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Me recuperei, agora estou livre das drogas");
						myAgent.send(order);
						new PessoaComum();
						takeDown();
						}else{
							order.setPerformative(ACLMessage.REFUSE);
							order.addReceiver(reply.getSender());
							order.setContent("Não, sai de perto");
							myAgent.send(order);
							
						}
					}else if(reply.getPerformative() == ACLMessage.FAILURE){
						//Resposta partiu de um usuário
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Foi mal cara");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.AGREE){
					if(dinheiro > 0){
						dinheiro -= 100;
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Ta ai, 100 conto");
						myAgent.send(order);
					}else{
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Não me mata");
						myAgent.send(order);
						takeDown();
					}
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
