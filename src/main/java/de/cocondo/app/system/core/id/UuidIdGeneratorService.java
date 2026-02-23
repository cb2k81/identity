package de.cocondo.app.system.core.id;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Default technical ID generator.
 * Fully domain-independent.
 */
@Service
public class UuidIdGeneratorService implements IdGeneratorService {

    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
