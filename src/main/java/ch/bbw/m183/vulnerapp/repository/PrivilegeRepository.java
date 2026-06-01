package ch.bbw.m183.vulnerapp.repository;

import ch.bbw.m183.vulnerapp.datamodel.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeEntity, Long> {

}

