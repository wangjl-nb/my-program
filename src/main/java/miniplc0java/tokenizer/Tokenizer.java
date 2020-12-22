package miniplc0java.tokenizer;


import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

import java.awt.event.MouseAdapter;
import java.util.regex.Pattern;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if (Character.isAlphabetic(peek)||peek=='_') {
            return lexIdentOrKeyword();
        }else if(peek=='"') {
            return lexString();
        }
        else if(peek=='\''){
            return lexChar();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexChar() throws TokenizeError{
        Pos now=it.currentPos();
        StringBuffer str=new StringBuffer();
        str.append(it.nextChar());
        Boolean isPreTurn=false;
        while(!it.isEOF()){
            if(it.peekChar()=='\''&&!isPreTurn)
                break;
            if(it.peekChar()=='\\')
                if(isPreTurn)
                    isPreTurn=false;
                else
                    isPreTurn=true;
            else{
                isPreTurn=false;
            }
            str.append(it.nextChar());
        }
        if(it.peekChar()=='\''){
            str.append(it.nextChar());
        }
        String pattern="\'([^'\\\\]|(\\\\[\\\\\"'ntr]))\'";
//        System.out.println(str+"\n"+pattern);
        boolean isMatch= Pattern.matches(pattern,str);
        if(isMatch){
            int num;
            String string=str.substring(1,str.length()-1);
            if(string.length()==1)
                num=(int)string.charAt(0);
            else{
                if(string.charAt(1)=='n'){
                    num=(int)'\n';
                }else if(string.charAt(1)=='r'){
                    num=(int)'\r';
                }else if(string.charAt(1)=='t'){
                    num=(int)'\t';
                }else{
                    num=(int)string.charAt(1);
                }
            }
            return new Token(TokenType.UINT_LITERAL,(long)num,now,it.currentPos());
        }else{
            throw new TokenizeError(ErrorCode.InvalidChar,now);
        }
    }

    private Token lexString() throws TokenizeError{
        Pos now=it.currentPos();
        StringBuffer str=new StringBuffer();
        str.append(it.nextChar());
        Boolean isPreTurn=false;
        while(!it.isEOF()){
            if(it.peekChar()=='"'&&!isPreTurn)
                break;
            if(it.peekChar()=='\\')
                if(isPreTurn)
                    isPreTurn=false;
                else
                    isPreTurn=true;
            else{
                isPreTurn=false;
            }
            str.append(it.nextChar());
        }
        if(it.peekChar()=='"'){
            str.append(it.nextChar());
        }
        //[^"\\]
        String pattern="\"(([^\"\\\\])|(\\\\[\\\\\"'nrt]))*\"";
//        System.out.println(str+"\n"+pattern);
        boolean isMatch= Pattern.matches(pattern,str);
        String string=str.substring(1,str.length()-1);
        if(isMatch){
            return new Token(TokenType.STRING_LITERAL,string,now, it.currentPos());
        }else{
            throw new TokenizeError(ErrorCode.InvalidString,now);
        }
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
//        throw new Error("Not implemented");
        long sum = 0;
        boolean isdouble=false;
        StringBuilder token = new StringBuilder();
        Pos now = it.currentPos();
        char ch;
        while (Character.isDigit(it.peekChar())) {
            ch=it.nextChar();
            token.append(ch);
            sum = sum * 10 + ch - '0';
        }
        if(it.peekChar()=='.'){
            isdouble=true;
            token.append(it.nextChar());
            if(!Character.isDigit(it.peekChar()))
                throw new TokenizeError(ErrorCode.InvalidInput,it.currentPos());
            while(Character.isDigit(it.peekChar())){
                token.append(it.nextChar());
            }
            ch = it.peekChar();
            if(ch == 'e' || ch == 'E'){
                token.append(it.nextChar());
                ch = it.peekChar();
                if(ch == '+' || ch == '-')
                    token.append(it.nextChar());
                if(!Character.isDigit(it.peekChar()))
                    throw new TokenizeError(ErrorCode.InvalidInput,it.currentPos());
                while(Character.isDigit(it.peekChar())){
                    token.append(it.nextChar());
                }
            }
            return new Token(TokenType.DOUBLE_LITERAL,Double.parseDouble(token.toString()),now,it.currentPos());
        }
        else
            return new Token(TokenType.UINT_LITERAL,sum,now,it.currentPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        try{
            Pos now=it.currentPos();
            String res=String.valueOf(it.nextChar());
            while(Character.isDigit(it.peekChar())||Character.isAlphabetic(it.peekChar())||it.peekChar()=='_'){
                res+=it.nextChar();
            }
            if(res.contentEquals("fn")){
                return new Token(TokenType.FN_KW,"fn",now,it.currentPos());
            }
            if(res.contentEquals("break")){
                return new Token(TokenType.BREAK_KW,"break",now,it.currentPos());
            }
            if(res.contentEquals("continue")){
                return new Token(TokenType.CONTINUE_KW,"continue",now,it.currentPos());
            }
            if(res.contentEquals("let")){
                return new Token(TokenType.LET_KW,"let",now,it.currentPos());
            }
            if(res.contentEquals("const")){
                return new Token(TokenType.CONST_KW,"const",now,it.currentPos());
            }
            if(res.contentEquals("as")){
                return new Token(TokenType.AS_KW,"as",now,it.currentPos());
            }
            if(res.contentEquals("while")){
                return new Token(TokenType.WHILE_KW,"while",now,it.currentPos());
            }
            if(res.contentEquals("if")){
                return new Token(TokenType.IF_KW,"if",now,it.currentPos());
            }
            if(res.contentEquals("else")){
                return new Token(TokenType.ELSE_KW,"else",now,it.currentPos());
            }
            if(res.contentEquals("return")){
                return new Token(TokenType.RETURN_KW,"return",now,it.currentPos());
            }
            if(res.contentEquals("int")){
                return new Token(TokenType.INTEGER,"int",now, it.currentPos());
            }
            if(res.contentEquals("void")){
                return new Token(TokenType.VOID,"void",now,it.currentPos());
            }
            if(res.contentEquals("double")){
                return new Token(TokenType.DOUBLE,"double",now,it.currentPos());
            }
            return new Token(TokenType.IDENT,res,now,it.currentPos());
        }catch (Exception e){
            throw new Error("Compiler Error");
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        Pos now=it.currentPos();
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                if(it.peekChar()=='>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW,"->",now,it.currentPos());
                }
                return new Token(TokenType.MINUS,'-',it.previousPos(),it.currentPos());
                // 填入返回语句

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL,'*',it.previousPos(),it.currentPos());

            case '/':
                // 填入返回语句
                if(it.peekChar()=='/'){
                    while(it.nextChar()!='\n'&&!it.isEOF());
                    return new Token(TokenType.ANNOTATION,"//",now, it.currentPos());
                }else{
                    return new Token(TokenType.DIV,'/',it.previousPos(),it.currentPos());
                }

            // 填入更多状态和返回语句
            case '=':
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.EQ,"==",now,it.currentPos());
                }
                return new Token(TokenType.ASSIGN,'=',it.previousPos(),it.currentPos());
            case '!':
                if(it.nextChar()!='='){
                    throw new TokenizeError(ErrorCode.InvalidInput, now);
                }
                return new Token(TokenType.NEQ,"!=",now,it.currentPos());
            case '<':
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.LE,"<=",now,it.currentPos());
                }
                return new Token(TokenType.LT,'<',it.previousPos(),it.currentPos());
            case '>':
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.GE,">=",now,it.currentPos());
                }
                return new Token(TokenType.GT,'>',it.previousPos(),it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON,';',it.previousPos(),it.currentPos());
            case '(':
                return new Token(TokenType.L_PAREN,'(',it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN,')',it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE,'{',it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE,'}',it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA,',',it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON,':',it.previousPos(), it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
