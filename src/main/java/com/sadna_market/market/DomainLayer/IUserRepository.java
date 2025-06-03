package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username);
    boolean contains(String username);
    void save(User user);
    void update(User user);
    void delete(String username);

    List<User> findAll();
    List<User> findByEmail(String email);
    List<User> findActiveUsers();

    boolean existsByIsAdmin(boolean isAdmin);
    List<User> findByIsAdmin(boolean isAdmin);
    long countByIsAdmin(boolean isAdmin);

    public int countAll();
    public int countActiveUsers();


    void clear();
}
