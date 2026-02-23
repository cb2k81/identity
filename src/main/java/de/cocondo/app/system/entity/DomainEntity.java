package de.cocondo.app.system.entity;

import de.cocondo.app.system.entity.metadata.KeyValuePair;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@EntityListeners({DomainEntityListener.class, AuditingEntityListener.class})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class DomainEntity implements Identifyable, Auditable, Taggable {

    @Id
    protected String id;

    @Version
    protected Long persistenceVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<String> tags = new HashSet<>();

    protected String createdBy;
    protected LocalDateTime createdAt;
    protected String lastModifiedBy;
    protected LocalDateTime lastModifiedAt;

    @OneToMany(mappedBy = "domainEntity",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    protected Set<KeyValuePair> keyValuePairs = new HashSet<>();

    // ----------------------------------------
    // equals / hashCode (JPA-safe, FINAL)
    // ----------------------------------------

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DomainEntity that = (DomainEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(id=" + id + ")";
    }

    // ----------------------------------------
    // Getter / Setter
    // ----------------------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getPersistenceVersion() { return persistenceVersion; }

    public Set<String> getTags() { return tags; }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public Set<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void addKeyValue(String key, String value) {
        KeyValuePair kv = new KeyValuePair();
        kv.setDomainEntity(this);
        kv.setKey(key);
        kv.setValue(value);
        this.keyValuePairs.add(kv);
    }

    public void removeKeyValue(String key) {
        this.keyValuePairs.removeIf(kv -> kv.getKey().equals(key));
    }

    public String getValueByKey(String key) {
        return this.keyValuePairs.stream()
                .filter(kv -> kv.getKey().equals(key))
                .map(KeyValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    public Set<KeyValuePair> getAllKeyValues() {
        return keyValuePairs;
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
}