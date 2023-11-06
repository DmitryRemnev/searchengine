package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "page")
@NoArgsConstructor
@Getter
@Setter
@Table(indexes = @Index(name = "path_index",columnList = "path"))
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    public Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "site_id")
    public Site site;

    @Column(columnDefinition = "VARCHAR(500)", nullable = false, name = "path")
    public String path;

    @Column(columnDefinition = "INT", nullable = false, name = "code")
    public Integer code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false, name = "content")
    public String content;
}
