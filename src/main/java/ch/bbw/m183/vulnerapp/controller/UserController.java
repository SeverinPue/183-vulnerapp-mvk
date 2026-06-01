package ch.bbw.m183.vulnerapp.controller;

import java.util.Base64;

import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import ch.bbw.m183.vulnerapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/whoami")
	public UserEntity whoami(@RequestHeader("Authorization") String basicAuth) {
		var usernamePassword = new String(Base64.getDecoder().decode(basicAuth.substring("Basic ".length())));
		var arr = usernamePassword.split(":", 2);
		return userService.whoami(arr[0], arr[1]);
	}

	@PostMapping("/fakelogin")
	public UserEntity fakelogin(@RequestParam String username, @RequestParam String password) {
		return userService.whoami(username, password);
	}

	@GetMapping("/fakelogout")
	public void fakelogout() {
		// does absolutely nothing
	}
}
