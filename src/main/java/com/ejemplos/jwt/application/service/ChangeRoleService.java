package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.ChangeRoleUseCase;
import com.ejemplos.jwt.domain.enums.UserRole;
import com.ejemplos.jwt.domain.exception.personalized.UserNotFoundException;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeRoleService implements ChangeRoleUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void changeRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.toggleRole();

        userRepository.save(user);
        refreshTokenRepository.revokeAllTokens(user.getId());
    }
}
