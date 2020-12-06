package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * 符号表
     */
//    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();
    public StackVar[] stack_vars = new StackVar[1000];
    public stack_point[] stack_points = new stack_point[1000];
    public func[] func_list = new func[1000];//函数列表
    public int stack_top1 = -1;
    public int stack_top2 = -1;
    public int func_top = -1;
    StackVar tmp_fn = null;//记录还没有定义类型的函数
    StackVar tmp_var = null;//记录还没有定义类型的变量
    func tmp_func = null;//目前所在的函数体
    int local_tmp = 0;//当前函数的局部变量个数
    int args_tmp = 0;//当前函数的输入参数个数
    ArrayList<ExprToken> expr_tokens=new ArrayList<ExprToken>();
    Map<String, func> func_map = new HashMap<String, func>();

//    Stack<Integer> stack_points=new Stack<Integer>();;

    /**
     * 下一个变量的栈偏移
     */
//    int nextOffset = 0;
    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        stack_point point = new stack_point(0, true);
        stack_points[++stack_top2] = point;
        initial_stack();
        func_list[++func_top] = new func(0, 0, stack_top1, 0,false);
        tmp_func=func_list[0];
        tmp_func.func_num=func_top;
        func_map.put(stack_vars[stack_top1].name, func_list[func_top]);//函数映射
        analyseProgram();
        return instructions;
    }


    //添加标准库
    public void initial_stack() {
        stack_vars[++stack_top1] = new StackVar(true, true, "getint", 2, new Var_position(0, 1));
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 1,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "getdouble", 4, new Var_position(1, 1));
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 1,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "getchar", 2, new Var_position(2, 1));
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 1,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putint", 3, new Var_position(3, 1));
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 0,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putdouble", 3, new Var_position(4, 1));
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 0,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putchar", 3, new Var_position(5, 1));
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 0,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putstr", 3, new Var_position(6, 1));
        func_map.put(stack_vars[stack_top1].name, new func(1, 0, stack_top1, 0,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "putln", 3, new Var_position(7, 1));
        func_map.put(stack_vars[stack_top1].name, new func(0, 0, stack_top1, 0,true));
        stack_vars[++stack_top1] = new StackVar(true, true, "_start", 1, new Var_position(8, 1));//初始化函数_start
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            do {
                peekedToken = tokenizer.nextToken();
            } while (peekedToken.getTokenType().equals(TokenType.ANNOTATION));
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            Token tmp_token;
            do {
                tmp_token = tokenizer.nextToken();
            } while (tmp_token.getTokenType() == TokenType.ANNOTATION);
            return tmp_token;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    //从总栈获取标识符
    private StackVar stack_get_full(String target) {
        int tmp_top1 = stack_top1, tmp_top2 = stack_top2;
        while (tmp_top2 >= 0) {
            for (int i = stack_points[tmp_top2].points; i <= tmp_top1; i++) {
                if (stack_vars[i].is_string_equal(target))
                    return stack_vars[i];
            }
            tmp_top1 = stack_points[tmp_top2].points - 1;
            tmp_top2--;
        }
        return null;
    }

    //从当前栈获取标识符
    private StackVar stack_get_current(String target) {
        int tmp_top1 = stack_top1, tmp_top2 = stack_top2;

        for (int i = stack_points[tmp_top2].points; i <= tmp_top1; i++) {
            if (stack_vars[i].is_string_equal(target))
                return stack_vars[i];
        }
        return null;
    }

    //新开辟一个函数局部栈
    private void newFnStack() {
        stack_point point = new stack_point(stack_top1 + 1, true);
        stack_points[++stack_top2] = point;
    }

    //新开辟一个语句块局部栈
    private void newStmtStack() {
        stack_point point = new stack_point(stack_top1 + 1, false);
        stack_points[++stack_top2] = point;
    }

    //回退一个栈
    private void deleteStack() {
//        debug_print.print_current_stack(stack_top1, stack_top2, stack_vars, stack_points);
        stack_top1 = stack_points[stack_top2].points - 1;
        stack_top2--;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos, int type,Var_position position/**/) throws
            AnalyzeError {
        if (stack_get_current(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.stack_vars[++stack_top1] = new StackVar(isConstant, isInitialized, name, type,position);
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    private boolean isInitialize(String name, Pos curPos) throws AnalyzeError {
        var entry = stack_get_full(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isInitialized();
        }
    }

    /**
     * item -> function | decl_stmt
     * program -> item*
     **/
    private void analyseProgram() throws CompileError {
        while (check(TokenType.LET_KW) || check(TokenType.FN_KW) || check(TokenType.CONST_KW)) {
            if (check(TokenType.FN_KW)) {
                analyseFn();
            } else {
                analyseDeclStmt();
            }
        }
        expect(TokenType.EOF);
//        System.out.println("ok!!!!!");
//        debug_print.print_stack(stack_top1, stack_top2, stack_vars, stack_points);
        debug_print.print_funcs(func_list, func_top);
    }

    /**
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * fn fib(x: int) -> int{}
     */
    private void analyseFn() throws CompileError {
        is_fun_first = true;
        expect(TokenType.FN_KW);
        Token fn_name = expect(TokenType.IDENT);
        if (func_map.get(fn_name.getValueString()) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, fn_name.getStartPos());
        } else {
            Var_position position=new Var_position(stack_top1+1,1);
            addSymbol(fn_name.getValueString(), true, true, fn_name.getStartPos(), 1,position);
            tmp_fn = stack_vars[stack_top1];
        }
        args_tmp = 0;
        local_tmp = 0;
        func_list[++func_top] = new func(0, 0, stack_top1, 0,false);
        tmp_func = func_list[func_top];
        tmp_func.func_num=func_top;
        func_map.put(fn_name.getValueString(), tmp_func);
        expect(TokenType.L_PAREN);
        newFnStack();
        if (check(TokenType.CONST_KW) || check(TokenType.IDENT))
            analyseFunctionParamList();
        tmp_func.set_args_num(args_tmp);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        int ty = analyseFnTy();
        tmp_fn.setType(ty);
        if (ty == 3)
            tmp_func.set_return_num(0);
        else {
            tmp_func.set_return_num(1);
        }
        analyseBlockStmt();
        tmp_func.set_locals_num(local_tmp);
        tmp_func=func_list[0];
    }


    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList() throws CompileError {
        analyseFunctionParam();
        args_tmp++;
        while (check(TokenType.COMMA)) {
            expect(TokenType.COMMA);
            analyseFunctionParam();
            args_tmp++;
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     */
    private void analyseFunctionParam() throws CompileError {
        Var_position position=new Var_position(args_tmp+1,2);
        if (check(TokenType.CONST_KW)) {
            expect(TokenType.CONST_KW);
            Token param = expect(TokenType.IDENT);
            addSymbol(param.getValueString(), true, true, param.getStartPos(), 1,position);
        } else {
            Token param = expect(TokenType.IDENT);
            addSymbol(param.getValueString(), true, false, param.getStartPos(), 1,position);
        }
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        tmp_var.setType(analyseVarTy());
    }

    /**
     * ty -> IDENT
     */
    private int analyseFnTy() throws CompileError {
        if (check(TokenType.INTEGER)) {
            expect(TokenType.INTEGER);
            return 2;
        } else {
            expect(TokenType.VOID);
            return 3;
        }
    }

    private int analyseVarTy() throws CompileError {
//            if (check(TokenType.INTEGER)) {
        expect(TokenType.INTEGER);
        return 2;
//            }
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     */
    private void analyseDeclStmt() throws CompileError {
        if (check(TokenType.LET_KW)) {
            analyseLetDeclStmt();
        } else {
            analyseConstDeclStmt();
        }
        local_tmp++;
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private void analyseLetDeclStmt() throws CompileError {
        expect(TokenType.LET_KW);
        Token var = expect(TokenType.IDENT);
        Var_position position;
        if(stack_top2==0)
            position=new Var_position(stack_top1+1,1);
        else
            position=new Var_position(local_tmp,3);
        addSymbol(var.getValueString(), false, false, var.getStartPos(), 1,position);
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        tmp_var.setType(analyseVarTy());
        if (check(TokenType.ASSIGN)) {
            expect(TokenType.ASSIGN);
            tmp_var.setInitialized(true);
            analyseExpr();
        }
        expect(TokenType.SEMICOLON);
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private void analyseConstDeclStmt() throws CompileError {
        expect(TokenType.CONST_KW);
        Token var = expect(TokenType.IDENT);
        Var_position position;
        if(stack_top2==0)
            position=new Var_position(stack_top1+1,1);
        else
            position=new Var_position(local_tmp,3);
        addSymbol(var.getValueString(), true, true, var.getStartPos(), 1,position);
        tmp_var = stack_vars[stack_top1];
        expect(TokenType.COLON);
        tmp_var.setType(analyseVarTy());
        expect(TokenType.ASSIGN);
        analyseExpr();
        expect(TokenType.SEMICOLON);
    }
//------------------------------------------------------------------表达式操作

    /**
     * expr ->
     * operator_expr
     * | negate_expr
     * | assign_expr
     * | as_expr
     * | call_expr
     * | literal_expr
     * | ident_expr
     * | group_expr
     */
    boolean expr_flag=false;//判断是不是第一次进入表达式
    private void analyseExpr() throws CompileError {
        Boolean flag = false,inner_flag=false;
        if(!expr_flag){
            inner_flag=true;
            expr_flag=true;
        }
        if (analyseNegateExpr())
            flag = true;
        if (!flag) {
            if (analyseAssignOrIdentOrCallExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            if (analyseLiteralExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            if (analyseGroupExpr()) {
                flag = true;
            }
        }
        if (!flag) {
            throw new AnalyzeError(ErrorCode.IncompleteExpression, peek().getStartPos());
        }
        if (check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            analyseVarTy();
        } else {
            if (analyseBinaryOperator()) {
                analyseExpr();
            }
        }
        if(inner_flag){
            debug_print.print_expr(expr_tokens);
            expr_flag=false;
            expr_tokens.clear();
        }
    }


    /**
     * negate_expr -> '-' expr
     */
    private Boolean analyseNegateExpr() throws CompileError {
        if (check(TokenType.MINUS)) {
            expect(TokenType.MINUS);
            expr_tokens.add(new ExprToken(ExprType.NEG));
            analyseExpr();
            return true;
        }
        return false;
    }

    /**
     * ident_expr -> IDENT
     * -----------------
     * assign_expr -> l_expr '=' expr
     * l_expr -> IDENT
     * -----------------
     * call_expr -> IDENT '(' call_param_list? ')'
     * call_param_list -> expr (',' expr)*
     */
    private boolean analyseAssignOrIdentOrCallExpr() throws CompileError {
        if (check(TokenType.IDENT)) {
            Token var = expect(TokenType.IDENT);
            if (check(TokenType.ASSIGN)) {
                expr_tokens.clear();
                expect(TokenType.ASSIGN);
                StackVar tmp_stack_var = stack_get_full(var.getValueString());
                if (tmp_stack_var != null && !tmp_stack_var.isConstant) {
                    tmp_stack_var.setInitialized(true);
                    analyseExpr();
                    return true;
                } else {
                    if (tmp_stack_var == null)
                        throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                    else {
                        throw new AnalyzeError(ErrorCode.AssignToConstant, var.getStartPos());
                    }
                }
            } else if (check(TokenType.L_PAREN)) {//函数调用
                expr_tokens.add(new ExprToken(ExprType.FUNC));
                expect(TokenType.L_PAREN);
                param_cnt = 0;
                if (!check(TokenType.R_PAREN))
                    analyseCallParamList();
                if (func_map.get(var.getValueString()) == null) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                }
                if (param_cnt != func_map.get(var.getValueString()).args_num) {
                    throw new AnalyzeError(ErrorCode.InvalidParamNum, var.getStartPos());
                }
                expect(TokenType.R_PAREN);
                return true;
            } else {
                StackVar tmp_stack_var = stack_get_full(var.getValueString());
                if (tmp_stack_var == null) {
                    throw new AnalyzeError(ErrorCode.NotDeclared, var.getStartPos());
                }
                if (!tmp_stack_var.isInitialized) {
                    throw new AnalyzeError(ErrorCode.NotInitialized, var.getStartPos());
                }
                expr_tokens.add(new Var(ExprType.Var,tmp_stack_var));
                return true;
            }
        }
        return false;
    }

    /**
     * private boolean analyseLiteralExpr()
     */
    int param_cnt = 0;

    private void analyseCallParamList() throws CompileError {
        param_cnt++;
        analyseExpr();
        while (check(TokenType.COMMA)) {
            param_cnt++;
            expect(TokenType.COMMA);
            analyseExpr();
        }
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private boolean analyseLiteralExpr() throws CompileError {
        if (check(TokenType.UINT_LITERAL)) {
            Token tmp=expect(TokenType.UINT_LITERAL);
            expr_tokens.add(new Uint(ExprType.UINT_LITERAL,(int)tmp.getValue()));
            return true;
        } else if (check(TokenType.STRING_LITERAL)) {
            expect(TokenType.STRING_LITERAL);
            return true;
        }
        return false;
        //暂不实现字符和浮点数
    }

    /**
     * group_expr -> '(' expr ')'
     */

    private boolean analyseGroupExpr() throws CompileError {
        if (check(TokenType.L_PAREN)) {
            expect(TokenType.L_PAREN);
            expr_tokens.add(new ExprToken(ExprType.L_PAREN));
            analyseExpr();
            expect(TokenType.R_PAREN);
            expr_tokens.add(new ExprToken(ExprType.R_PAREN));
            return true;
        }
        return false;
    }

    /**
     * binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
     */
    private boolean analyseBinaryOperator() throws CompileError {
        switch (peek().getTokenType()) {
            case PLUS:
                expect(TokenType.PLUS);
                expr_tokens.add(new ExprToken(ExprType.PLUS));
                return true;
            case MINUS:
                expect(TokenType.MINUS);
                expr_tokens.add(new ExprToken(ExprType.MINUS));
                return true;
            case MUL:
                expect(TokenType.MUL);
                expr_tokens.add(new ExprToken(ExprType.MUL));
                return true;
            case DIV:
                expect(TokenType.DIV);
                expr_tokens.add(new ExprToken(ExprType.DIV));
                return true;
            case EQ:
                expect(TokenType.EQ);
                expr_tokens.add(new ExprToken(ExprType.EQ));
                return true;
            case NEQ:
                expect(TokenType.NEQ);
                expr_tokens.add(new ExprToken(ExprType.NEG));
                return true;
            case LE:
                expect(TokenType.LE);
                expr_tokens.add(new ExprToken(ExprType.LE));
                return true;
            case GE:
                expect(TokenType.GE);
                expr_tokens.add(new ExprToken(ExprType.GE));
                return true;
            case GT:
                expect(TokenType.GT);
                expr_tokens.add(new ExprToken(ExprType.GT));
                return true;
            case LT:
                expect(TokenType.LT);
                expr_tokens.add(new ExprToken(ExprType.LT));
                return true;
            default:
                return false;
        }
    }

    //--------------------------------------------------------------------------
//----------------------------------------------------------语句
    boolean is_fun_first = false;

    /**
     * block_stmt -> '{' stmt* '}'
     */
    private void analyseBlockStmt() throws CompileError {
        if (!is_fun_first) {
            newStmtStack();
        } else {
            is_fun_first = false;
        }
        expect(TokenType.L_BRACE);
        while (!check(TokenType.R_BRACE))
            analyseStmt();
        expect(TokenType.R_BRACE);
        deleteStack();
    }

    /**
     * stmt ->
     * expr_stmt
     * | decl_stmt
     * | if_stmt
     * | while_stmt
     * | break_stmt
     * | continue_stmt
     * | return_stmt
     * | block_stmt
     * | empty_stmt
     */
    private void analyseStmt() throws CompileError {
        if (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseDeclStmt();
        } else if (check(TokenType.IF_KW)) {
            analyseIfStmt();
        } else if (check(TokenType.WHILE_KW)) {
            analyseWhileStmt();
        }
//      else if(check(TokenType.BREAK_KW))
//      else if(check(TokenType.CONTINUE_KW))
        else if (check(TokenType.RETURN_KW)) {
            analyseReturnStmt();
        } else if (check(TokenType.L_BRACE)) {
            analyseBlockStmt();
        } else if (check(TokenType.SEMICOLON)) {
            expect(TokenType.SEMICOLON);
        } else {
            analyseExpr();
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * return_stmt -> 'return' expr? ';'
     */
    private void analyseReturnStmt() throws CompileError {
        expect(TokenType.RETURN_KW);
        analyseExpr();
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     */
    private void analyseWhileStmt() throws CompileError {
        expect(TokenType.WHILE_KW);
        analyseExpr();
        analyseBlockStmt();
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)
     */
    private void analyseIfStmt() throws CompileError {
        expect(TokenType.IF_KW);
        analyseExpr();
        analyseBlockStmt();
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                analyseExpr();
                analyseBlockStmt();
            } else {
                analyseBlockStmt();
                break;
            }
        }
    }


    //----------------------------------------------------------
    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 如果下一个 token 是 const 就继续
//        while (nextIf(TokenType.Const) != null) {
//            // 变量名
//            var nameToken = expect(TokenType.Ident);
//
//            // 等于号
//            expect(TokenType.Equal);
//
//            // 常表达式
//            analyseConstantExpression();
//
//            // 分号
//            expect(TokenType.Semicolon);
//
//            addSymbol(nameToken.getValueString(),true,true,nameToken.getStartPos());
//        }
    }

    // <变量声明> ::= {<变量声明语句>}
// <变量声明语句> ::= 'var'<标识符>['='<表达式>]';'
    private void analyseVariableDeclaration() throws CompileError {
//        throw new Error("Not implemented");
//        while(nextIf(TokenType.Var)!=null){
//            var nameToken=expect(TokenType.Ident);
//            if(nextIf(TokenType.Equal)!=null){
//                analyseExpression();
//                addSymbol(nameToken.getValueString(),true,false,nameToken.getStartPos());
//            }else{
//                addSymbol(nameToken.getValueString(),false,false,nameToken.getStartPos());
//                instructions.add(new Instruction(Operation.LIT, 0));
//            }
//            expect(TokenType.Semicolon);
//        }
    }

    // <语句序列> ::= {<语句>}
// <语句> :: = <赋值语句> | <输出语句> | <空语句>
// <赋值语句> :: = <标识符>'='<表达式>';'
// <空语句> :: = ';'
// <输出语句> :: = 'print' '(' <表达式> ')' ';'
    private void analyseStatementSequence() throws CompileError {
//        throw new Error("Not implemented");
//
//        while(check(TokenType.Semicolon)||check(TokenType.Print)||check(TokenType.Ident)){
//            analyseStatement();
//        }
    }

    private void analyseStatement() throws CompileError {
//        throw new Error("Not implemented");
//        if(nextIf(TokenType.Semicolon)!=null){
//            ;
//        }else if(check(TokenType.Print)){
//            analyseOutputStatement();
//        }else if(check(TokenType.Ident)){
//            analyseAssignmentStatement();
//        }
    }

    // <常表达式> ::= [<符号>]<无符号整数>
    private void analyseConstantExpression() throws CompileError {
//        throw new Error("Not implemented");
//        if(check(TokenType.Plus)){
//            next();
//        }else if(check(TokenType.Minus)){
//            next();
//            instructions.add(new Instruction(Operation.LIT, -Integer.parseInt(expect(TokenType.Uint).getValueString())));
//            return;
//        }
//        instructions.add(new Instruction(Operation.LIT, Integer.parseInt(expect(TokenType.Uint).getValueString())));
    }

    // <表达式> ::= <项>{<加法型运算符><项>}
    private void analyseExpression() throws CompileError {
//        throw new Error("Not implemented");
//        analyseItem();
//        while(check(TokenType.Minus)||check((TokenType.Plus))){
//            boolean flag=check(TokenType.Minus);
//            next();
//            analyseItem();
//            if(flag){
//                instructions.add(new Instruction(Operation.SUB));
//            }else{
//                instructions.add(new Instruction(Operation.ADD));
//            }
//        }
    }

    // <赋值语句> ::= <标识符>'='<表达式>';'
    private void analyseAssignmentStatement() throws CompileError {
//        var nameToken=expect(TokenType.Ident);
//        if(isConstant(nameToken.getValueString(),nameToken.getStartPos())){
//            throw new AnalyzeError(ErrorCode.AssignToConstant,nameToken.getStartPos());
//        }
//        expect(TokenType.Equal);
//        analyseExpression();
//        expect(TokenType.Semicolon);
//        declareSymbol(nameToken.getValueString(),nameToken.getStartPos());
//        instructions.add(new Instruction(Operation.STO,getOffset(nameToken.getValueString(), nameToken.getStartPos())));
    }

    // <输出语句> :: = 'print' '(' <表达式> ')' ';'
    private void analyseOutputStatement() throws CompileError {
//        expect(TokenType.Print);
//        expect(TokenType.LParen);
//        analyseExpression();
//        expect(TokenType.RParen);
//        expect(TokenType.Semicolon);
//        instructions.add(new Instruction(Operation.WRT));
    }

    // <项> :: = <因子>{ <乘法型运算符><因子> }
    private void analyseItem() throws CompileError {
//        analyseFactor();
//        while(check(TokenType.Mult)||check((TokenType.Div))){
//            boolean flag=check(TokenType.Mult);
//            next();
//            analyseFactor();
//            if(flag)
//                instructions.add(new Instruction(Operation.MUL));
//            else
//                instructions.add(new Instruction(Operation.DIV));
//        }
//        throw new Error("Not implemented");
    }

    // <因子> ::= [<符号>]( <标识符> | <无符号整数> | '('<表达式>')' )
    private void analyseFactor() throws CompileError {
        boolean negate;
//        if (nextIf(TokenType.Minus) != null) {
//            negate = true;
//            // 计算结果需要被 0 减
//            instructions.add(new Instruction(Operation.LIT, 0));
//        } else {
//            nextIf(TokenType.Plus);
//            negate = false;
//        }
//
//        if (check(TokenType.Ident)) {
//            // 调用相应的处理函数
//            var nameToken=expect(TokenType.Ident);
//            if(!isInitialize(nameToken.getValueString(),nameToken.getStartPos())){
//                throw new AnalyzeError(ErrorCode.NotInitialized,nameToken.getStartPos());
//            }
//            instructions.add(new Instruction(Operation.LOD,getOffset(nameToken.getValueString(), nameToken.getStartPos())));
//        } else if (check(TokenType.Uint)) {
//            // 调用相应的处理函数
//            instructions.add(new Instruction(Operation.LIT,Integer.parseInt(expect(TokenType.Uint).getValueString())));
//        } else if (check(TokenType.LParen)) {
//            // 调用相应的处理函数
//            expect(TokenType.LParen);
//            analyseExpression();
//            expect(TokenType.RParen);
//        } else {
//            // 都不是，摸了
//            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
//        }
//
//        if (negate) {
//            instructions.add(new Instruction(Operation.SUB));
//        }
//        throw new Error("Not implemented");
    }
}
