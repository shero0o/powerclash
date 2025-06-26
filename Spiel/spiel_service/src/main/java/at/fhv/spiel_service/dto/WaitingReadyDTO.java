package at.fhv.spiel_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaitingReadyDTO {
    private String playerId;
    private String roomId;
    private String brawlerId;  // NEU
    private Long levelId;    // NEU
    private String playerName;
}
