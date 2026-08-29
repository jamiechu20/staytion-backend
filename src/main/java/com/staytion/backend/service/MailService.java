package com.staytion.backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final JavaMailSender mailSender;

	private final TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
    private String fromEmail;

	public void sendSimpleEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);

		mailSender.send(message);
	}
	
	public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

            log.info("郵件已成功發送到: {}", to);
        } catch (Exception e) {
            log.error("發送郵件失敗", e);
            throw new RuntimeException("發送郵件失敗", e);
        }
    }

	// 用 Thymeleaf 模板渲染 HTML 信件內容並寄出，取代手動字串拼接
	public void sendTemplatedMail(String to, String subject, String templateName, Map<String, Object> variables) {
		Context context = new Context();
		context.setVariables(variables);
		String htmlContent = templateEngine.process("mail/" + templateName, context);
		sendHtmlMail(to, subject, htmlContent);
	}
}
