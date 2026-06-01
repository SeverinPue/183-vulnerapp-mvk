package ch.bbw.m183.vulnerapp.datamodel;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "users")
public class UserEntity {

	@Size(min = 2, max = 20)
	@Id
	String username;

	@Column
	String email;

	@Size(min = 2, max = 20)
	@Column
	String fullname;

	@Column(length = 200)
	String password;

	@Column
	boolean enabled;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "users_roles",
			joinColumns = @JoinColumn(name = "username"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Collection<RoleEntity> roles;

}
