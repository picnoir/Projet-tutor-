/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import behaviour.TravelAgencyAutomatonBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.logging.Level;
import java.util.logging.Logger;
import travelagency.ITravelAgency;




/**
 *
 * @author josuah
 */
public class TravelAgency extends jade.core.Agent
{
    private boolean _remoteMode;
    
    public static final String ServiceDescription;
    
    static
    {
        ServiceDescription = "Casom-TravelAgency-Stub-Agent";
    }
    
    public TravelAgency(boolean remoteMode)
    {
        super();
        _remoteMode = remoteMode;
    }
    
    public TravelAgency()
    {
        this(false);
    }
    
    @Override
    public void setup()
    {
        // DF registration
        try {
            _register2DF();
        } catch (FIPAException ex) {
            System.err.println("TravelAgency::setup : DF registration error. "+ex);
            Logger.getLogger(TravelAgency.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ITravelAgency remoteAgencyStub = (_remoteMode) ? new service.WsTravelAgency() : new fake.RemoteAgency(this);
        this.addBehaviour(new TravelAgencyAutomatonBehaviour(this, remoteAgencyStub));
    }
    
    @Override
    protected void takeDown()
    {
        // Deregister from the yellow pages
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException fe)
        {
            System.err.println("TravelAgency::takeDown : DF de-registration error. "+fe);
            Logger.getLogger(TravelAgency.class.getName()).log(Level.SEVERE, null, fe);
        }
        // Printout a dismissal message
        System.out.println(TravelAgency.ServiceDescription+" "+getAID().getName()+" terminating.");
    }
    
    private void _register2DF() throws FIPAException
    {
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TravelAgency.ServiceDescription);
        sd.setName(this.getAID().getName());
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(this.getAID());
        
        dfd.addServices(sd);
        DFService.register(this, dfd);
    }
    
    public boolean isRemoteMode()
    {
        return _remoteMode;
    }
}
