package at.fhv.wallet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gadget")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Gadget {
    @Id
    @Column(name = "gadget_id")
    private Long id;
    private String name;
    private Integer cost;
    private String description;
}
