package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createLobby() {
        String lobbyId = lobbyService.createLobby();
        return ResponseEntity.ok(Map.of("lobbyId", lobbyId));
    }

    @PostMapping("/join/{lobbyId}")
    public ResponseEntity<String> joinLobby(@PathVariable String lobbyId, @RequestBody Map<String, String> payload) {
        String playerName = payload.get("playerName");
        boolean success = lobbyService.joinLobby(lobbyId, playerName);

        if (success) {
            return ResponseEntity.ok("Player " + playerName + " added to the lobby.");
        } else {
            return ResponseEntity.badRequest().body("Lobby not found or already full.");
        }
    }


    @GetMapping("/{lobbyId}/players")
    public ResponseEntity<List<String>> getPlayers(@PathVariable String lobbyId) {
        List<String> players = lobbyService.getPlayers(lobbyId);
        if (players == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(players);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<String>>> getAllLobbies() {
        return ResponseEntity.ok(lobbyService.getAllLobbies());
    }

    @PostMapping("/{lobbyId}/remove")
    public ResponseEntity<String> removePlayer(@PathVariable String lobbyId, @RequestParam String playerName) {
        boolean success = lobbyService.removePlayer(lobbyId, playerName);
        if (success) {
            return ResponseEntity.ok("Player removed from the lobby");
        } else {
            return ResponseEntity.badRequest().body("Player or lobby not found");
        }
    }

    @GetMapping("/{lobbyId}")
    public ResponseEntity<Map<String, String>> getLobby(@PathVariable String lobbyId) {
        Map<String, String> response = new HashMap<>();
        response.put("lobbyId", lobbyId);

        // You can add more lobby details here (host name, players, etc.)
        response.put("status", "Active");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{lobbyId}")
    public ResponseEntity<String> deleteLobby(@PathVariable String lobbyId) {
        boolean success = lobbyService.deleteLobby(lobbyId);
        if (success) {
            return ResponseEntity.ok("Lobby deleted");
        } else {
            return ResponseEntity.badRequest().body("Lobby not found");
        }
    }
}
