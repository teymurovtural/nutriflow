package com.nutriflow.repositories;

import com.nutriflow.entities.MenuBatchEntity;
import com.nutriflow.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuBatchRepository extends JpaRepository<MenuBatchEntity, Long> {

    List<MenuBatchEntity> findByMenuId(Long menuId);
    Optional<MenuBatchEntity> findFirstByMenu_User_EmailAndStatus(String email, MenuStatus status);
    long countByStatus(MenuStatus status);

}