package com.staytion.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(unique = true)
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "tel")
	private String tel;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "email_verified")
	private Boolean emailVerified = false;

	public User() {
	}

	public User(String firstName, String lastName, String email, String password, String tel, LocalDateTime createdAt, Boolean emailVerified) {
	        this.firstName = firstName;
	        this.lastName = lastName;
	        this.email = email;
	        this.password = password;
	        this.tel = tel;
	        this.createdAt = createdAt;
			this.emailVerified = emailVerified;
	    }


	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<Hotel> hotels;
}