package ch.bbw.m183.vulnerapp;

import ch.bbw.m183.vulnerapp.datamodel.PrivilegeEntity;
import ch.bbw.m183.vulnerapp.datamodel.RoleEntity;
import ch.bbw.m183.vulnerapp.datamodel.UserEntity;
import ch.bbw.m183.vulnerapp.repository.PrivilegeRepository;
import ch.bbw.m183.vulnerapp.repository.RoleRepository;
import ch.bbw.m183.vulnerapp.repository.UserRepository;
import ch.bbw.m183.vulnerapp.service.AdminService;
import ch.bbw.m183.vulnerapp.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SecurityTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private AdminService adminService;

	@MockitoBean
	private BlogService blogService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		when(adminService.getUsers(any())).thenReturn(Page.empty());
		when(adminService.createUser(any())).thenAnswer(inv -> inv.getArgument(0));

		initDatabase();
	}

	private void initDatabase() {
		if (roleRepository.count() > 0) return;

		PrivilegeEntity read = new PrivilegeEntity().setName("READ");
		PrivilegeEntity write = new PrivilegeEntity().setName("WRITE");
		PrivilegeEntity del = new PrivilegeEntity().setName("DELETE");
		PrivilegeEntity adm = new PrivilegeEntity().setName("ADMIN");
		privilegeRepository.saveAll(Arrays.asList(read, write, del, adm));

		RoleEntity userRole = new RoleEntity().setName("ROLE_USER").setPrivileges(Arrays.asList(read));
		RoleEntity editorRole = new RoleEntity().setName("ROLE_EDITOR").setPrivileges(Arrays.asList(read, write));
		RoleEntity adminRole = new RoleEntity().setName("ROLE_ADMIN").setPrivileges(Arrays.asList(read, write, del, adm));
		roleRepository.saveAll(Arrays.asList(userRole, editorRole, adminRole));

		userRepository.saveAll(Arrays.asList(
				new UserEntity().setUsername("admin").setEmail("admin@example.com").setFullname("Super Admin")
						.setPassword(passwordEncoder.encode("Super5ecret1")).setEnabled(true)
						.setRoles(Collections.singletonList(adminRole)),
				new UserEntity().setUsername("fuu").setEmail("fuu@example.com").setFullname("Johanna Doe")
						.setPassword(passwordEncoder.encode("Bar12345")).setEnabled(true)
						.setRoles(Collections.singletonList(editorRole))
		));
	}

	@Test
	void blogsLesenOhneLogin_erlaubt() throws Exception {
		mockMvc.perform(get("/api/blog")).andExpect(status().isOk());
	}

	@Test
	void startseiteOhneLogin_erlaubt() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk());
	}


	@Test
	void whoamiOhneLogin_gibt401() throws Exception {
		mockMvc.perform(get("/api/user/whoami")).andExpect(status().isUnauthorized());
	}

	@Test
	void blogErstellenOhneLogin_blocked() throws Exception {
		mockMvc.perform(post("/api/blog").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Test Title\",\"body\":\"Some body content here\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void blogErstellenAlsEditor_erlaubt() throws Exception {
		mockMvc.perform(post("/api/blog").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Test Title\",\"body\":\"Some body content here\"}"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "viewer", roles = "USER")
	void blogErstellenAlsUser_verboten() throws Exception {
		mockMvc.perform(post("/api/blog").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Test Title\",\"body\":\"Some body content here\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void blogLoeschenAlsEditor_verboten() throws Exception {
		mockMvc.perform(delete("/api/blog").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void postOhneCsrf_wirdAbgelehnt() throws Exception {
		mockMvc.perform(post("/api/blog")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Test Title\",\"body\":\"Some body content here\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void postMitUngueltigemCsrf_wirdAbgelehnt() throws Exception {
		mockMvc.perform(post("/api/blog").with(csrf().useInvalidToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Test Title\",\"body\":\"Some body content here\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void adminEndpunkt_alsAdmin_erlaubt() throws Exception {
		mockMvc.perform(get("/api/admin123/users")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void adminEndpunkt_alsEditor_nichtGeschuetzt() throws Exception {
		mockMvc.perform(get("/api/admin123/users")).andExpect(status().isOk());
	}

	@Test
	void loginMitKorrektemPasswort_ok() throws Exception {
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.content("username=admin&password=Super5ecret1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"));
	}

	@Test
	void loginMitFalschemPasswort_forbidden() throws Exception {
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.content("username=admin&password=falsch"))
				.andExpect(status().isForbidden());
	}

	@Test
	void passwoerterSindGehasht() {
		UserEntity admin = userRepository.findByUsername("admin");
		assertThat(admin.getPassword()).startsWith("{bcrypt}");
		assertThat(admin.getPassword()).isNotEqualTo("Super5ecret1");
		assertThat(passwordEncoder.matches("Super5ecret1", admin.getPassword())).isTrue();
		assertThat(passwordEncoder.matches("Falsch123", admin.getPassword())).isFalse();
	}

	@Test
	void schwachesPasswortWirdAbgelehnt() {
		AdminService realService = new AdminService(userRepository, roleRepository, privilegeRepository, passwordEncoder);

		assertThatThrownBy(() -> realService.createUser(
				new UserEntity().setUsername("t1").setEmail("t@t.com").setFullname("Test").setPassword("Short1")))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> realService.createUser(
				new UserEntity().setUsername("t2").setEmail("t@t.com").setFullname("Test").setPassword("NoDigitsHere")))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> realService.createUser(
				new UserEntity().setUsername("t3").setEmail("t@t.com").setFullname("Test").setPassword("ALLUPPER123")))
				.isInstanceOf(IllegalArgumentException.class);
	}


	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void blogMitZuKurzemTitel_wirdNichtVomControllerValidiert() throws Exception {

		mockMvc.perform(post("/api/blog").with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"X\",\"body\":\"Valid body content here\"}"))
				.andExpect(status().isOk());
	}


	@Test
	void actuatorHealthOhneLogin_ohneDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@Test
	@WithMockUser(username = "fuu", roles = "EDITOR")
	void actuatorHealthMitLogin_erlaubt() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}
}
