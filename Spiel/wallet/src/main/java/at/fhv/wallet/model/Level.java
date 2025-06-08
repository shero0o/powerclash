package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gadget")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer cost;
}
