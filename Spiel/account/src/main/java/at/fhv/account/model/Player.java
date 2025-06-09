package at.fhv.account.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

}
