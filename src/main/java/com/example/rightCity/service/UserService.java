package com.example.rightCity.service;

import com.example.rightCity.entity.UserEntity;
import com.example.rightCity.exception.user.CombinationMailPasswordException;
import com.example.rightCity.exception.user.OldNameMatchesNewOneException;
import com.example.rightCity.exception.user.UserNotFoundException;
import com.example.rightCity.exception.user.UserWithMailAlreadyExistException;
import com.example.rightCity.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity registration (UserEntity user) throws UserWithMailAlreadyExistException {
        userRepository
            .findById(user.getID())
            .ifPresent(u -> {
                throw new UserWithMailAlreadyExistException();
            });
        return userRepository.save(user);
    }


    public UserEntity updateUsernameById(String updatedUsername, Long id) throws OldNameMatchesNewOneException {
        final AtomicReference<UserEntity> saved = new AtomicReference<>();
        userRepository
                .findById(id)
                        .ifPresentOrElse(user -> {
                            checkMatches(updatedUsername, user);
                            user.setFIO(updatedUsername);
                            saved.set(userRepository.save(user));
                        },
                                UserNotFoundException::new
                        );
        return saved.get();
    }


    public UserEntity updatePasswordById(String password, Long id) {
        final AtomicReference<UserEntity> saved = new AtomicReference<>();
        userRepository
                .findById(id)
                .ifPresentOrElse(user -> {
                    user.setPassword(password);
                    saved.set(userRepository.save(user));
                },
                        UserNotFoundException::new
                );

        return saved.get();
    }

    public void deleteUserById(Long id){
        userRepository
            .findById(id)
                .ifPresentOrElse(
                        user -> userRepository.deleteById(user.getID()),
                        UserNotFoundException::new
                );
    }

    public UserEntity loginByMailPassword(UserEntity user)
            throws UserNotFoundException, CombinationMailPasswordException {
        AtomicReference<UserEntity> ref = new AtomicReference<>();
        userRepository
            .findById(user.getID())
            .ifPresentOrElse(
                u -> {
                    checkPassword(u);
                    ref.set(u);
                },
                UserNotFoundException::new
            );
        return ref.get();
    }


    public UserEntity getUserByMail(String mail) throws UserNotFoundException {
        AtomicReference<UserEntity> user = new AtomicReference<>();
        userRepository
            .findByMail(mail)
                .ifPresentOrElse(
                    user::set,
                    UserNotFoundException::new
                );
        return user.get();
    }


    private void checkMatches(String updatedUsername, UserEntity user) throws OldNameMatchesNewOneException {
        if(Objects.equals(user.getFIO(), updatedUsername)) {
            throw new OldNameMatchesNewOneException();
        }
    }


    private void checkPassword(UserEntity user) throws CombinationMailPasswordException {
        if(!Objects.equals(userRepository.findByMail(user.getMail()).orElseThrow().getPassword(), user.getPassword())) {
            throw new CombinationMailPasswordException();
        }
    }
}
