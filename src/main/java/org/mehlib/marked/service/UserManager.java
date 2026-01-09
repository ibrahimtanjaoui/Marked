package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public class UserManager<T extends User>  implements UserService<T>  {
    private final JpaRepository<T, Long> jpaRepository;

    public UserManager(JpaRepository<T, Long> jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public T createUser(T user) {
       return jpaRepository.save(user);
    }

    @Override
    public Optional<T> getUser(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<T> getAllUsers() {
        return jpaRepository.findAll();
    }


    @Override
    public T updateUser(T user) {
        return jpaRepository.save(user);
    }

    @Override
    public void deleteUser(T user) {
        jpaRepository.delete(user);
    }

    @Override
    public void deleteUserById(Long id) {
        jpaRepository.deleteById(id);
    }
}
