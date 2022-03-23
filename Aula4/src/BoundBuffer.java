import java.util.concurrent.Semaphore;

public class BoundBuffer<Integer> {
    private int[] buffer;
    int iget = 0;
    int iput = 0;
    Semaphore items;
    Semaphore  slots;
    Semaphore multget = new Semaphore(1);
    Semaphore multput = new Semaphore(1);

    BoundBuffer(int n){
        buffer = new int[n];

         items = new Semaphore(0);
         slots = new Semaphore(n); //todas as n posicoes do buffer estao livres

    }

    int get() throws InterruptedException{
        int res;
        items.acquire(); //Se o tamanho do buffer for 0 entao fica bloqueado
        multget.acquire();
        res = buffer[iget];
        if (iget +1 == buffer.length){
            iget = 0;
        }
        else {
            iget += 1;
        }
        multget.release();
        slots.release();
        return res;
    }

    void put(int x) throws InterruptedException {
        slots.acquire();
        multput.acquire();
        buffer[iput] = x;
        if (iput + 1 == buffer.length){
            iput = 0;
        }
        else {
            iput += 1;
        }
        multput.release();
        items.release();
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
