package de.cocondo.app.system.entity.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cocondo.app.system.entity.DomainEntity;
import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * KeyValuePair entity used for dynamic metadata on DomainEntity.
 *
 * Design decisions:
 *
 * - Each KeyValuePair belongs to exactly one DomainEntity (mandatory relation).
 * - Unique constraint on (domain_entity_id, key_name) to guarantee
 *   that a key can only exist once per entity.
 * - Explicit column names to avoid SQL reserved keyword conflicts.
 * - 'value' column renamed to 'kv_value' to avoid conflicts with SQL keywords.
 * - equals/hashCode based solely on id (JPA-safe, proxy-safe).
 *
 * Important:
 * The former redundant field "entityId" has been removed.
 * The owning side of the relationship is exclusively the @ManyToOne
 * association to DomainEntity.
 */
@Entity
@Table(
        name = "key_value_pair",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"domain_entity_id", "key_name"})
        }
)
public class KeyValuePair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name", nullable = false)
    private String key;

    /**
     * Renamed column to avoid SQL reserved keyword conflicts.
     */
    @Column(name = "kv_value")
    private String value;

    /**
     * Owning side of the relation.
     * A KeyValuePair cannot exist without a DomainEntity.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_entity_id", nullable = false)
    @JsonIgnore
    private DomainEntity domainEntity;

    // --------------------------------------------------------
    // equals / hashCode (JPA-safe, proxy-safe)
    // --------------------------------------------------------

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        KeyValuePair that = (KeyValuePair) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "KeyValuePair(id=" + id + ", key=" + key + ")";
    }

    // --------------------------------------------------------
    // Getter / Setter
    // --------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    public void setDomainEntity(DomainEntity domainEntity) {
        this.domainEntity = domainEntity;
    }
}