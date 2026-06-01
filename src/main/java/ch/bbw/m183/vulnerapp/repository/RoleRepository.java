package ch.bbw.m183.vulnerapp.repository;

import ch.bbw.m183.vulnerapp.datamodel.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

	RoleEntity findByName(String name);
}

