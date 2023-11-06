package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "site")
@NoArgsConstructor
@Getter
@Setter
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    public Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false, name = "status")
    public Status status;

    @Column(columnDefinition = "DATETIME", nullable = false, name = "status_time")
    public Date statusTime;

    @Column(columnDefinition = "TEXT", name = "last_error")
    public String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, name = "url")
    public String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, name = "name")
    public String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<Page> pageList = new HashSet<>();
}
