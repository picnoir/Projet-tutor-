/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package behaviour;

import agent.CasomClient;
import agent.TravelAgency;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.IOfferRequest;

/**
 *
 * @author josuah
 */
public class RequestOfferBehaviour extends jade.core.behaviours.OneShotBehaviour
{
    private CasomClient _myAgent;
    private IOfferRequest _offerRequest;
    
    public RequestOfferBehaviour(CasomClient myAgent, IOfferRequest offerRequest)
    {
        _myAgent = myAgent;
        _myAgent.setBooked(false);
        _myAgent.setSearchRuinning(true);
    }
    
    /**
     * Lookup travel agencies agents from DF and send them the OfferRequest message received from the View Agent.
     */
    @Override
    public void action()
    {
        AID agency;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        
        // Template for searching TravelAgency agents from DF
        sd.setType(TravelAgency.ServiceDescription);
        template.addServices(sd);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        
        try
        {
            msg.setContentObject(_offerRequest);
            DFAgentDescription[] result = DFService.search(myAgent, template); // !!! this myAgent is different (Jade stuffs)
            
            for(int i = 0; i < result.length; ++i)
            {
                agency = result[i].getName();
                msg.addReceiver(agency);
            }
        }
        catch (FIPAException fe)
        {
            System.err.println("FinalizeBehaviour::action : DF lookup error. "+fe.getLocalizedMessage());
            Logger.getLogger(FinalizeBehaviour.class.getName()).log(Level.SEVERE, null, fe);
        }
        catch (IOException ex)
        {
            System.err.println("RequestOfferBehaviour::action : message sending error. "+ex.getLocalizedMessage());
            Logger.getLogger(RequestOfferBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        _myAgent.send(msg);
    }
}
