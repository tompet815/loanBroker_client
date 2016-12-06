/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package starter;

import com.what.bankws.NewWebService;
import com.what.bankws.WhatLoanBrokerService;
import com.what.loanclient.Customer;
import java.util.Scanner;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author Tomoe
 */
public class Starter {
    @WebServiceRef(wsdlLocation
            = "http://localhost:8080/BrokerWS/WhatLoanBrokerService?WSDL")
    private static WhatLoanBrokerService service = new WhatLoanBrokerService();
       public static void main(String[] argv) throws Exception {
       Customer customer = new Customer();
       String id= customer.getId1();
       Scanner scan = new Scanner(System.in);
           System.out.println("Please insert your Social Security Number");
       String ssn=scan.next();
           System.out.println("Please insert the amount of loan");
       double loanAmount=scan.nextDouble();
           System.out.println("How many years do you want to loan? Insert years in number");
       int loanDuration=scan.nextInt();
           System.out.println("Wait a moment. We are searching the best offer for you...");
       NewWebService port = service.getNewWebServicePort();
           System.out.println( port.getBestOffer( ssn, loanAmount, loanDuration ));
//       customer.send(ssn, loanAmount, loanDuration, id);
//      customer.receive();
      }
}
