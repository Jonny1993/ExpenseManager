package model;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExpenseManagerExecutor {

    private static ExpenseManagerExecutor sInstance;
    private Executor diskIo;

    private ExpenseManagerExecutor(Executor diskIo){
        this.diskIo =  diskIo;
    }

    public static ExpenseManagerExecutor getInstance(){
        if (sInstance == null){
            sInstance = new ExpenseManagerExecutor(Executors.newSingleThreadExecutor());
        }
        return sInstance;
    }


    public Executor getDiskIo(){
        return diskIo;
    }


}