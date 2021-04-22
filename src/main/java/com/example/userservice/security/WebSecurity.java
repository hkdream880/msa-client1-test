package com.example.userservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.userservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter{
	
	private UserService userService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private Environment env;
	
	//@Autowired @Configuration 어노테이션으로 인해 필요 없다.
	public WebSecurity(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, Environment env) {
		// TODO Auto-generated constructor stub
		this.userService = userService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.env = env;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
//		super.configure(http);
		http.csrf().disable();
//		http.authorizeRequests().antMatchers("/**").permitAll().and().addFilter(getAuthenticationFilter());
		http.authorizeRequests().antMatchers("/**").hasIpAddress("172.30.1.29").and().addFilter(getAuthenticationFilter());
		
		http.headers().frameOptions().disable();
	}

	private AuthenticationFilter getAuthenticationFilter() throws Exception{
		
		AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(), userService, env);
//		authenticationFilter.setAuthenticationManager(authenticationManager());
		return authenticationFilter;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
		
	}
};


