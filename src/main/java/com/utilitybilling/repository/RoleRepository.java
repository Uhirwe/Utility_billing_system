package com.utilitybilling.repository;

import com.utilitybilling.entity.Role;
import com.utilitybilling.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
