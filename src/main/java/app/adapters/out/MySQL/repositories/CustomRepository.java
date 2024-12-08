package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByName(String name);
}
