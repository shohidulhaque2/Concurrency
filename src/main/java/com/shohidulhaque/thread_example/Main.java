package com.shohidulhaque.thread_example;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    /**
     * invariant
     * only one thread can read or write on numberOfReaders, used to co-ordinate readers and writers
     * multiple readers can read
     * only one writer can write
     */

    static class SharedResource{

        static Logger Logger = LoggerFactory.getLogger(SharedResource.class);
        static class LockedState{
            Object lock = new Object();
            //state that is only accessible obtaining the monitor of lock
            int numberOfReaders;
        }
        volatile  LockedState lockedState = new LockedState();

        public void read(int readerId){
            Logger.info("reader["+ readerId +"]" +  ": is trying to read the shared resource.");
            synchronized (this.lockedState.lock){
                ++this.lockedState.numberOfReaders;
                Logger.info("reader ["+ readerId +"]" +  ": number of readers " + this.lockedState.numberOfReaders + ".  reading and doing some work." + "\u00a5");
            }
            try{
                //do some work by reading the resource
                Logger.info("reader ["+ readerId +"]" +  ": there is no writer writing. number of readers " + this.lockedState.numberOfReaders + " .reading and doing some work." + "\u00a5");
                Thread.sleep(1000 * 1);

            } catch (InterruptedException e){}
            synchronized (this.lockedState.lock){
                Logger.info("reader ["+ readerId +"]" +  ": there is no writer writing. number of reader " + this.lockedState.numberOfReaders + " .finished doing the work." + "\u00a5");
                --this.lockedState.numberOfReaders;
                if(this.lockedState.numberOfReaders == 0){
                    this.lockedState.lock.notifyAll();
                }
            }
        }

        public void write(int writerId){
            Logger.info("writer ["+ writerId +"]" +  " is trying to write the shared resource. number of readers are " + this.lockedState.numberOfReaders);
            synchronized (this.lockedState.lock){
                //thread can only do work when conditions are met <- when there are no readers and there cannot be any readers when writing is taking place
                while(this.lockedState.numberOfReaders != 0){
                    try{
                        Logger.info("writer ["+ writerId +"]" +  ": there are readers. number of readers " + this.lockedState.numberOfReaders + " .going to wait.\u00a5");
                        this.lockedState.lock.wait();
                    } catch (InterruptedException e){}
                }
                //do some work and write to resource
                try{
                    Logger.info("writer ["+ writerId +"]" +  ": there are no readers. doing some work." + "\u00a5");
                    Thread.sleep(1000 * 1);
                    Logger.info("writer ["+ writerId +"]" +  ": finished doing the work." + "\u00a5");
                }catch (InterruptedException e){}
            }
        }
    }

    static class Reader implements Runnable{

        static int ReaderIdCounter;
        final int id;
        volatile SharedResource sharedResource;
        public Reader(SharedResource sharedResource){
            id = ReaderIdCounter;
            ++ReaderIdCounter;
            this.sharedResource = sharedResource;
        }

        @Override
        public void run() {
            //just to make my intentions obvious
            long sleepTime  = 0;
            int numberOfReads = (int)(Math.random() * 5);
            while(numberOfReads-- > 0){
                sleepTime = (long) (Math.random() * 1000 * 1);

                sharedResource.read(this.id);
                try{
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e){}

            }
        }
    }

    static class Writer implements Runnable {

        static int WriterIdCounter;
        final int id;
        volatile SharedResource sharedResource;
        public Writer(SharedResource sharedResource){
            id = WriterIdCounter;
            ++WriterIdCounter;
            this.sharedResource = sharedResource;
        }
        @Override
        public void run() {
            //just to make my intentions obvious
            long sleepTime  = 0;
            int numberOfWrites = (int)(Math.random() * 5);
            while(numberOfWrites-- > 0){
                sleepTime = (long) (Math.random() * 1000 * 1);

                sharedResource.write(this.id);
                try{
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e){}

            }

        }
    }

    public static void main(String ...args){
        final int NUMBER_OF_READERS = 10;
        final int NUMBER_OF_WRITERS = 10;
        List<Thread> threads = new LinkedList<>();
        SharedResource sharedResource = new SharedResource();
        int j = 0;
        for(int i = 0; i < NUMBER_OF_READERS; ++i, ++j){
            threads.add(new Thread(new Reader(sharedResource)));
        }
        for(int i = 0; i < NUMBER_OF_WRITERS; ++i, ++j){
            threads.add(new Thread(new Writer(sharedResource)));
        }
        Collections.shuffle(threads);
        threads.stream().forEach(x -> x.start());
    }

}
