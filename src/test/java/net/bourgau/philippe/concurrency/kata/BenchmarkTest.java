package net.bourgau.philippe.concurrency.kata;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class BenchmarkTest {

    private static BufferedWriter output;

    @Parameterized.Parameters(name = "{0} clients each sending {1} messages")
    public static Collection<Object[]> parameters() {
        ArrayList<Object[]> parameters = new ArrayList<>();
        parameters.add(new Object[]{1000, 10});
        parameters.add(new Object[]{100, 1000});
        parameters.add(new Object[]{10, 100000});
        parameters.add(new Object[]{1, 10000000});
        return parameters;
    }

    @Parameterized.Parameter(0)
    public int clientCount;
    @Parameterized.Parameter(1)
    public int messagePerClientCount;

    private final ConcurrentChatRoom chatRoom = ChatRoomFactory.createChatRoom();
    private List<Client> clients = new ArrayList<>();

    @BeforeClass
    public static void before_all() throws Exception {
        output = new BufferedWriter(new OutputStreamWriter(System.out));
        output.write("clients,incoming messages,outgoing messages,duration(seconds),scenario, outgoing throughput(message/second)\n");
        output.flush();
        Thread.sleep(500);
    }

    @Before
    public void before_each() throws Exception {
        for (int i = 0; i < clientCount; i++) {
            Client client = new Client("Client#" + i, chatRoom, new NullOutput());
            client.enter();
            clients.add(client);
        }
    }

    @Test(timeout = 30000)
    public void benchmark() throws Exception {
        long startMillis = System.currentTimeMillis();

        for (int iMessage = 0; iMessage < messagePerClientCount; iMessage++) {
            for (Client client : clients) {
                client.announce("wazzzaaa");
            }
        }

        shutdown();

        double duration = (System.currentTimeMillis() - startMillis) / 1000.;
        int outgoingMessages = clientCount * messagePerClientCount * clientCount;
        output.write(String.format("%s,%s,%s,%s,%s x %s,%s\n", clientCount, messagePerClientCount, outgoingMessages,
                duration, clientCount, messagePerClientCount, outgoingMessages / duration));
    }

    @After
    public void after_each() throws Exception {
        shutdown();
        output.flush();
    }

    @AfterClass
    public static void after_all() throws Exception {
        output.close();
    }

    private void shutdown() throws InterruptedException {
        chatRoom.shutdownAndAwaitTermination(15, TimeUnit.SECONDS);
    }
}
