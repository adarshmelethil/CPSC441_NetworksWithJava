/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author cow
 */
public class ScheduledExecutorService_Learning {
    private final ScheduledExecutorService scheduler = 
                Executors.newScheduledThreadPool(1);

    public void beepForAMinute() {
        System.out.println();
        final Runnable beeper = new Runnable() {
            @Override
            public void run() { System.out.println("beep"); }
        };

        final ScheduledFuture<?> beeperHandle = 
                scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() { 
                beeperHandle.cancel(true); 
            }
        }, 60, SECONDS);

//        scheduler.schedule(() -> {
//            beeperHandle.cancel(true);
//        }, 60 * 60, SECONDS);
    }
    
    public static void main(String[] args){
//        ScheduledExecutorService_Learning ses_l = new ScheduledExecutorService_Learning();
//        ses_l.beepForAMinute();
        System.out.println(SECONDS);
    }
}
