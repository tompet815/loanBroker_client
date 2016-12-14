package clientTest;

import static clientTest.ClientTest.generateRandomNumber;
import java.util.Random;
import starter.Starter;

class ClientTest {

    public static String generateRandomNumber(String type) {
        Random random = new Random();
        long randomNumber = 0, start = 0, end = 0;
        if (type.equals("ssn")) {
            start = 1000000000L;
            end = 9999999999L;
        }
        else if (type.equals("loanAmount")) {
            start = 100000;
            end = 2000000;
        }
        else if (type.equals("loanDuration")) {
            start = 1;
            end = 30;
        }
        long range = end - start + 1;
        long fraction = (long) (range * random.nextDouble());
        randomNumber = fraction + start;
        return "" + randomNumber;
    }

    public static void main(String[] args) throws Exception {
      for(int i=0; i<3; i++){
        new Thread(new TesterRunnable()).start();
      Thread.sleep(1000);}
    }
    
    }
  class TesterRunnable implements Runnable {

            @Override
            public void run() {
                String ssn= generateRandomNumber("ssn").substring(0,6)+"-"+generateRandomNumber("ssn").substring(6);
                System.out.println(ssn);
                Starter.send(ssn, Double.parseDouble(generateRandomNumber("loanAmount")),
                        Integer.parseInt(generateRandomNumber("loanDuration")));
            }

        }
      
