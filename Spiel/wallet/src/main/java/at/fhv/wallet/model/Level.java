package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "level")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Level {
    @Id
    @Column(name = "level_id")
    private Long id;
    private String name;
    private Integer cost;
}
