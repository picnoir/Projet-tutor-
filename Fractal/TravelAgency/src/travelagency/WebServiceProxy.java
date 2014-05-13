
package travelagency;
import java.util.Date;
import javax.xml.ws.Endpoint;
import message.IConfirmationLetter;
import message.IOffer;
import message.IOfferPack;
import message.IOfferRequest;
import message.Offer;
import message.OfferRequest;
import org.objectweb.fractal.fraclet.annotations.Component;
import org.objectweb.fractal.fraclet.annotations.Interface;
import org.objectweb.fractal.fraclet.annotations.Requires;
import travelagency.interfaces.IVacationOfferBrowser;
import travelagency.interfaces.IVacationReservationManager;

@Component(provides=@Interface(name="r", signature = java.lang.Runnable.class))
public class WebServiceProxy implements ITravelAgency, Runnable {
    public WebServiceProxy() {
        
    }
    
    @Override
    public void run() {
        Client client = new Client(_vacationOfferBrowser, _vacationReservationManager);
        Endpoint.publish("http://localhost:8080/VacationOffer",
                client.getProposals(new OfferRequest(10,10,"ets",
        new Date(), new Date(), "hola", 1000)));
        Endpoint.publish("http://localhost:8080/VacationReservation",
                client.reserveOffer(new Offer(100,"patrick")));
    }
    
    @Requires(name = "getProposals")
    private IVacationOfferBrowser _vacationOfferBrowser;
    
    @Requires(name = "vacationReservationManager")
    private IVacationReservationManager _vacationReservationManager;
    
    @Override
    public IOfferPack requestProposal(IOfferRequest offerRequest) {
        return _vacationOfferBrowser.getProposals(offerRequest);
    }
    
    @Override
    public IConfirmationLetter reserveOffer(IOffer offer) {
        return _vacationReservationManager.reserveOffer(offer);
    }
}
