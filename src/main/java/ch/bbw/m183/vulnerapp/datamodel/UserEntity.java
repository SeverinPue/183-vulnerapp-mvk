package ch.bbw.m183.vulnerapp.datamodel;

import jakarta.persistence.*;
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

	@Id
	String username;

	@Column
	String email;

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
