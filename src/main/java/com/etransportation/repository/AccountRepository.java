package com.etransportation.repository;

import com.etransportation.enums.AccountStatus;
import com.etransportation.enums.RoleAccount;
import com.etransportation.model.Account;
import java.util.Date;
import java.util.List;
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

    Optional<Account> findDistinctByIdAndRoles_NameIn(Long id, RoleAccount[] role);

    Page<Account> findDistinctByRoles_NameInAndStatusNotIn(RoleAccount[] roles, Pageable pageable, AccountStatus[] status);

    Page<Account> findDistinctByRoles_NameInAndStatusIn(RoleAccount[] roles, Pageable pageable, AccountStatus[] status);

    Optional<Account> findByIdAndStatusIn(Long id, AccountStatus[] status);

    Page<Account> findDistinctByRoles_NameInAndStatusIn(RoleAccount[] role, AccountStatus[] status, Pageable pageable);

    List<Account> findDistinctByTimeKeepings_DateAndRoles_NameIn(Date date, RoleAccount[] role);

    List<Account> findDistinctTop3ByIdNotInAndRoles_NameIn(Long[] accountIds, RoleAccount[] role);
}
