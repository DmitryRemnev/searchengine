package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Index;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "page")
@NoArgsConstructor
@Getter
@Setter
@Table(indexes = @Index(name = "path_index", columnList = "path"))
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "site_id")
    private Site site;

    @Column(columnDefinition = "VARCHAR(500)", nullable = false, name = "path")
    private String path;

    @Column(columnDefinition = "INT", nullable = false, name = "code")
    private Integer code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false, name = "content")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private Set<searchengine.model.Index> indexSet = new HashSet<>();
}
