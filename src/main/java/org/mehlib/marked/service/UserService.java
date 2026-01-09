package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService<T extends User> {
    T createUser(T user);
    Optional<T> getUser(Long id);
    List<T> getAllUsers();
    T updateUser(T user);
    void deleteUser(T user);
    void deleteUserById(Long id);
}
