package com.nutriflow.repositories;

import com.nutriflow.entities.UserEntity;
import com.nutriflow.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Checks whether the email entered during registration already exists in the system.
     * Used to prevent duplicate registrations.
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their email address.
     * Used during login and when fetching profile information.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Returns the total number of users (patients) assigned to a specific dietitian.
     * Used for the "Total Patients" statistic on the Dietitian Dashboard.
     */
    long countByDietitianEmail(String email);

    /**
     * Returns the count of users belonging to a specific dietitian with a particular status (e.g. ACTIVE).
     * Used to display "Active Menu" or "Pending Menu" counts on the dashboard.
     */
    long countByDietitianEmailAndStatus(String email, UserStatus status);

    /**
     * Returns the full list of users assigned to a dietitian with a specific status.
     * Used when building "Urgent Patients" or "My Patients" lists.
     */
    List<UserEntity> findByDietitianEmailAndStatus(String email, UserStatus status);

    /**
     * Used in admin panel to view all users with a certain status
     */
    List<UserEntity> findAllByStatus(UserStatus status);

    /**
     * Finds users who have not yet been assigned to a specific dietitian (dietitian == null)
     * (Important for the assign process)
     */
    List<UserEntity> findAllByDietitianIsNull();

    @Query("SELECT u FROM UserEntity u WHERE u.dietitian.email = :dietitianEmail " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UserEntity> searchPatientsByDietitian(@Param("dietitianEmail") String dietitianEmail,
                                               @Param("query") String query);

    List<UserEntity> findAllByStatusAndDietitianIsNull(UserStatus status);

    /**
     * Finds ACTIVE users who have not yet been assigned to a specific caterer
     */
    List<UserEntity> findAllByStatusAndCatererIsNull(UserStatus status);

    long countByStatus(UserStatus status);
    long countByStatusAndDietitianIsNull(UserStatus status);
    long countByStatusAndCatererIsNull(UserStatus status);

    // Search by first name or last name
    Page<UserEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT(:query, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT(:query, '%'))")
    Page<UserEntity> searchUsers(@Param("query") String query, Pageable pageable);

    List<UserEntity> findAllByDietitianId(Long dietitianId);
    List<UserEntity> findAllByCatererId(Long catererId);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :start AND u.createdAt <= :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}