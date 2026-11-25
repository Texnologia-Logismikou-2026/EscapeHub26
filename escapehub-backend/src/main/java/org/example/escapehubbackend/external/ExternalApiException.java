package org.example.escapehubbackend.external;





public class  ExternalApiException extends RuntimeException {

  public enum Reason {

     TIMEOUT,
    NOT_FOUND,
    RATE_LIMITED,
    HTTP_ERROR,
   UNAVAILABLE,
   INVALID_RESPONSE,
   INVALID_INPUT,
   
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
