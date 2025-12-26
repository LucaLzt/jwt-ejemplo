package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.LoginCommand;
import com.ejemplos.jwt.application.ports.in.LoginResult;
import com.ejemplos.jwt.application.ports.in.LoginUseCase;
import com.ejemplos.jwt.application.ports.out.GeneratedToken;
import com.ejemplos.jwt.application.ports.out.JwtTokenProviderPort;
import com.ejemplos.jwt.application.ports.out.PasswordEncoderPort;
import com.ejemplos.jwt.domain.model.RefreshToken;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.RefreshTokenRepository;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean matches = passwordEncoderPort.matches(
                command.password(),
                user.getPassword()
        );

        if (!matches) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtTokenProviderPort.generateAccessToken(user);
        GeneratedToken refreshTokenData = jwtTokenProviderPort.generateRefreshToken(user);

        LoginResult result = new LoginResult(accessToken, refreshTokenData.token());

        RefreshToken refreshToken = RefreshToken.create(
                user.getId(),
                refreshTokenData.token(),
                refreshTokenData.expiresAt()
        );

        refreshTokenRepository.save(refreshToken);

        return result;

    }
}
