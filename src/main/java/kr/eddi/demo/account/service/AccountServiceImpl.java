package kr.eddi.demo.account.service;

import kr.eddi.demo.account.controller.form.AccountLoginRequestForm;
import kr.eddi.demo.account.entity.Account;
import kr.eddi.demo.account.entity.AccountRole;
import kr.eddi.demo.account.entity.Role;
import kr.eddi.demo.account.entity.RoleType;
import kr.eddi.demo.account.repository.*;
import kr.eddi.demo.account.service.request.BusinessAccountRegisterRequest;
import kr.eddi.demo.account.service.request.NormalAccountRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

    final private AccountRepository accountRepository;
    final private AccountRoleRepository accountRoleRepository;
    final private RoleRepository roleRepository;
    final private UserTokenRepository userTokenRepository = UserTokenRepositoryImpl.getInstance();

    // 일반 회원(구매자)의 회원가입
    @Override
    public Boolean normalAccountRegister(NormalAccountRegisterRequest request) {
        final Optional<Account> maybeAccount = accountRepository.findByEmail(request.getEmail());
        // 중복 이메일 확인
        if (maybeAccount.isPresent()) {
            return false;
        }

        // 계정 생성
        final Account account = accountRepository.save(request.toAccount());

        // 회원 타입 부여
        final Role role = roleRepository.findByRoleType(request.getRoleType()).get();
        final AccountRole accountRole = new AccountRole(role, account);
        accountRoleRepository.save(accountRole);

        return true;
    }

    // 사업자 회원(판매자)의 회원가입
    @Override
    public Boolean businessAccountRegister(BusinessAccountRegisterRequest request) {
        final Optional<Account> maybeAccount =
                accountRepository.findByEmail(request.getEmail());
        // 중복 이메일 확인
        if (maybeAccount.isPresent()) {
            return false;
        }

        final Long businessNumber = request.getBusinessNumber();

        // 중복 사업자 번호 확인
        final Optional<AccountRole> maybeAccountRole =
                accountRoleRepository.findByBusinessNumber(businessNumber);

        if(maybeAccountRole.isPresent()) {
            return false;
        }

        // 계정 생성
        final Account account = accountRepository.save(request.toAccount());

        // 회원 타입 부여
        final Role role = roleRepository.findByRoleType(request.getRoleType()).get();

        final AccountRole accountRole = new AccountRole(role, account, businessNumber);
        accountRoleRepository.save(accountRole);

        return true;
    }

    // 회원 로그인
    @Override
    public String login(AccountLoginRequestForm requestForm) {
        Optional<Account> maybeAccount = accountRepository.findByEmail(requestForm.getEmail());
        // 이메일 확인 후 비밀번호 검사
        if(maybeAccount.isPresent()) {
            if(requestForm.getPassword().equals(maybeAccount.get().getPassword())) {
                // 맞으면 해당 계정 가져와서 토큰 부여 후 반환
                final Account account = maybeAccount.get();
                final String userToken = UUID.randomUUID().toString();
                userTokenRepository.save(userToken, account.getId());
                return userToken;
            }
        }

        return null;
    }

    @Override
    public RoleType lookup(String userToken) {
        final Long accountId = userTokenRepository.findAccountIdByUserToken(userToken);
        final Optional<Account> maybeAccount = accountRepository.findById(accountId);

        if (maybeAccount.isEmpty()) {
            return null;
        }

        final Account account = maybeAccount.get();
        final Role role = accountRoleRepository.findRoleByAccount(account);

        log.info("roleType: " + role.getRoleType());
        return role.getRoleType();
    }
}