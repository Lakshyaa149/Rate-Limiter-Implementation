import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

class CustomerRequest{

    long timeStamp;

    CustomerRequest(long timeStamp){
        this.timeStamp=timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
public class RollingWindowRateLimiter<L,V> extends RateLimiter {
    private Integer maxRequests;
    private Integer maxRequestTotalSeconds;

    HashMap<Integer, BlockingQueue<CustomerRequest>> customerRequestsInWindow = new HashMap<>();

    RollingWindowRateLimiter(Integer requests, Integer maxRequestTotalSeconds) {
        this.maxRequests = requests;
        this.maxRequestTotalSeconds = maxRequestTotalSeconds;

    }
    @Override
    boolean rateLimit(int customerId) {
           long currentTimeStampInMillis = getCurrentTimeInMillis();
           synchronized (this) {
               System.out.println("Got request at timeStamp " + currentTimeStampInMillis);
               BlockingQueue<CustomerRequest> customerRequests = null;
               if (!customerRequestsInWindow.containsKey(customerId)) {
                   customerRequests = new LinkedBlockingQueue<>(this.maxRequests);
               } else {
                   customerRequests = customerRequestsInWindow.get(customerId);
                   if (customerRequests.remainingCapacity() == 0 && (customerRequests.peek().timeStamp + this.maxRequestTotalSeconds * 1000) > currentTimeStampInMillis) {
                       System.out.println("blocking the requests arrived at timestamp" + currentTimeStampInMillis);
                       return false;
                   }
                   if (customerRequests.remainingCapacity() == 0) {
                       System.out.println("saving  the requests arrived at timestamp" + currentTimeStampInMillis + "removing timestamp" + customerRequests.poll().getTimeStamp());
                   }
               }
               CustomerRequest customerRequest = new CustomerRequest(currentTimeStampInMillis);
               customerRequests.add(customerRequest);
               customerRequestsInWindow.put(customerId, customerRequests);
           }

            return true;
    }

    public long getCurrentTimeInMillis() {
        return  System.currentTimeMillis();
   }

    public static void main(String[] args) throws InterruptedException {
        RollingWindowRateLimiter rollingWindowRateLimiter=new RollingWindowRateLimiter(10,1);
        IntStream.range(1,30).parallel().forEach(request->System.out.println(rollingWindowRateLimiter.rateLimit(1)));
       System.out.println("after checking");
        Thread.sleep(10000);
        IntStream.range(1,30).parallel().forEach(request->System.out.println(rollingWindowRateLimiter.rateLimit(1)));
    }



}
