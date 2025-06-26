package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brawler")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Brawler {
    @Id
    @Column(name = "brawler_id")
    private Long id;
    private String name;
    private Integer cost;
    private Integer healthPoints;

}
