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
@Table(name = "roles")
public class RoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@ManyToMany(mappedBy = "roles")
	private Collection<UserEntity> users;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "roles_privileges",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "privilege_id")
	)
	private Collection<PrivilegeEntity> privileges;
}
