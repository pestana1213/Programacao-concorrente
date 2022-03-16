import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.*;


class NotEnoughFunds extends  Exception{}
class InvalidAccount extends  Exception{}


/*

2. Implemente uma classe Banco que ofereça os métodos da interface abaixo, para crédito, débito e consulta
do saldo total de um conjunto de contas.
Considere um número fixo de contas, definido no construtor do Banco, com saldo inicial nulo.
Utilize exclusão mútua ao nível do objecto Banco.

interface Bank {
    void deposit(int id, int val) throws InvalidAccount;
    void withdraw(int id, int val) throws InvalidAccount, NotEnoughFunds;
    int totalBalance(int accounts[]) throws InvalidAccount;
}

 */

class Bank {

    public static class Account {
        int balance;

        //ou então ,
        public synchronized int balance() {
            return balance;
        }

        public synchronized void deposit(int val) {
            balance += val;
        }

        public synchronized void withdraw(int val) throws NotEnoughFunds {
            if (balance < val) throw new NotEnoughFunds();
            balance -= val;
        }

    }

    //public Account [] accounts;
    HashMap<Integer,Account> accounts = new HashMap<>();
    Lock l = new ReentrantLock();
    int lastId = 0;

    int createAccount(int balance){
        Account c = new Account();
        l.lock();
        lastId += 1;
        int id = lastId;
        c.deposit(balance);
        accounts.put(lastId,c);
        l.unlock();
        return id;
    }

    int createAccount(int balance){
        Account c = new Account();
        c.deposit(balance);
        l.lock();
        try {
            lastId += 1;
            int id = lastId;
            c.deposit(balance);
            accounts.put(lastId, c);
            return id;
        }
        finally {
            l.unlock();
        }
    }

    public void deposit (int id, int val) throws InvalidAccount{

        l.lock();
        try {
            Account c = accounts.get(id);
            if (c == null) throw new InvalidAccount();
            c.deposit(val);
        }
        finally {
            l.unlock();
        }
    }

    public void withdraw (int id, int val) throws InvalidAccount{
        l.lock();
        try {
            Account c = accounts.get(id);
            if (c == null) throw new InvalidAccount();
            c.withdraw(val);
        }
        finally {
            l.unlock();
        }
    }


    /*
        private Account get(int id) throws InvalidAccount {
            if (id < 0 || id >= accounts.length) throw new InvalidAccount() ;
            return accounts[id];
        }
    */
        public Bank(int n) {
            accounts = new Account[n];
            for (int i = 0; i < accounts.length; i++){
                accounts[i] = new Account();
            }
        }

    public /* synchronized */ void deposit(int id, int val) throws InvalidAccount {
        // accounts[id].deposit((val));

        Account c = get(id);
        synchronized (this) {         // só concorrência a nível do banco
            c.deposit(val);
        }

    }


    public /* synchronized */ void withdraw(int id, int val) throws InvalidAccount, NotEnoughFunds {
        Account c = get(id);
        synchronized (this) {         // só concorrência a nível do banco
            c.withdraw(val);
        }
    }

    public /* synchronized */ int totalBalance(int[] accounts) throws InvalidAccount {
        int total = 0;
        for (int id :accounts) {
            total += get(id).balance();
        }
        return total;
    }

    //EXERCÍCIO 3//

    public /* synchronized */ void transfer(int from, int to, int val) throws InvalidAccount, NotEnoughFunds {

        /*
        withdraw(from,val);
        deposit(from,val);
         */
        if (from == to) return;     //Sincronizaacao ao nivel das contas
        Account cfrom = get(from);
        Account cTo = get(to);
        Account o1,o2;

        if (from<to){
            o1 = cfrom;
            o2 = cTo;
        }
        else{
            o2 = cfrom;
            o1 = cTo;
        }
        synchronized (o1){
            synchronized (o2){
                cfrom.withdraw(val);
                cTo.deposit(val);
            }
        }

        /*
        get(from).withdraw(val); Sincronizacao ao nivel do banho
        get(to).deposit(val);

         */
    }


}

class Transferer extends Thread{
    final int iterations;
    final Bank b;

    Transferer (int iterations, Bank b) {
        this.iterations = iterations;
        this.b= b;
    }

    public void run(){
        Random r = new Random();
        for (int i = 0; i < iterations ; i++){
            try {
                int from = r.nextInt(b.accounts.length);
                int to = r.nextInt(b.accounts.length);
                b.transfer(from,to,1);
            } catch (InvalidAccount | NotEnoughFunds e) {
                e.printStackTrace();
            }
        }
    }
}


class Depositor extends Thread {
    final int iterations;
    final Bank b;

    Depositor(int iterations, Bank b) {
        this.iterations = iterations;
        this.b= b;
    }

    public void run(){
        for (int i = 0; i < iterations ; i++){
            try {
                b.deposit( i % b.accounts.length, 1);
            } catch (InvalidAccount e) {
                e.printStackTrace();
            }
        }
    }
}


class Observer extends Thread {
    final int iterations;
    final Bank b;

    Observer(int iterations, Bank b) {
        this.iterations = iterations;
        this.b= b;
    }

    public void run(){
       try{
           int NC = 10;
           int []todasContas = new int[NC];
           for (int i = 0; i<iterations;++i){
               int balance = b.totalBalance(todasContas);
               if (balance != NC * 1000000){
                   System.out.println("saldo errado: " + balance);
               }
           }
       }
       catch (Exception e ){}
    }
}

class Main5 {
    public static void main(String[] args) throws InterruptedException, InvalidAccount {
        final int N = Integer.parseInt(args[0]);
        final int NC = Integer.parseInt(args[1]);
        final int I = Integer.parseInt(args[2]);

        Bank b = new Bank(NC);
        Thread[] a = new Thread[N];                       // para guardar as diferentes Threads

        int[] todasContas = new int[NC];

        for (int i = 0; i < NC; i++) {
            todasContas[i] = i;
        }
        for (int i = 0; i < NC; i++) {
            b.deposit(i, 1000000);
        }

        // for (int i=0; i<NC; i++) {b.deposit(i,1000); }           // em comentário, para começarmos com slados a 0

        // for (int i = 0; i<N; i++){ a[i] = new Depositor(I, b); }
        for (int i = 0; i < N; i++) {
            a[i] = new Transferer(I, b);
        }

        new Observer(I,b).start();

        for (int i = 0; i < N; i++) {
            a[i].start();
        }
        for (int i = 0; i < N; i++) {
            a[i].join();
        }

        System.out.println(b.totalBalance(todasContas));                    // Para mostrar o valor final da Thread
    }
}