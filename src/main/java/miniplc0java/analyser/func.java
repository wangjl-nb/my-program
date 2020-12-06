package miniplc0java.analyser;

import miniplc0java.instruction.Instruction;

import java.util.ArrayList;

public class func {
    ArrayList<Instruction> operations=new ArrayList<>();//函数操作
    public int args_num;
    public int locals_num;
    public int global_num;
    public int return_num;
    public int func_num;
    boolean is_inner_func;
    public func(int args_num,int locals_num,int global_num,int return_num,boolean is_inner_func){
        this.args_num=args_num;
        this.is_inner_func=is_inner_func;
        this.locals_num=locals_num;
        this.global_num=global_num;
        this.return_num=return_num;
    }
    public void AddOperations(Instruction operation){
        this.operations.add(operation);
    }

    public ArrayList<Instruction> getOperations() {
        return operations;
    }

    public void set_locals_num(int num){
        this.locals_num=num;
    }
    public void set_return_num(int num){
        this.return_num=num;
    }
    public void set_args_num(int num){
        this.args_num=num;
    }
}
