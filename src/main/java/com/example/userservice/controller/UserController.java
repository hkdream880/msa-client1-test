package com.example.userservice.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;

@RestController
@RequestMapping("/")
public class UserController {
	
	private Environment env;
	private UserService userService;
	
	@Autowired
	private Greeting greeting;
	
	@Autowired
	public UserController(Environment env, UserService userService) {
		// TODO Auto-generated constructor stub
		this.env = env;
		this.userService = userService;
	}
	
	@GetMapping("/health-check")
	public String status() {
		return "it's working in User Service";
	}
	
	@GetMapping("/welcome")
	public String welcome() {
//		return env.getProperty("greeting.message");
		return greeting.getMessage();
	}
	
	@PostMapping("/users")
	public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDto = mapper.map(requestUser, UserDto.class);
		userService.createUser(userDto);
		
		ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
	}
	
	
}

