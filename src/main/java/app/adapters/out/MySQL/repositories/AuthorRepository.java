package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
    Optional<AuthorEntity> findByName(String name);
    Page<AuthorEntity> findByNameContainingIgnoreCase(String query, Pageable pageable);
}
