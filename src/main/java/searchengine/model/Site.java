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
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false, name = "status")
    private Status status;

    @Column(columnDefinition = "DATETIME", nullable = false, name = "status_time")
    private Date statusTime;

    @Column(columnDefinition = "TEXT", name = "last_error")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, name = "url")
    private String url;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, name = "name")
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    private Set<Page> pageList = new HashSet<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    private Set<Lemma> lemmaList = new HashSet<>();
}
