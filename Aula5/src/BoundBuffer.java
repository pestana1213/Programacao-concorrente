public class BoundBuffer<Integer> {
    private int[] buffer;
    int iget = 0;
    int iput = 0;
    int relees = 0;

    BoundBuffer(int n){
        buffer = new int[n];
    }

     synchronized int  get() throws InterruptedException{
        while(relees == 0){
            wait();
        }
        int res;
        res = buffer[iget];
        if (iget +1 == buffer.length){
            iget = 0;
        }
        else {
            iget += 1;
        }
        relees -= 1;
        notifyAll();

        if(relees == buffer.length-1){
            notifyAll();
        }
        return res;
    }

    synchronized void put(int x) throws InterruptedException {
        while(relees == buffer.length){
            wait();
        }
        buffer[iput] = x;
        if (iput + 1 == buffer.length){
            iput = 0;
        }
        else {
            iput += 1;
        }
        relees += 1;
        notifyAll();
        if(relees == 1){
            notifyAll();
        }
    }
}

class Barreira{
    private final int N;
    private int c = 0;
    private boolean w = false;
    public Barreira (int N){
        this.N = N;
    }

    public void await() throws InterruptedException{
        c += 1;
        if (c == 1 ) w = true;

        if (c==N) {
            notifyAll();
            c = 0;
            w = false;
        }
        else while (c<N) wait();

        while(c<N){
            wait();
        }
    }

}

class Main5 {
    public static void main(String[] args) throws InterruptedException {
        BoundBuffer buff = new BoundBuffer(20);

        new Thread ( () -> {
            for(int i=1;;++i){
                System.out.println("vou fazer put de: " + i);
                try {
                    buff.put(i);
                    System.out.println("Fiz put de: " + i);
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread ( () -> {
            for(int i=1;;++i){
                System.out.println("vou fazer get de: " + i);
                try {
                    buff.get();
                    System.out.println("Retornou: " + i);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
