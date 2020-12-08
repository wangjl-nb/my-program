package miniplc0java.analyser;

public class StackVar {
    boolean isConstant;
    boolean isInitialized;
    String name;
    public int type;
    boolean is_fn;
    Var_position position;
    StackVar(Boolean isConstant,Boolean isInitialized,String name,int type,Var_position position,boolean is_fn){
        this.is_fn=is_fn;
        this.position=position;
        this.isInitialized=isInitialized;
        this.isConstant=isConstant;
        this.name=name;
        this.type=type;//1 Unknown,2 int,3 void,4 double
    }
    public boolean isConstant() {
        return isConstant;
    }

    public String getName(){return name;}

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isIs_fn(){return is_fn;}

    public void setType(int type){this.type=type;}

    public Boolean is_string_equal(String target){
        return this.name.contentEquals(target);
    }

    public void setInitialized(Boolean isInitialized){
        this.isInitialized=isInitialized;
    }
}

class Var_position{
    int position;
    int var_type;//1 global,2 args,3 locals
    Var_position(int position,int var_type){
        this.position=position;
        this.var_type=var_type;
    }
}

class stack_point{
    int points;
    boolean is_fn;
    stack_point(int points,boolean is_fn){
        this.points=points;
        this.is_fn=is_fn;
    }
}
