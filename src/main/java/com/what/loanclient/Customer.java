/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.what.loanclient;

import com.rabbitmq.client.AMQP.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.what.models.Data;
import com.what.models.LoanResponse;
import connector.RabbitMQConnector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

public class Customer {

    private RabbitMQConnector connector;
    private final String GETBANK_EXCHANGE_NAME = "customer_direct_exchange";
    private final String ROUTING_KEY = "customer";
    private final String EXCHANGE_NAME = "whatLoanBroker";
    Channel channel;
    private final String id;
//     send(ssn, loanAmount, loanDuration, id);

    public Customer() {
        connector = new RabbitMQConnector();
        try {
            channel = connector.getChannel();
        }
        catch (IOException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void send(String ssn, double loanAmount, int loanDuration, String id) throws JAXBException, IOException {

        BasicProperties.Builder builder = new BasicProperties.Builder();
        builder.correlationId(id);
        System.out.println(id);
        BasicProperties prop = builder.build();
        Data data = new Data(ssn, loanAmount, loanDuration);
        JAXBContext jc = JAXBContext.newInstance(Data.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        JAXBElement<Data> je2 = new JAXBElement(new QName("Data"), Data.class, data);
        StringWriter sw = new StringWriter();
        marshaller.marshal(je2, sw);
        String xmlString = sw.toString().trim();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput objout = new ObjectOutputStream(bos);
        objout.writeObject(xmlString);
        byte body[] = bos.toByteArray();

        channel.basicPublish(GETBANK_EXCHANGE_NAME, ROUTING_KEY, prop, body);
        System.out.println(" [x] Sent '" + xmlString + "'");

        connector.close(channel);

    }

    public void receive() throws IOException {
        channel = connector.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages.");

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                try {
                    System.out.println(" [x] Received ");
                           
                    if (properties.getCorrelationId().equals(id)) {
                        Map<String,Object> headers=properties.getHeaders();
                        String bankName=headers.get("bankName").toString();
                        String bodyString = removeBom(new String(body));
                        System.out.println("The best offer to you is: ");
                        LoanResponse res = unmarchal(bodyString);
                        String ssn=res.getSsn();
                        if(!ssn.contains("-")){
                        ssn=ssn.substring(0, 4)+"-"+ssn.substring(4);
                        }
                        System.out.println("SSN: "+ssn);
                        System.out.println("Interest Rate: "+res.getInterestRate());
                        System.out.println("Bank name: "+bankName);

                    }
                }
                catch (JAXBException ex) {
                    Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally {
                    System.out.println("Thank you for using our service.");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    System.exit(0);
                }
            }
        };
        channel.basicConsume(queueName, false, consumer);

    }
    //unmarshal from string to Object

    private LoanResponse unmarchal(String bodyString) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(LoanResponse.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StringReader reader = new StringReader(bodyString);
        return (LoanResponse) unmarshaller.unmarshal(reader);
    }

    //marshal from pbkect to xml string
    private String marchal(LoanResponse d) throws JAXBException {
        JAXBContext jc2 = JAXBContext.newInstance(LoanResponse.class);
        Marshaller marshaller = jc2.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        JAXBElement<LoanResponse> je2 = new JAXBElement(new QName("Data"), LoanResponse.class, d);
        StringWriter sw = new StringWriter();
        marshaller.marshal(je2, sw);

        return removeBom(sw.toString());
    }

    //remove unnecessary charactors before xml declaration 
    private String removeBom(String bodyString) {
        String res = bodyString.trim();
        int substringIndex = res.indexOf("<?xml");
        if (substringIndex < 0) {
            return res;
        }
        return res.substring(res.indexOf("<?xml"));
    }

}
