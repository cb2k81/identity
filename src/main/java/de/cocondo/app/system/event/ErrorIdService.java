package de.cocondo.app.system.event;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ErrorIdService {

    private final ErrorEntityRepository errorEntityRepository;

    @Autowired
    public ErrorIdService(ErrorEntityRepository errorEntityRepository) {
        this.errorEntityRepository = errorEntityRepository;
    }

    public Long generateErrorId() {
        Long highestId = errorEntityRepository.findHighestId();
        if (highestId == null) {
            highestId = 0L;
        }
        return highestId + 1;
    }
}