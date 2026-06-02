package com.ami.service;

public interface EmailService {

	void sendResetPasswordEmail(String toEmail, String firstName, String resetLink);
}