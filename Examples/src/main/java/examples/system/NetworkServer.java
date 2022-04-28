package examples.system;

import lombok.Getter;

@Getter
public class NetworkServer {

    private String name;
    private int onlineCount = 20 + (int)(Math.random() * 10);
}
