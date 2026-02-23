package de.cocondo.app.system.entity.sequence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class NumberSequenceService {

    private final NumberSequenceRepository numberSequenceRepository;

    @Transactional
    public synchronized void init(String sequenceName, BigInteger startNumber, BigInteger endNumber) {
        NumberSequence numberSequence = numberSequenceRepository.findBySequenceName(sequenceName);
        if (numberSequence == null) {
            numberSequence = new NumberSequence(sequenceName, startNumber, endNumber);
            numberSequenceRepository.save(numberSequence);
        }
    }

    @Transactional
    public synchronized BigInteger next(String sequenceName) {
        NumberSequence numberSequence = numberSequenceRepository.findBySequenceName(sequenceName);
        if (numberSequence == null) {
            throw new IllegalStateException("Sequence not initialized.");
        }

        if (numberSequence.getCurrentNumber() == null) {
            numberSequence.setCurrentNumber(numberSequence.getStartNumber());
        } else {
            BigInteger nextNumber = numberSequence.getCurrentNumber().add(BigInteger.ONE);
            if (nextNumber.compareTo(numberSequence.getEndNumber()) > 0) {
                throw new RuntimeException("Number sequence exceeded its limit.");
            }
            numberSequence.setCurrentNumber(nextNumber);
        }

        numberSequenceRepository.save(numberSequence);
        return numberSequence.getCurrentNumber();
    }

    public BigInteger getHighest(String sequenceName) {
        NumberSequence numberSequence = numberSequenceRepository.findBySequenceName(sequenceName);
        if (numberSequence == null) {
            throw new IllegalArgumentException("Sequence not found: " + sequenceName);
        }
        return numberSequence.getEndNumber();
    }

    public BigInteger getLowest(String sequenceName) {
        NumberSequence numberSequence = numberSequenceRepository.findBySequenceName(sequenceName);
        if (numberSequence == null) {
            throw new IllegalArgumentException("Sequence not found: " + sequenceName);
        }
        return numberSequence.getStartNumber();
    }

    @Transactional
    public synchronized void reset(String sequenceName) {
        NumberSequence numberSequence = numberSequenceRepository.findBySequenceName(sequenceName);
        if (numberSequence == null) {
            throw new IllegalArgumentException("Sequence not found: " + sequenceName);
        }

        numberSequence.setCurrentNumber(numberSequence.getStartNumber());
        numberSequenceRepository.save(numberSequence);
    }
}
