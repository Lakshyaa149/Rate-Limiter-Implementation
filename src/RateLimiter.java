public abstract class RateLimiter<L,V> {



    //Perform rate limiting logic for provided customer ID. Return true if the
    // request is allowed, and false if it is not.
    abstract boolean rateLimit(int customerId);

}
