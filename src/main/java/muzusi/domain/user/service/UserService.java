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

    public Optional<User> readById(Long userId) {
        return userRepository.findById(userId);
    }
}

