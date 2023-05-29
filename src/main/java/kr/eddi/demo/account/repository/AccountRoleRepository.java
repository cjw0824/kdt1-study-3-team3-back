package kr.eddi.demo.account.repository;

import kr.eddi.demo.account.entity.Account;
import kr.eddi.demo.account.entity.AccountRole;
import kr.eddi.demo.account.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {
    Optional<AccountRole> findByBusinessNumber(Long businessNumber);

    @Query("select ar.role from AccountRole ar join fetch Role r where ar.account = :account")
    Role findRoleByAccount(Account account);
    @Query("SELECT ar FROM AccountRole ar JOIN FETCH ar.role WHERE ar.account.id = :accountId")
    Optional<AccountRole> findByAccount_IdWithRole(@Param("accountId") Long accountId);

}

