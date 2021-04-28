package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
	
	UserRepository userRepository;
	BCryptPasswordEncoder pwdEncoder;
	Environment env;
	RestTemplate restTemplate;
	
	OrderServiceClient orderServiceClient;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder pwdEncoder, Environment env, RestTemplate restTemplate, OrderServiceClient orderServiceClient) {
		this.userRepository = userRepository;
		this.pwdEncoder = pwdEncoder; 
		this.env = env;
		this.restTemplate = restTemplate;
		this.orderServiceClient = orderServiceClient;
	}
	
	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setEncryptedPwd(pwdEncoder.encode(userDto.getPwd()));
		userRepository.save(userEntity);
		
		UserDto returnUserDto = mapper.map(userEntity, UserDto.class);
		
		return returnUserDto;
	}
	
	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException("User not found");
		}
		
		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		
		List<ResponseOrder> orders = new ArrayList<ResponseOrder>();
		// rest template order 정보 요청 start
//		List<ResponseOrder> orders = null;
//		String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//		
//		ResponseEntity<List<ResponseOrder>> orderRes = restTemplate.exchange(orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
//		});
//		orders = orderRes.getBody();
		// rest template order 정보 요청 end
		
		/*feign client*/ //예외처리는 FeignErrorDecoder에서 처리
		orders = orderServiceClient.getOrders(userId);
		
		userDto.setOrders(orders);
		
		return userDto;
	}
	
	@Override
	public Iterable<UserEntity> getUserByAll() {
		return userRepository.findAll();
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if(userEntity == null) {
			throw new UsernameNotFoundException(email);
		}
		return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(), true, true, true, true, new ArrayList<>());
		
	}
	
	@Override
	public UserDto getUserDetailsByEmail(String userEmail) {
		UserEntity userEntity = userRepository.findByEmail(userEmail);
		
		if(userEntity == null) {
			throw new UsernameNotFoundException(userEmail);
		}
		
		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		return userDto;
	}
	
	
}
