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
@Table(name = "privileges")
public class PrivilegeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@ManyToMany(mappedBy = "privileges")
	private Collection<RoleEntity> roles;
}
