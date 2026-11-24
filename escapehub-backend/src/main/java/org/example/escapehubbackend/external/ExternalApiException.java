package org.example.escapehubbackend.external;





public class  ExternalApiException extends RuntimeException {

  public enum Reason {

     TIMEOUT,
    NOT_FOUND,
   UNAVAILABLE,
   INVALID_INPUT,
   HTTP_ERROR
}

private final Reason reason;


public ExternalApiException(Reason reason, String message) {

   super(message);
   this.reason = reason;

}

public ExternalApiException(Reason reason,  String message, Throwable cause) {
   
    super(message, cause);
    this.reason = reason;
}

 public Reason getReason() {
   
   return reason;


}

}
