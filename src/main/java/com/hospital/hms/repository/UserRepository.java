package com.hospital.hms.repository;

import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    // ✅ Find by phone
    Optional<User> findByPhone(String phone);

    // ✅ Find all PENDING patients
    // (self-registered patients with no patient record yet)
    List<User> findByRoleAndStatus(Role role, UserStatus status);

    // ✅ Search by email OR phone
    @Query("SELECT u FROM User u WHERE " +
            "u.email = :query OR u.phone = :query")
    Optional<User> findByEmailOrPhone(
            @Param("query") String query);

    // ✅ Count pending patients for sidebar badge
    long countByRoleAndStatus(Role role, UserStatus status);
}
