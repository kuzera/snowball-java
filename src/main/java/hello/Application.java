package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import sun.management.counter.StringCounter;

import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.api.core.ApiFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.bigquery.storage.v1.*;
import com.google.protobuf.Descriptors;
import org.json.JSONArray;
import org.json.JSONObject;
 
import java.io.IOException;
import java.time.Instant;


@SpringBootApplication
@RestController
public class Application {

    String lastCommand = "T";
    static class WriteCommittedStream {

        final JsonStreamWriter jsonStreamWriter;
    
        public WriteCommittedStream(String projectId, String datasetName, String tableName) throws IOException, Descriptors.DescriptorValidationException, InterruptedException {
    
          try (BigQueryWriteClient client = BigQueryWriteClient.create()) {
    
            WriteStream stream = WriteStream.newBuilder().setType(WriteStream.Type.COMMITTED).build();
            TableName parentTable = TableName.of(projectId, datasetName, tableName);
            CreateWriteStreamRequest createWriteStreamRequest =
                    CreateWriteStreamRequest.newBuilder()
                            .setParent(parentTable.toString())
                            .setWriteStream(stream)
                            .build();
    
            WriteStream writeStream = client.createWriteStream(createWriteStreamRequest);
    
            jsonStreamWriter = JsonStreamWriter.newBuilder(writeStream.getName(), writeStream.getTableSchema()).build();
          }
        }
    
        public ApiFuture<AppendRowsResponse> send(Arena arena) {
          Instant now = Instant.now();
          JSONArray jsonArray = new JSONArray();
    
          arena.state.forEach((url, playerState) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("x", playerState.x);
            jsonObject.put("y", playerState.y);
            jsonObject.put("direction", playerState.direction);
            jsonObject.put("wasHit", playerState.wasHit);
            jsonObject.put("score", playerState.score);
            jsonObject.put("player", url);
            jsonObject.put("timestamp", now.getEpochSecond() * 1000 * 1000);
            jsonArray.put(jsonObject);
          });
    
          return jsonStreamWriter.append(jsonArray);
        }
    
      }
    
      final String projectId = ServiceOptions.getDefaultProjectId();
      final String datasetName = "snowball";
      final String tableName = "events";
    
      final WriteCommittedStream writeCommittedStream;
    
      public Application() throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
        writeCommittedStream = new WriteCommittedStream(projectId, datasetName, tableName);
      }

      
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
    writeCommittedStream.send(arenaUpdate.arena);


    // System.out.println(arenaUpdate);
    // String[] commands = new String[]{"F", "R", "L", "T"};
    int i = new Random().nextInt(20);
    // return commands[i];
    String me = arenaUpdate._links.self.href;
    PlayerState myState = arenaUpdate.arena.state.get(me);
    System.out.println("[" + i + "]" + me + ": " + myState);
    String command = "T";
    if (i<=2) {
        if (lastCommand == "T") {
            command = "L";
        } else if (lastCommand == "L") {
            command = "F";
        } else if (lastCommand == "F") {
            command = "R";
        } else if (lastCommand == "R") {
            command = "T";
        }


        // if (myState.direction.equals("W")) {
        //     command = "L";
        // } else if (myState.direction.equals("E")) {
        //     command = "R";
        // } else if (myState.direction.equals("N")) {
        //     command = "R";
        // } else if (myState.direction.equals("S")) {
        //     command = "L";
        // }
    } else if (i==3) {
        command = "F";
    }
    lastCommand = command;
    return command;
  }

}

