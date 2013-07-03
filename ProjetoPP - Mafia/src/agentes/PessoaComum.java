package agentes;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PessoaComum extends Agent {

	public void setup() {
		System.out.println("A Pessoa Comum: " + getAID().getName()
				+ " entrou no jogo");
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
	}

	public void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("A Pessoa Comum: " + getAID().getName()
				+ "saiu do jogo");
	}
}
