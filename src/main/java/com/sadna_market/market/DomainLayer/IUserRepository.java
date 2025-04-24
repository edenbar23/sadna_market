package com.sadna_market.market.DomainLayer;

import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username);
    boolean contains(String username);

    void save(User user);
    void update(User user);

    void delete(String username);



    // Add other methods as needed

}
