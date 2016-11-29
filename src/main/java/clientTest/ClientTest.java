/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientTest;

import com.what.loanclient.Customer;
import java.util.Random;

/**
 *
 * @author mady
 */
public class ClientTest {
    public static String generateRandomNumber( String type ) {
        Random random = new Random();
        long randomNumber = 0, start = 0, end = 0;
        if ( type.equals( "ssn" ) ) {
            start = 1000000000L;
            end = 9999999999L;
        } else if ( type.equals( "loanAmount" ) ) {
            start = 100000;
            end = 2000000;
        } else if ( type.equals( "loanDuration" ) ) {
            start = 1;
            end = 30;
        }
        long range = end - start + 1;
        long fraction = ( long ) (range * random.nextDouble());
        randomNumber = fraction + start;
        return "" + randomNumber;
    }

    public static void main( String[] args ) throws Exception {
        for ( int i = 0; i < 2; i++ ) {
            Customer customer = new Customer();
            String id = customer.getId();
            System.out.println( i );
            customer.send(generateRandomNumber( "ssn" ) , Double.parseDouble(generateRandomNumber( "loanAmount" )), 
                                                          Integer.parseInt(generateRandomNumber( "loanDuration" )), id);
            customer.receive();
        }
        System.out.println( "END" );

    }

}
