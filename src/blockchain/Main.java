package blockchain;



import java.util.*;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static final int NUM_OF_THREAD = 10;

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        BlockChain blockChain = new BlockChain();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);

        for (int i = 0; i < NUM_OF_THREAD; i++) {
            executor.submit(new Miner(blockChain, i + 1, random.nextLong()));
        }
        while(blockChain.lastBlockId <= 10) Thread.sleep(10000);
        executor.shutdown();
        executor.shutdown();
        System.exit(0);
    }
}

class BlockChain {
    final int CHAIN_MAX_LEN = 20;
    final long INCREASE_TIME = 1;
    final long DECREASE_TIME = 5;
    Random random = new Random();
    int numOfZero = 0;
    int lastBlockId = 0;
    String lastBlockHash = "0";
    long lastBlockTime = new Date().getTime();

    public synchronized void tryNewBlock(int minerId, int id, long magic, String hash, long timestamp) {
        if (id != lastBlockId + 1 || id == 16) return;
        System.out.printf("%nBlock:%nCreated by miner%d%n", minerId);
        System.out.println("miner" +  minerId + " gets 100 VC");
        System.out.printf("Id: %d%nTimestamp: %d%nMagic number: %d%nHash of the previous block:%n%s%n" +
                "Hash of the block:%n%s%n", id, timestamp, magic, lastBlockHash, hash);
        if (lastBlockHash.equals("0")) {
            System.out.println("Block data:\n" + "No transaction");
        } else {
            System.out.println("Block data:");
            System.out.println(transaction());
        }
        /////////////////////////////////////////////////////////// if previosBlock = 0 no message
        long duration = (timestamp - lastBlockTime)/1000;
        lastBlockTime = timestamp;
        lastBlockHash = hash;
        lastBlockId++;
        System.out.printf("Block was generating for %d seconds%n", duration);
        if (duration < INCREASE_TIME) {
            System.out.printf("N was increased to %d%n", ++numOfZero);
            return;
        }
        if (duration > DECREASE_TIME && numOfZero > 0) {
            System.out.printf("N was decreased to %d%n", --numOfZero);
            return;
        }
        System.out.println("N stays the same");


    }

    public String transaction() {
        int a = (int) ( Math.random() * 3 );
        String s = "";
        if (a == 1) {
            s = "miner9 sent 30 VC to miner1\n" +
                    "miner9 sent 30 VC to miner2\n" +
                    "miner9 sent 30 VC to Nick";
        } else if (a == 2) {
            s = "miner9 sent 10 VC to Bob\n" +
                    "miner7 sent 10 VC to Alice\n" +
                    "Nick sent 1 VC to ShoesShop\n" +
                    "Nick sent 2 VC to FastFood\n" +
                    "Nick sent 15 VC to CarShop\n" +
                    "miner7 sent 90 VC to CarShop";
        } else {
            s = "CarShop sent 10 VC to Worker1\nCarShop sent 10 VC to Worker2";
        }

        return s;
    }
}



class Miner implements Runnable {
    private int minerId;
    private Random random;
    BlockChain blockChain;

    public Miner(BlockChain blockChain, int minerId, long seed) {
        this.minerId = minerId;
        this.blockChain = blockChain;
        random = new Random(seed);
    }

    @Override
    public void run() {
        int lastBlockId = blockChain.lastBlockId;
        int blockId = lastBlockId + 1;
        String lastHash = blockChain.lastBlockHash;
        while (lastBlockId < blockChain.CHAIN_MAX_LEN) {
            if (lastBlockId != blockChain.lastBlockId) {
                lastBlockId = blockChain.lastBlockId;
                blockId = lastBlockId + 1;
                lastHash = blockChain.lastBlockHash;
            }
            int magic = random.nextInt();
            long timestamp = new Date().getTime();
            String newHash = applySha256(String.format("%d%d%d%s", blockId, timestamp, magic, lastHash));
            int cnt = 0;
            while(newHash.charAt(cnt) == '0' && cnt < newHash.length()) cnt++;
            if (cnt >= blockChain.numOfZero || cnt <= 2) blockChain.tryNewBlock(minerId, blockId, magic, newHash, timestamp);//blockChain.numOfZero
        }
    }

    String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Applies sha256 to our input
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


}
