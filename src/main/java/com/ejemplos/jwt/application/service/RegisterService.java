package com.ejemplos.jwt.application.service;

import com.ejemplos.jwt.application.ports.in.RegisterCommand;
import com.ejemplos.jwt.application.ports.in.RegisterUseCase;
import com.ejemplos.jwt.domain.exception.personalized.EmailAlreadyExistsException;
import com.ejemplos.jwt.domain.model.User;
import com.ejemplos.jwt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.createClient(
                command.firstName(),
                command.lastName(),
                command.email(),
                encodedPassword
        );

        return userRepository.save(user);
    }
}
