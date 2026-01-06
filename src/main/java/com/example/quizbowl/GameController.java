package com.example.quizbowl;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://trishul.us-east-1.elasticbeanstalk.com",
        "https://d33ss8nc3ru9k4.cloudfront.net",
        "https://d3ge92rx18q4i4.cloudfront.net",
        "https://quizbowl.game-manager.org",
        "https://api.quizbowl.game-manager.org"
})
public class GameController {

    private final GameService gameService;
    private final AuthService authService;
    private final GameStreamService streamService;

    public GameController(GameService gameService, AuthService authService, GameStreamService streamService) {
        this.gameService = gameService;
        this.authService = authService;
        this.streamService = streamService;
    }

    @GetMapping
    public GameState getState(@RequestParam(name = "gameId", defaultValue = "default") String gameId) {
        return gameService.getState(gameId);
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam(name = "gameId", defaultValue = "default") String gameId) {
        SseEmitter emitter = streamService.register();
        try {
            emitter.send(SseEmitter.event().name("game").data(gameService.getState(gameId)));
        } catch (Exception ignored) {
        }
        return emitter;
    }

    @PostMapping("/team-names")
    public void setTeamNames(@RequestParam(name = "gameId", defaultValue = "default") String gameId,
                             @RequestBody TeamNamesRequest request,
                             @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.requireAdmin(token);
        gameService.setTeamNames(gameId, request.getTeamAName(), request.getTeamBName());
    }

    @PostMapping("/reset")
    public void resetGame(@RequestParam(name = "gameId", defaultValue = "default") String gameId,
                          @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.requireAdmin(token);
        gameService.resetGame(gameId);
    }

    @PostMapping("/next-tossup")
    public void nextTossup(@RequestParam(name = "gameId", defaultValue = "default") String gameId,
                           @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.requireAdmin(token);
        gameService.nextTossup(gameId);
    }

    @PostMapping("/award-tossup")
    public void awardTossup(@RequestParam(name = "gameId", defaultValue = "default") String gameId,
                            @RequestBody AwardTossupRequest request,
                            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.requireAdmin(token);
        gameService.awardTossup(gameId, request.getTeam());
    }

    @PostMapping("/award-bonus")
    public void awardBonus(@RequestParam(name = "gameId", defaultValue = "default") String gameId,
                           @RequestBody AwardBonusRequest request,
                           @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.requireAdmin(token);
        gameService.awardBonus(gameId, request.getPoints());
    }
}
