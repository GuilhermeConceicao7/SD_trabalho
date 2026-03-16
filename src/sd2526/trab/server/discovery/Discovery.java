package sd2526.trab.server.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import sd2526.trab.api.ServiceInfo;

public class Discovery {

  private static Logger Log = Logger.getLogger(Discovery.class.getName());

  static final public InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
  static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
  static final int DISCOVERY_RETRY_TIMEOUT = 5000;
  static final int MAX_DATAGRAM_SIZE = 65536;

  // Used separate the two fields that make up a service announcement.
  private static final String DELIMITER = "\t";

  private final String serviceName;
  private final String domain;
  private final String serviceURI;
  private final MulticastSocket ms;

  ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInfo>> serviceMap = new ConcurrentHashMap<>();

  public Discovery(String serviceName, String domain, String serviceURI) throws IOException {

    this.serviceName = serviceName;
    this.domain = domain;
    this.serviceURI = serviceURI;

    this.ms = new MulticastSocket(DISCOVERY_ADDR.getPort());
    this.ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
  }

  // DISCOVERY para ouvir outros serviços
  public Discovery() throws IOException {
    this.serviceName = null;
    this.domain = null;
    this.serviceURI = null;

    this.ms = new MulticastSocket(DISCOVERY_ADDR.getPort());
    this.ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
  }

  /**
   * Starts listener
   * 
   * @throws IOException
   */
  public void start() {

    new Thread(() -> {

      DatagramPacket packet = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

      while (true) {
        try {
          packet.setLength(MAX_DATAGRAM_SIZE);
          ms.receive(packet);
          String msg = new String(packet.getData(), 0, packet.getLength()).trim();

          String[] parts = msg.split(DELIMITER);
          if (parts.length != 2)
            continue;

          String serviceDomain = parts[0];
          String uriStr = parts[1];

          String[] sdParts = serviceDomain.split("@");
          if (sdParts.length != 2)
            continue;

          String svc = sdParts[0];
          String dom = sdParts[1];

          ServiceInfo info = new ServiceInfo(svc, dom, URI.create(uriStr));

          serviceMap.computeIfAbsent(svc, k -> new CopyOnWriteArrayList<>());
          List<ServiceInfo> list = serviceMap.get(svc);

          boolean exists = false;
          for (ServiceInfo si : list) {
            if (si.equals(info)) {
              si.updateLastSeen();
              exists = true;
              break;
            }
          }
          if (!exists)
            list.add(info);

          System.out.printf("FROM %s (%s) : %s\n",
              packet.getAddress().getHostName(),
              packet.getAddress().getHostAddress(),
              msg);

        } catch (IOException e) {
          // ignora erros temporários
        }
      }
    }, "DiscoveryListener").start();

    // Thread de anúncios periódicos
    if (serviceName != null && serviceURI != null) {
      new Thread(() -> {
        try {
          String msg = serviceName + "@" + domain + DELIMITER + serviceURI;
          DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, DISCOVERY_ADDR);
          while (true) {
            ms.send(packet);
            Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
          }
        } catch (Exception e) {
          Log.warning("Erro ao anunciar serviço: " + e.getMessage());
        }
      }, "DiscoveryAnnouncer").start();
    }

    // Thread para remover serviços inativos
    new Thread(() -> {
      while (true) {
        long now = System.currentTimeMillis();
        serviceMap.forEach((svc, list) -> list.removeIf(si -> now - si.getLastSeen() > DISCOVERY_RETRY_TIMEOUT));
        try {
          Thread.sleep(DISCOVERY_RETRY_TIMEOUT);
        } catch (InterruptedException ignored) {
        }
      }
    }, "DiscoveryCleanup").start();
  }

  /**
   * Returns the known services.
   * 
   * @param serviceName the name of the service being discovered
   * @param minReplies  - minimum number of requested URIs. Blocks until the
   *                    number is satisfied.
   * @return an array of URI with the service instances discovered.
   * 
   */
  public URI[] knownUrisOf(String serviceName, int minReplies) {

    while (true) {
      List<ServiceInfo> list = serviceMap.get(serviceName);
      if (list != null && list.size() >= minReplies) {
        return list.stream().map(ServiceInfo::getUri).toArray(URI[]::new);
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException ignored) {
      }
    }
  }

  // Main just for testing purposes
  public static void main(String[] args) throws Exception {
    Discovery discovery = new Discovery("Users", "fct",
        "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080");
    discovery.start();
  }
}
