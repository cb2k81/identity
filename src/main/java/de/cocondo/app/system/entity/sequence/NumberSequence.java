package de.cocondo.app.system.entity.sequence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigInteger;

@Data
@Entity
public class NumberSequence {
    @Id
    private String sequenceName;
    private BigInteger startNumber;
    private BigInteger endNumber;
    private BigInteger currentNumber;

    public NumberSequence(String sequenceName, BigInteger startNumber, BigInteger endNumber) {
        this.sequenceName = sequenceName;
        this.startNumber = startNumber;
        this.endNumber = endNumber;
        this.currentNumber = null;
    }

    public NumberSequence() {
    }
}
