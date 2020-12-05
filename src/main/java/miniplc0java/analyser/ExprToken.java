package miniplc0java.analyser;

import java.util.ArrayList;

public class ExprToken {
    ExprType type;
    ExprToken(ExprType type){
        this.type=type;
    }
}

class Uint extends ExprToken{
    int value;
    Uint(ExprType type,int value){
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
    ArrayList<ExprToken> args=new ArrayList<ExprToken>();
    func function;
    Call_func(ExprType type,func func){
        super(type);
        this.function=func;
    }
}