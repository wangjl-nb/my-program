package miniplc0java.analyser;

import miniplc0java.instruction.Instruction;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class ExprToken {
    ExprType type;
    ExprToken(ExprType type){
        this.type=type;
    }
}

class Uint extends ExprToken{
    long value;
    Uint(ExprType type,long value){
        super(type);
        this.value=value;
    }
}

class Var extends ExprToken{
    StackVar stack_var;
    Var(ExprType type,StackVar stack_var){
        super(type);
        this.stack_var=stack_var;
    }
}

class Call_func extends ExprToken{
    ArrayList<ArrayList<ExprToken>> args=new ArrayList<ArrayList<ExprToken>>();
    func function;
    Call_func(ExprType type,func func){
        super(type);
        this.function=func;
    }
    public void add_arg(ArrayList<ExprToken> args){
        this.args.add(args);
    }
}

class While_br {
    ArrayList<Instruction> br_list=new ArrayList<Instruction>();
    int size;
    While_br(int size){
        this.size=size;
    }
}