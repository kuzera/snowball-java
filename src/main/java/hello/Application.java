package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import sun.management.counter.StringCounter;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
public class Application {

  static class Self {
    public String href;
  }

  static class Links {
    public Self self;

    @Override
    public String toString() {
        return "Links{" +
                "self='" + self.href + '\'' +
                '}';
    }
  }

static class PlayerState {
    public Integer x;
    public Integer y;
    public String direction;
    public Boolean wasHit;
    public Integer score;

    @Override
    public String toString() {
        return "PlayerState{" +
                "x,y='" + x + ',' + y + '\'' +
                ", direction='" + direction + '\'' +
                ", wasHit='" + wasHit + '\'' +
                ", score='" + score + '\'' +
                '}';
    }
  }

  static class Arena {
    public List<Integer> dims;
    public Map<String, PlayerState> state;

    @Override
    public String toString() {
        return "Arena{" +
                "dims='" + dims + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
  }

  static class ArenaUpdate {
    public Links _links;
    public Arena arena;
    @Override
    public String toString() {
        return "ArenaUpdate{" +
                "_links='" + _links + '\'' +
                ", arena='" + arena + '\'' +
                '}';
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    // System.out.println(arenaUpdate);
    // String[] commands = new String[]{"F", "R", "L", "T"};
    int i = new Random().nextInt(10);
    // return commands[i];
    String me = arenaUpdate._links.self.href;
    PlayerState myState = arenaUpdate.arena.state.get(me);
    System.out.println("[" + i + "]" + me + ": " + myState);
    String command = "T";
    if (i<=3) {
        if (myState.direction.equals("W")) {
            command = "L";
        } else if (myState.direction.equals("E")) {
            command = "R";
        } else if (myState.direction.equals("N")) {
            command = "R";
        } else if (myState.direction.equals("S")) {
            command = "L";
        }
    } else if (i==4) {
        command = "F";
    }
    return command;
  }

}

