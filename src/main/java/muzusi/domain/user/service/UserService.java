package muzusi.domain.user.service;

import lombok.RequiredArgsConstructor;
import muzusi.domain.user.entity.User;
import muzusi.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> readById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> readByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

