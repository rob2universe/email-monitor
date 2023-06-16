package io.camunda.example.emailmonitor;

import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.search.FlagTerm;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailPollerJob implements Job {

    private static final String EMAIL_USERNAME = "rob2universe@gmail.com";
    private static final String EMAIL_PASSWORD = "cjsvtemfeamdociy";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("Checking for new emails...");

        try {
            // Connect to the email server
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", "imap.gmail.com");
            props.setProperty("mail.imaps.port", "993");
            props.setProperty("mail.imaps.connectiontimeout", "5000");
            props.setProperty("mail.imaps.timeout", "5000");
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(EMAIL_USERNAME, EMAIL_PASSWORD);

            // Open the inbox folder with ability to write so email can be marked as seen
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Retrieve the unseen emails
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            // Process each unseen email
            for (Message message : messages) {
                log.info("New email with subject: {} from {}", message.getSubject(), message.getFrom()[0].toString());

                // Log the email body
                try {
                    Object content = message.getContent();
                    if (content instanceof String) {
                        log.info("Body: " + content);
                    } else if (content instanceof Multipart multiPart) {
                        for (int i = 0; i < multiPart.getCount(); i++) {
                            BodyPart bodyPart = multiPart.getBodyPart(i);
                            if (bodyPart.isMimeType("text/plain")) {
                                log.info("Body: {}", bodyPart.getContent());
                            }
                        }
                    }
                } catch (IOException | MessagingException e) {
                    e.printStackTrace();
                }
                //mark email as seen so it will not be picked up twice
                inbox.setFlags(new Message[] {message}, new Flags(Flags.Flag.SEEN), true);
            }
                        // Close the connections
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            throw new JobExecutionException("Error occurred while polling the email inbox.", e);
        }
    }
}
