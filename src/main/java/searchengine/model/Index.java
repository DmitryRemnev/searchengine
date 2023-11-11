package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "_index")
@NoArgsConstructor
@Getter
@Setter
@Table(indexes = @javax.persistence.Index(name = "page_lemma_index", columnList = "page_id, lemma_id", unique = true))
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "page_id")
    private Page page;

    @ManyToOne
    @JoinColumn(nullable = false, name = "lemma_id")
    private Lemma lemma;

    @Column(columnDefinition = "FLOAT", nullable = false, name = "rating")
    private Float rating;
}
