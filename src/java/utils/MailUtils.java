/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.*;

public class MailUtils {
    private static final Logger LOGGER = Logger.getLogger(MailUtils.class.getName());
    // Khai báo biến để giữ instance của ConfigManager
    private static ConfigManager configManagerInstance; 

    // Khai báo user và pass, nhưng không khởi tạo ngay lập tức ở đây
    static String user;
    static String pass;

    // Khối static initializer: Khối này sẽ chạy MỘT LẦN duy nhất khi class MailUtils được tải vào bộ nhớ
    static {
        // Lấy thể hiện Singleton của ConfigManager
        configManagerInstance = ConfigManager.getInstance();
        
        // Bây giờ, khi configManagerInstance đã được khởi tạo, chúng ta có thể sử dụng nó
        user = configManagerInstance.getProperty("mail.username");
        pass = configManagerInstance.getProperty("mail.password");
    }
    
    public static void send(String to, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(content);

            Transport.send(msg);
        } catch (MessagingException e) {
            LOGGER.log(Level.INFO, e.getMessage());
        }
    }
}