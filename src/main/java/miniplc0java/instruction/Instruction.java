package miniplc0java.instruction;

import miniplc0java.analyser.StackVar;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    int arg1;
    int arg2;

    public Instruction(Operation opt) {
        this.opt = opt;
    }

    public Instruction(Operation opt, int x) {
        this.opt = opt;
        arg1=x;
    }

    public Instruction(Operation opt, int x,int y) {
        this.opt = opt;
        arg1=x;
        arg2=y;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (o == null || getClass() != o.getClass())
//            return false;
//        Instruction that = (Instruction) o;
//        return opt == that.opt && Objects.equals(x, that.x);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(opt, x);
//    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public void setArg1(int arg1){this.arg1=arg1;}

    @Override
    public String toString() {
        switch (this.opt) {
            case not:
                return "Not";
            case neg_i:
                return "NegI";
            case nop:
                return "Nop()";
            case push:
                return String.format("Push(%s)",this.arg1);
            case pop:
                return "Pop()";
            case popn:
                return String.format("PopN(%s)",this.arg1);
            case dup:
                return "Dup()";
            case loca:
                return String.format("LocA(%s)",this.arg1);
            case arga:
                return String.format("ArgA(%s)",this.arg1);
            case globa:
                return String.format("GlobA(%s)",this.arg1);
            case load_8:
                return "Load8";
            case load_16:
                return "Load16";
            case load_32:
                return "Load32";
            case load_64:
                return "Load64";
            case store_8:
                return "Store8";
            case store_16:
                return "Store16";
            case store_32:
                return "Store32";
            case store_64:
                return "Store64";
            case stackalloc:
                return String.format("StackAlloc(%s)",this.arg1);
            case add_i:
                return "AddI";
            case sub_i:
                return "SubI";
            case mul_i:
                return "MulI";
            case div_i:
                return "DivI";
            case cmp_i:
                return "CmpI";
            case set_lt:
                return "SetLt";
            case set_gt:
                return "SetGt";
            case br:
                return String.format("Br(%s)",this.arg1);
            case br_false:
                return String.format("BrFalse(%s)",this.arg1);
            case br_true:
                return String.format("BrTrue(%s)",this.arg1);
            case call:
                return String.format("call(%s)",this.arg1);
            case ret:
                return "Ret";
            case callname:
                return String.format("CallName(%s)",this.arg1);
            default:
                return "Panic";
        }
    }
}
