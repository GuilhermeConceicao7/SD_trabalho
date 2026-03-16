package sd2526.trab.api;

import java.net.URI;

public class ServiceInfo {

  private final String serviceName;
  private final String domain;
  private final URI uri;
  private volatile long lastSeen;

  public ServiceInfo(String serviceName, String domain, URI uri) {
    this.serviceName = serviceName;
    this.domain = domain;
    this.uri = uri;
    this.lastSeen = System.currentTimeMillis();
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getDomain() {
    return domain;
  }

  public URI getUri() {
    return uri;
  }

  public long getLastSeen() {
    return lastSeen;
  }

  public void updateLastSeen() {
    this.lastSeen = System.currentTimeMillis();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ServiceInfo))
      return false;
    ServiceInfo other = (ServiceInfo) obj;
    return this.serviceName.equals(other.serviceName) &&
        this.domain.equals(other.domain) &&
        this.uri.equals(other.uri);
  }

  @Override
  public int hashCode() {
    return (serviceName + domain + uri.toString()).hashCode();
  }

  @Override
  public String toString() {
    return serviceName + "@" + domain + "\t" + uri.toString();
  }
}