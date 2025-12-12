package com.verify.service.Utils;

import java.io.File;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
   @Autowired
   private JavaMailSender sender;
   @Value("${spring.mail.username}")
   private String from;

   public void sendTextEmail(String to, String subject, String content) {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(this.from);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);
      message.setSentDate(new Date());
      this.sender.send(message);
   }

   public void sendImageMail(String to, String subject, String content, String imgPath, String imgId) throws MessagingException {
      MimeMessage message = this.sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(this.from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, true);
      FileSystemResource file = new FileSystemResource(new File(imgPath));
      helper.addInline(imgId, file);
      this.sender.send(message);
   }
}
