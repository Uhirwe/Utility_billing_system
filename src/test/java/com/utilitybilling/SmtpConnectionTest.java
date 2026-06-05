package com.utilitybilling;

import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Run: mvn test-compile exec:java -Dexec.mainClass=com.utilitybilling.SmtpConnectionTest */
public class SmtpConnectionTest {

    public static void main(String[] args) throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername("uhirweestherhope@gmail.com");
        sender.setPassword("syqvnasxvvfohukh");

        var props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        System.out.println("Testing Gmail SMTP for uhirweestherhope@gmail.com ...");
        sender.testConnection();
        System.out.println("SUCCESS: Gmail authentication works!");

        var msg = sender.createMimeMessage();
        var helper = new org.springframework.mail.javamail.MimeMessageHelper(msg, false);
        helper.setFrom("uhirweestherhope@gmail.com");
        helper.setTo("uhirweestherhope@gmail.com");
        helper.setSubject("Utility Billing SMTP Test");
        helper.setText("If you receive this, SMTP is working.");
        sender.send(msg);
        System.out.println("SUCCESS: Test email sent!");
    }
}
