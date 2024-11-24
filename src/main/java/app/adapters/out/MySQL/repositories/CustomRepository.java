package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomRepository extends JpaRepository<CustomerEntity, UUID> {
}
