package com.company.material.service;

import com.company.material.entity.AccountLock;
import com.company.material.entity.LoginLog;
import com.company.material.entity.User;
import com.company.material.repository.AccountLockRepository;
import com.company.material.repository.LoginLogRepository;
import com.company.material.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final AccountLockRepository accountLockRepository;
    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Transactional
    public void logLogin(Long userId, String username, String loginIp, boolean success, String failureReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginIp(loginIp);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setResult(success ? "成功" : "失败");
        loginLog.setFailureReason(failureReason);
        loginLogRepository.save(loginLog);

        if (!success && userId != null) {
            checkAndLockAccount(userId, username);
        } else if (success && userId != null) {
            unlockAccountIfNeeded(userId);
        }
    }

    private void checkAndLockAccount(Long userId, String username) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCK_DURATION_MINUTES);
        long failedCount = loginLogRepository.countFailedLoginsSince(userId, since);
        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            AccountLock lock = new AccountLock();
            lock.setUserId(userId);
            lock.setUsername(username);
            lock.setLockStartTime(LocalDateTime.now());
            lock.setLockEndTime(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            lock.setReason("连续" + MAX_FAILED_ATTEMPTS + "次登录失败");
            lock.setFailedCount((int) failedCount);
            lock.setUnlocked(false);
            accountLockRepository.save(lock);
            log.warn("账号 {} 因连续登录失败被锁定，锁定时间 {} 分钟", username, LOCK_DURATION_MINUTES);
        }
    }

    private void unlockAccountIfNeeded(Long userId) {
        accountLockRepository.findActiveLock(userId, LocalDateTime.now()).ifPresent(lock -> {
            lock.setUnlocked(true);
            lock.setUnlockTime(LocalDateTime.now());
            accountLockRepository.save(lock);
            log.info("账号 {} 登录成功，自动解锁", lock.getUsername());
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkAccountLock(String username) {
        Map<String, Object> result = new HashMap<>();
        result.put("locked", false);
        userRepository.findByUsername(username).ifPresent(user -> {
            Optional<AccountLock> activeLock = accountLockRepository.findActiveLock(user.getId(), LocalDateTime.now());
            if (activeLock.isPresent()) {
                AccountLock lock = activeLock.get();
                long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), lock.getLockEndTime()).toMinutes();
                if (remainingMinutes > 0) {
                    result.put("locked", true);
                    result.put("remainingMinutes", remainingMinutes);
                    result.put("reason", lock.getReason());
                    result.put("lockEndTime", lock.getLockEndTime());
                }
            }
        });
        return result;
    }

    @Transactional(readOnly = true)
    public Page<LoginLog> search(String username, String result, LocalDateTime startTime,
                                 LocalDateTime endTime, Pageable pageable) {
        return loginLogRepository.findByConditions(username, result, startTime, endTime, pageable);
    }



    @Transactional(readOnly = true)
    public List<AccountLock> getLockedAccounts() {
        return accountLockRepository.findAllActiveLocks(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<AccountLock> getAccountLockHistory(Long userId) {
        return accountLockRepository.findByUserIdOrderByLockStartTimeDesc(userId);
    }
}
