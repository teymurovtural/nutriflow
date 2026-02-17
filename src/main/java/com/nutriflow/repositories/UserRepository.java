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
     * Qeydiyyat zamanı daxil edilən emailin sistemdə olub-olmadığını yoxlayır.
     * Təkrarlanan qeydiyyatın qarşısını almaq üçün istifadə olunur.
     */
    boolean existsByEmail(String email);

    /**
     * İstifadəçini email ünvanına görə tapır.
     * Giriş (Login) prosesində və profil məlumatlarını gətirərkən istifadə olunur.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Müəyyən bir dietoloqa təyin edilmiş ümumi istifadəçi (pasiyent) sayını qaytarır.
     * Dietoloq Dashboard-undakı "Cəmi Pasiyentlər" statistikası üçün istifadə olunur.
     */
    long countByDietitianEmail(String email);

    /**
     * Müəyyən bir dietoloqa aid olan və xüsusi statusda (məs: ACTIVE) olan istifadəçilərin sayını qaytarır.
     * Dashboard-da "Aktiv Menyu" və ya "Gözləyən Menyu" sayını göstərmək üçün istifadə olunur.
     */
    long countByDietitianEmailAndStatus(String email, UserStatus status);

    /**
     * Dietoloqa təyin edilmiş və xüsusi statusu olan istifadəçilərin tam siyahısını gətirir.
     * "Təcili Pasiyentlər" (Urgent Patients) və ya "Mənim Pasiyentlərim" siyahısını hazırlayarkən istifadə olunur.
     */
    List<UserEntity> findByDietitianEmailAndStatus(String email, UserStatus status);

    /**
     * Admin panelində bütün USER statusunda olanları görmək üçün
     */
    List<UserEntity> findAllByStatus(UserStatus status);

    /**
     * Müəyyən bir dietoloqa hələ təyin olunmamış (dietitian == null)
     * istifadəçiləri tapmaq üçün (Assign prosesi üçün vacibdir)
     */
    List<UserEntity> findAllByDietitianIsNull();

    @Query("SELECT u FROM UserEntity u WHERE u.dietitian.email = :dietitianEmail " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UserEntity> searchPatientsByDietitian(@Param("dietitianEmail") String dietitianEmail,
                                               @Param("query") String query);

    List<UserEntity> findAllByStatusAndDietitianIsNull(UserStatus status);

    /**
     * Müəyyən bir caterer-ə (mətbəxə) hələ təyin olunmamış ACTIVE istifadəçiləri tapır
     */
    List<UserEntity> findAllByStatusAndCatererIsNull(UserStatus status);

    long countByStatus(UserStatus status);
    long countByStatusAndDietitianIsNull(UserStatus status);
    long countByStatusAndCatererIsNull(UserStatus status);

    // Axtarış üçün (Ad və ya Soyada görə)
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