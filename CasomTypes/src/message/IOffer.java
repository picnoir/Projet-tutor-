/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package message;

import jade.core.AID;

/**
 *
 * @author josuah
 */
public interface IOffer  extends IMessage
{
    public float price();
    public String companyName();
    public void setAgency(AID agency);
    public AID getAgency();
}