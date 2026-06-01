package ch.bbw.m183.vulnerapp.service;

import jakarta.persistence.EntityManager;

import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {

	private final EntityManager entityManager;

	@Override
	public UserEntity whoami(String username, String password) {
		// native queries are more performant!!1 :P
		var user = (UserEntity) entityManager.createNativeQuery("SELECT * from users where username='" + username + "'", UserEntity.class)
			.getSingleResult();
		if (password.equals(user.getPassword())) {
			return user;
		}
		throw new InvalidPasswordException("invalid password for user " + user.getUsername());
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@StandardException
	public static class InvalidPasswordException extends RuntimeException {

	}
}
