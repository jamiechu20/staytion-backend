package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String password;

	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$", message = "First name must contain only English letters")
	private String firstName;

	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only English letters")
	private String lastName;
	
	@Pattern(regexp = "^\\d{10}$", message = "Telephone number must be 10 digits")
	private String tel;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

}
