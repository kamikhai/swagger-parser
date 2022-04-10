package com.example.swaggerparser.repository;

import com.example.swaggerparser.entity.TypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TypeMappingRepository extends JpaRepository<TypeMapping, UUID> {

    Optional<TypeMapping> findBySwaggerType(String swaggerType);
}
