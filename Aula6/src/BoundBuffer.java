import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BBuffer {

    private int[] buf;
    private int iget = 0;
    private int iput = 0;
    private int nelems = 0;

    Lock l = new ReentrantLock();
    Condition notEmpty = l.newCondition();
    Condition notFull = l.newCondition();

    public BBuffer(int N) {
        buf = new int[N];
    }

    public int get() throws InterruptedException {
        l.lock();
        try {
            while (nelems == 0) wait();
            int res;
            res = buf[iget];
            iget = (iget+1) % buf.length;
            if(nelems==buf.length) {
                notFull.signal();
            }
            nelems -= 1;
            //notifyAll();
            return res;
        } finally {
            l.unlock();
        }
    }

    public void put(int v) throws InterruptedException {
        l.lock();
        try {
            while (nelems == buf.length) wait();
            buf[iput] = v;
            iput = (iput+1) % buf.length;
            nelems += 1;
            notEmpty.signal();
            //notifyAll();
        } finally {
            l.unlock();
        }
    }
}

class Warehouse{
    Lock l = new ReentrantLock();

    private class Item{

        int quant;
        Condition cond = l.newCondition();

    }

    Map<String,Item> map = new HashMap<>();

    private Item get(String s){
        Item item = map.get(s);
        if(item == null){
            item = new Item();
            map.put(s,item);
        }
        return item;
    }

    public void supply(String s,int quant){
        l.lock();
        try{
            Item item = get(s);
            item.quant += quant;
            item.cond.signalAll();

        }
        finally {
            l.unlock();
        }
    }
    public void consume(String[] items){
        l.lock();
        try{
            for (String s : items){
                Item item = get(s);
                while(item.quant == 0){
                    item.cond.await();
                }
                item.quant -= 1;

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            l.unlock();
        }

    }


}