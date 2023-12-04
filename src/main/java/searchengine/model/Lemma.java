package searchengine.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "lemma")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "site_id")
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false, name = "lemma")
    private String lemma;

    @Column(columnDefinition = "INT", nullable = false, name = "frequency")
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE)
    private Set<searchengine.model.Index> indexSet = new HashSet<>();
}
