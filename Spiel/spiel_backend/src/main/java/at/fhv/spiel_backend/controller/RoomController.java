package at.fhv.spiel_backend.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    public RoomController(){
    }

    @PostMapping("/join")
    public joinRoom(){}


    public leaveRoom(){}
}
