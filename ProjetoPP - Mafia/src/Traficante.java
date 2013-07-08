
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
public class Traficante extends Agent {
	// Pessoas nas paginas amarelas
	private AID[] pessoas;
	//Tempo que o traficante fica vulneravel a captura do policial
	int tempoDeCaptura;
	public void setup() {
	
		// Inicializando um Traficante

		System.out.println("O traficante: " + getAID().getName()
				+ " entrou no jogo");

		// Registrando nas páginas amarelas

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
		//Pesquisar Pessoas nas Páginas Amarelas
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
				myAgent.addBehaviour(new venderDrogas());
				myAgent.addBehaviour(new respostasTraficante());
				
			}
		});
		
		
	}

	public void takeDown() {
		// Desresgistrando das páginas amarelas
		System.out.println("O traficante: " + getAID().getName()
				+ " saiu do jogo");

		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}

	
	private class respostasTraficante extends CyclicBehaviour {

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
					if(tempoDeCaptura - System.currentTimeMillis() > 2000){
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("É doido é tio, sô trabaiadô");
					}else{
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setContent("Me pego, quero meus direitos");
					}
				//Recebe Mensagem de PessoaComum
				}else if(title == "Eae cara, tudo bem?"){
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent("Eai mano, ta afim de ficar alegre?");
				
				//Recebe mensagem de Usuário
				}else if(title == "Cade o bagulho?"){
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("Ta na mão, agora passa a grana");
				}
				//Recebe mensagem de outro Traficante
				else if(title == "Eai mano, ta afim do bagulho?"){
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					reply.setContent("Sem vender drogas no meu territorio");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
	private class venderDrogas extends CyclicBehaviour {
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		public void action() {
	
				ACLMessage order = new ACLMessage(ACLMessage.CFP);
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < pessoas.length; ++i) {
					cfp.addReceiver(pessoas[i]);
				} 
				cfp.setContent("Eai mano, ta afim do bagulho?");
				cfp.setConversationId("VenderDrogas");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("VenderDrogas"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.FAILURE) {
						// A resposta partiu de um policial
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Vou pro xilindró");
						myAgent.send(order);
						takeDown();
					}else if(reply.getPerformative() == ACLMessage.AGREE){
						//Resposta partiu de um usuário
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Toma sua droga, agora cade o dinheiro?");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.REFUSE){
						//Resposta partiu de uma pessoa comum
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Um dia você ainda compra");
						myAgent.send(order);
					}else if(reply.getPerformative() == ACLMessage.REJECT_PROPOSAL){
						//Resposta partiu de outro traficante
						order.setPerformative(ACLMessage.AGREE);
						order.addReceiver(reply.getSender());
						order.setContent("Foi mal mano");
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
