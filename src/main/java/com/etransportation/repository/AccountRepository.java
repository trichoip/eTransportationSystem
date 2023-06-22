package com.etransportation.repository;

import com.etransportation.enums.RoleAccount;
import com.etransportation.model.Account;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Boolean existsByUsername(String username);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByIdAndRoles_Name(Long id, RoleAccount role);

    Page<Account> findByUsernameContains(String username, Pageable pageable);

    Boolean existsByIdAndLikeCars_Id(Long accountId, Long carId);

    Page<Account> findDistinctByRoles_NameIn(RoleAccount[] role, Pageable pageable);
}
