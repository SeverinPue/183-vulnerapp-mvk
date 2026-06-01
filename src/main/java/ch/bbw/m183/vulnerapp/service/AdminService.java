package ch.bbw.m183.vulnerapp.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import ch.bbw.m183.vulnerapp.datamodel.PrivilegeEntity;
import ch.bbw.m183.vulnerapp.datamodel.RoleEntity;
import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import ch.bbw.m183.vulnerapp.repository.PrivilegeRepository;
import ch.bbw.m183.vulnerapp.repository.RoleRepository;
import ch.bbw.m183.vulnerapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PrivilegeRepository privilegeRepository;

	private final PasswordEncoder passwordEncoder;

	public UserEntity createUser(UserEntity newUser) {
		String pw = newUser.getPassword();
		if (pw != null && pw.startsWith("{")) {
			if (pw.startsWith("{noop}")) {
				String raw = pw.substring(pw.indexOf('}') + 1);
				validatePasswordStrength(raw);
				newUser.setPassword(passwordEncoder.encode(raw));
			}
			return userRepository.save(newUser);
		}
		validatePasswordStrength(pw);
		newUser.setPassword(passwordEncoder.encode(pw));
		return userRepository.save(newUser);
	}


	public Page<UserEntity> getUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public void deleteUser(String username) {
		userRepository.deleteById(username);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void loadTestUsers() {
		initializeRoles();
		var adminRole = roleRepository.findByName("ROLE_ADMIN");
		var editorRole = roleRepository.findByName("ROLE_EDITOR");
		Stream.of(
				new UserEntity().setUsername("admin").setEmail("admin@example.com").setFullname("Super Admin").setPassword("Super5ecret1").setEnabled(true).setRoles(Collections.singletonList(adminRole)),
				new UserEntity().setUsername("fuu").setEmail("fuu@example.com").setFullname("Johanna Doe").setPassword("Bar12345").setEnabled(true).setRoles(Collections.singletonList(editorRole))
		).forEach(this::createUser);
	}

	private void validatePasswordStrength(String password) {
		if (password == null) {
			throw new IllegalArgumentException("Password must not be null");
		}
		if (password.length() < 8) {
			throw new IllegalArgumentException("Password must be at least 8 characters long");
		}
		boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);
		if (! (hasLower && hasDigit) ) {
			throw new IllegalArgumentException("Password must contain at least one lower-case letter and one digit");
		}
	}

	private void initializeRoles() {
		if (roleRepository.count() > 0) {
			return;
		}


		PrivilegeEntity readPrivilege = new PrivilegeEntity().setName("READ");
		PrivilegeEntity writePrivilege = new PrivilegeEntity().setName("WRITE");
		PrivilegeEntity deletePrivilege = new PrivilegeEntity().setName("DELETE");
		PrivilegeEntity adminPrivilege = new PrivilegeEntity().setName("ADMIN");

		privilegeRepository.saveAll(Arrays.asList(
				readPrivilege, writePrivilege, deletePrivilege, adminPrivilege
		));

		RoleEntity userRole = new RoleEntity()
				.setName("ROLE_USER")
				.setPrivileges(Arrays.asList(readPrivilege));

		RoleEntity editorRole = new RoleEntity()
				.setName("ROLE_EDITOR")
				.setPrivileges(Arrays.asList(readPrivilege, writePrivilege));

		RoleEntity adminRole = new RoleEntity()
				.setName("ROLE_ADMIN")
				.setPrivileges(Arrays.asList(readPrivilege, writePrivilege, deletePrivilege, adminPrivilege));

		roleRepository.saveAll(Arrays.asList(userRole, editorRole, adminRole));
	}
}